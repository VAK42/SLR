import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.bind.MethodArgumentNotValidException;
import jakarta.persistence.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.Map;
/*
@SpringBootApplication = @Configuration + @EnableAutoConfiguration + @ComponentScan
@Configuration: Đánh Dấu Class Này Chứa Các @Bean → Spring Đọc
@EnableAutoConfiguration: Tự Đoán & Cấu Hình Dựa Trên Thư Viện Jar Có Trong Project
@ComponentScan: Quét Toàn Bộ Package → Tìm Class Có Annotation: @Component, @Service, @Repository, @Controller → Đăng Ký Vào IoC Container (ApplicationContext)
*/
@SpringBootApplication
public class SpringBootFramework {
  public static void main(String[] args) {
    /*
    SpringApplication.run() Khởi Động Toàn Bộ Spring Context:
    - Tạo ApplicationContext (IoC Container)
    - Chạy Auto-Configuration
    - Quét & Đăng Ký Tất Cả Bean
    - Khởi Động Embedded Server (Tomcat Mặc Định)
    */
    ApplicationContext ctx = SpringApplication.run(SpringBootFramework.class, args);
    /*
    ApplicationContext: Nơi Chứa Toàn Bộ Bean Đã Dc Khởi Tạo
    getBeanDefinitionCount() Return Số Lượng Bean Đang Dc Quản Lý
    */
    System.out.println(ctx.getBeanDefinitionCount());
  }
}
/*
@Configuration → Spring Dùng CGLIB Tạo Subclass Proxy Cho Class Này → Call @Bean Method Many Times → Luôn Return Cùng 1 Instance (Singleton)
* @Component | @Service | @Repository | @Controller | @RestController | @Configuration | @Bean
  → Spring Reg Bean Vào ApplicationContext
  → new → Ko Qua ApplicationContext → New Obj
  → @Autowired + DI → Qua ApplicationContext → Lấy Bean Singleton
  Use Case:
  → Inject Bean Vào Class Khác
  → Common Use Khi Develop App
  → Ko Cần Tự Call @Bean Method
  
* @Configuration → CGLIB Proxy Intercept Internal @Bean Method Call
  → Call service()
  → Qua ApplicationContext
  → Lấy Bean Singleton Instead Of New Obj
  Use Case:
  → Class Config Chứa Nhiều @Bean Có Dependency Lẫn Nhau
  → Call @Bean Method Inside @Configuration Vẫn Lấy Bean Singleton
  → Tránh New Obj Khi Call @Bean Method Directly
*/
@Configuration
class Config {
  /*
  @Bean: Khai Báo Method Này Trả Về 1 Obj Sẽ Dc Spring Quản Lý
  Method Name ("service"): Tên Bean Default Trong Container
  @Bean("customService"): Tên Bean Custom Dc Thêm Vào Container
  Spring Đảm Bảo Method Này Chỉ Thực Thi 1 Lần Dù Dc Gọi Nhiều Nơi
  */
  @Bean
  public Service service() {
    return new Service("DefaultConfig");
  }
}

/*
@RestController = @Controller + @ResponseBody
@Controller: Đánh Dấu Class Xử Lý HTTP Request
@ResponseBody: Tự Động Serialize Return Value Thành JSON (Dùng Jackson)
ResponseEntity<T>: Cho Phép Kiểm Soát Đầy Đủ Response - HTTP Status Code + Response Headers + Response Body
@Valid: Kích Hoạt Bean Validation Trên @RequestBody → Ném MethodArgumentNotValidException Nếu Vi Phạm Constraint
*/
@RestController
@RequestMapping("/items")
class ItemController {
  // Record: Class Bất Biến (Immutable) + Tự Generate Constructor/Getter/Equals/HashCode
  record ItemRequest(@NotBlank String name, @Positive double price) {}
  record ItemResponse(Long id, String name, double price) {}
  @GetMapping("/{id}") // GET /items/{id}
  public ResponseEntity<ItemResponse> getItem(@PathVariable Long id) {
    return ResponseEntity.ok(new ItemResponse(id, "Widget", 9.99));
  }
  /*
  @Valid: Validate ItemRequest Theo Annotation Constraint: @NotBlank + @Positive Trước Khi Vào Method
  Ko Có @Valid → @NotBlank + @Positive Ko Dc Kiểm Tra
  @RequestBody: Deserialize JSON Request Body → ItemRequest Obj
  */
  @PostMapping // POST /items
  public ResponseEntity<ItemResponse> createItem(@Valid @RequestBody ItemRequest req) {
    var resp = new ItemResponse(1L, req.name(), req.price());
    return ResponseEntity.status(201).body(resp);
  }
  @GetMapping // GET /items
  public ResponseEntity<?> searchItems(
    @RequestParam(defaultValue = "") String query,
    @RequestParam(defaultValue = "0") int page
  ) {
    return ResponseEntity.ok(List.of());
  }
}

/*
Dependency Injection & Loose Coupling:
- Giảm Phụ Thuộc Trực Tiếp Từ Service Class Vào Implementation Class
- Service Chỉ Phụ Thuộc Vào PaymentGateway
- Thay Đổi Default Bean Qua @Primary Ko Cần Sửa Source Code Của Service

Cơ Chế Phân Giải Ambiguity Khi Dùng Interface:
- Single Interface Injection: Auto Resolve @Primary Bean
- Explicit Qualifier Binding: Override Default Bean With @Qualifier("name")
- Strategy Pattern Injection: Inject Map<String, Implementation> For Dynamic Selection
*/
interface PaymentGateway {
  void process(double amount);
}
@Component
@Primary
class VnPayPayment implements PaymentGateway {
  @Override
  public void process(double amount) {
    System.out.println("VNPay:" + amount);
  }
}
@Component
@Qualifier("momo")
class MomoPayment implements PaymentGateway {
  @Override
  public void process(double amount) {
    System.out.println("Momo:" + amount);
  }
}
// Single Interface Injection & Loose Coupling
@Service
class CheckoutService {
  private final PaymentGateway paymentGateway;
  public CheckoutService(PaymentGateway paymentGateway) {
    this.paymentGateway = paymentGateway; // Inject @Primary Bean (VnPayPayment)
  }
  public void checkout(double amount) {
    paymentGateway.process(amount);
  }
}
// Explicit Qualifier Binding
@Service
class MomoSpecialService {
  private final PaymentGateway paymentGateway;
  public MomoSpecialService(@Qualifier("momo") PaymentGateway paymentGateway) {
    this.paymentGateway = paymentGateway; // Inject @Qualifier Bean (MomoPayment)
  }
  public void processMomo(double amount) {
    paymentGateway.process(amount);
  }
}
// Dynamic Strategy Pattern Injection
@Service
class DynamicCheckoutService {
  private final Map<String, PaymentGateway> paymentGateways;
  public DynamicCheckoutService(Map<String, PaymentGateway> paymentGateways) {
    this.paymentGateways = paymentGateways;
  }
  public void checkout(String provider, double amount) {
    PaymentGateway gateway = paymentGateways.get(provider);
    if (gateway != null) {
      gateway.process(amount);
    }
  }
}
/*
@Scope("Singleton") - Default
- Chỉ Tạo 1 Instance Duy Nhất Cho Toàn Bộ ApplicationContext
- Mọi Nơi Inject Cùng 1 Đối Tượng
- Phù Hợp: Service, Repository, Controller
@Scope("Prototype")
- Tạo Instance Mới Mỗi Khi Có Yêu Cầu Inject
- Ko Dc Spring Quản Lý Sau Khi Tạo Ra - Ko Gọi Destroy
- Phù Hợp: Bean Có Trạng Thái (Stateful) & Cần Tách Biệt Giữa Các Lần Dùng
@Scope("Request")
- 1 Instance Duy Nhất Trong Phạm Vi 1 HTTP Request
- Chỉ Dùng Dc Trong Web Application
@Scope("Session")
- 1 Instance Duy Nhất Trong Phạm Vi 1 HTTP Session
- Chỉ Dùng Dc Trong Web Application
*/

/*
JpaRepository<T, ID>: Interface Cung Cấp Sẵn:
- CRUD: save(), findById(), findAll(), delete()...
- Paging: findAll(Pageable) → Page<T>
- Sorting: findAll(Sort)
Derived Query Methods: Spring Tự Sinh Câu SQL Từ Tên Method
@Query: Custom JPQL/Native SQL Khi Tên Method Quá Phức Tạp
@Transactional: Bọc Toàn Bộ Method Trong 1 Database Transaction
*/
@Entity
@Table(name = "products") // Map Class Này Với Bảng "products" Trong DB
class Product {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-Increment ID Từ DB
  private Long id;
  @Column(nullable = false)
  private String name;
  // FetchType.LAZY: Chỉ Load Category Khi Thực Sự Gọi product.getCategory() → Tránh N+1 Problem: Ko Load Dữ Liệu Thừa Khi Chỉ Cần Thông Tin Product
  @ManyToOne(fetch = FetchType.LAZY)
  private Category category;
}
@Entity
class Category {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String name;
}
interface ProductRepository extends JpaRepository<Product, Long> {
  /*
  Derived Query: Spring Đọc Tên Method & Tự Sinh JPQL
  → Select p From Product p Where Lower(p.name) Like Lower('%Name%')
  */
  List<Product> findByNameContainingIgnoreCase(String name);
  /*
  → Select p From Product p Where p.price < Price (Có Phân Trang)
  Pageable: Chứa Thông Tin Trang (Số Trang, Kích Thước Trang, Sắp Xếp)
  */
  Page<Product> findByPriceLessThan(double price, Pageable pageable);
  /*
  @Query: JPQL Tùy Chỉnh Khi Logic Phức Tạp Hơn Derived Query Có Thể Diễn Đạt
  :min & :max: Named Parameters + Map Với @Param("min") & @Param("max")
  */
  @Query("SELECT p FROM Product p WHERE p.price BETWEEN :min AND :max")
  List<Product> findInPriceRange(@Param("min") double min, @Param("max") double max);
}
@Service
@Transactional // Mặc Định: Tất Cả Method Trong Class Này Đều Chạy Trong Transaction
class ProductService {
  private final ProductRepository repo;
  public ProductService(ProductRepository repo) {
    this.repo = repo;
  }
  /*
  readOnly = True: Gợi Ý Cho Transaction Manager Rằng Transaction Này Chỉ Đọc
  - Bỏ Dirty Checking (Ko Theo Dõi Thay Đổi Entity)
  - Bỏ Flush (Ko Đồng Bộ Xuống DB Trước Khi Query)
  - Tăng Performance Đáng Kể Cho Các Thao Tác Đọc
  */
  @Transactional(readOnly = true)
  public Page<Product> findCheap(double maxPrice, int page) {
    // PageRequest.of(page, 20): Lấy Trang Thứ 'page' + Mỗi Trang 20 Item
    return repo.findByPriceLessThan(maxPrice, PageRequest.of(page, 20));
  }
}

/*
SecurityFilterChain: Cấu Hình Chuỗi Filter Bảo Mật
OncePerRequestFilter: Filter Đảm Bảo Chỉ Chạy 1 Lần Mỗi Request
@PreAuthorize: Bảo Mật Cấp Method — Kiểm Tra Quyền Trước Khi Vào Method
SessionCreationPolicy.STATELESS: Ko Tạo HTTP Session → Phù Hợp Cho REST API + JWT
*/
class JwtFilter extends OncePerRequestFilter {
  @Override
  protected void doFilterInternal(
    HttpServletRequest req,
    HttpServletResponse res,
    FilterChain chain
  ) throws java.io.IOException, jakarta.servlet.ServletException {
    String header = req.getHeader("Authorization");
    if (header != null && header.startsWith("Bearer ")) {
      String token = header.substring(7);
    }
    chain.doFilter(req, res); // Tiếp Tục Đến Filter/Controller Tiếp Theo
  }
}
@Configuration
class SecurityConfig {
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
      /*
      Tắt CSRF Protection: REST API Dùng Token - Stateless - Ko Cần CSRF
      CSRF Chỉ Cần Với Session-Based Auth
      */
      .csrf(c -> c.disable())
      // STATELESS: Spring Security Ko Tạo/Dùng HTTP Session → Mỗi Request Phải Tự Xác Thực Qua Token
      .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      // Phân Quyền Từng Endpoint:
      .authorizeHttpRequests(a -> a
        .requestMatchers("/auth/**", "/public/**").permitAll() // Ai Cũng Vào Dc
        .requestMatchers("/admin/**").hasRole("Admin")         // Chỉ Admin
        .anyRequest().authenticated()                          // Còn Lại Phải Đăng Nhập
      )
      // Thêm JwtFilter Vào Trc UsernamePasswordAuthenticationFilter - Built-in Filter
      .addFilterBefore(new JwtFilter(), UsernamePasswordAuthenticationFilter.class)
      .build();
  }
}

/*
@ConfigurationProperties: Bind Toàn Bộ 1 Nhóm Property Từ application.yml Vào 1 Class/Record
- Type-Safe: Compile-Time Check + Ko Sợ Typo Key
- Thay Thế @Value Từng Field Lẻ
@Profile: Bean Chỉ Dc Tạo Khi Profile Tương Ứng Đang Active
- Tách Biệt Config Cho Từng Môi Trường (Dev/Prod/Test)
*/
@ConfigurationProperties(prefix = "app.database")
record DatabaseProps(String url, String username, int poolSize) {}
@Configuration
class ProfileConfig {
  // --spring.profiles.active=dev
  @Bean
  @Profile("dev")
  public DatabaseProps devDatabase() {
    return new DatabaseProps("jdbc:h2:mem:devDb", "dev", 5);
  }
  // --spring.profiles.active=prod
  @Bean
  @Profile("prod")
  public DatabaseProps prodDatabase() {
    return new DatabaseProps("jdbc:postgresql://prod:5432/db", "prod", 20);
  }
}