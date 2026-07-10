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
/*
@SpringBootApplication = @Configuration + @EnableAutoConfiguration + @ComponentScan
@Configuration: Đánh Dấu Class Này Chứa Các @Bean -> Spring Đọc
@EnableAutoConfiguration: Tự Đoán & Cấu Hình Dựa Trên Thư Viện Jar Có Trong Project
@ComponentScan: Quét Toàn Bộ Package -> Tìm Class Có Annotation: @Component, @Service, @Repository, @Controller -> Đăng Ký Vào IoC Container (ApplicationContext)
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
    ApplicationContext: Nơi Chứa Toàn Bộ Bean Đã Được Khởi Tạo
    getBeanDefinitionCount() Trả Về Số Lượng Bean Đang Được Quản Lý
    */
    System.out.println(ctx.getBeanDefinitionCount());
  }
}
/*
@Configuration — CGLIB Proxy Đảm Bảo Singleton
@Configuration -> Spring Dùng CGLIB Tạo Subclass Proxy Cho Class Này -> Dù Gọi service() Nhiều Lần -> Luôn Trả Về Cùng 1 Instance (Singleton)
Nếu Dùng @Component Thay Vì @Configuration → Không Có Proxy → Tạo Instance Mới Mỗi Lần
*/
@Configuration
class Config {
  /*
  @Bean: Khai Báo Method Này Trả Về 1 Đối Tượng Sẽ Được Spring Quản Lý
  Method Name ("service"): Tên Bean Mặc Định Trong Container
  @Bean("customService"): Tên Bean Custom Đc Thêm Vào Container
  Spring Đảm Bảo Method Này Chỉ Thực Thi 1 Lần Dù Được Gọi Nhiều Nơi
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
@Valid: Kích Hoạt Bean Validation Trên @RequestBody -> Ném MethodArgumentNotValidException Nếu Vi Phạm Constraint
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
  Ko Có @Valid -> @NotBlank + @Positive Ko Đc Kiểm Tra
  @RequestBody: Deserialize JSON Request Body → ItemRequest Object
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
Dependency Injection:
- Tránh Lặp Code Khởi Tạo
- Thay Vì Tự New Class Khắp Nơi -> Spring Tự Khởi Tạo & Bơm Vào
- Khi Cần Sửa Đổi -> Chỉ Cần Sửa Đúng 1 Nơi Duy Nhất
Vấn Đề Ambiguity Khi Lập Trình Với Interface:
- Khi Khai Báo Kiểu Interface PaymentGateway -> Có Nhiều Class Con Cùng Thỏa Mãn
- Spring Sẽ Bối Rối Ko Biết Chọn Class Nào Để Bơm Vào Constructor → Lỗi Crash
@Primary — Hành Vi Mặc Định Toàn Hệ Thống:
- Đặt Làm Cổng Thanh Toán Chính (VD: VNPay Cho 100 Class Dùng Chung)
- Khi Muốn Đổi Cổng Thanh Toán Mặc Định Toàn App -> Chỉ Cần Gỡ @Primary & Gắn Sang Class Khác
@Qualifier — Giải Quyết Các Trường Hợp Ngoại Lệ:
- Chỉ Định Đích Danh Khi Muốn Ghi Đè Lên Cấu Hình Mặc Định Của Hệ Thống
- VD: Mặc Định Dùng VNPay Nhưng Riêng Dịch Vụ Ship COD Thì Ép Dùng Momo
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
@Service
class CheckoutService {
  private final PaymentGateway primaryGateway;
  private final PaymentGateway momoGateway;
  // Spring 4.3+:     Không Cần @Autowired -> Tự Inject Qua Constructor
  // primaryGateway → Không Có @Qualifier → Spring Chọn @Primary (VnPayPayment)
  // momoGateway    → Có @Qualifier("momo") → Spring Chọn Đúng MomoPayment
  public CheckoutService(
    PaymentGateway primaryGateway,
    @Qualifier("momo") PaymentGateway momoGateway
  ) {
    this.primaryGateway = primaryGateway;
    this.momoGateway = momoGateway;
  }
  public void checkout(double amount) {
    primaryGateway.process(amount);
    momoGateway.process(amount);
  }
}
/*
@Scope("Singleton") - Default
- Chỉ Tạo 1 Instance Duy Nhất Cho Toàn Bộ ApplicationContext
- Mọi Nơi Inject Cùng 1 Đối Tượng
- Phù Hợp: Service, Repository, Controller
@Scope("Prototype")
- Tạo Instance Mới Mỗi Khi Có Yêu Cầu Inject
- Ko Được Spring Quản Lý Sau Khi Tạo Ra - Không Gọi Destroy
- Phù Hợp: Bean Có Trạng Thái (Stateful) & Cần Tách Biệt Giữa Các Lần Dùng
@Scope("Request")
- 1 Instance Duy Nhất Trong Phạm Vi 1 HTTP Request
- Chỉ Dùng Được Trong Web Application
@Scope("Session")
- 1 Instance Duy Nhất Trong Phạm Vi 1 HTTP Session
- Chỉ Dùng Được Trong Web Application
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
  // FetchType.LAZY: Chỉ Load Category Khi Thực Sự Gọi product.getCategory() -> Tránh N+1 Problem: Ko Load Dữ Liệu Thừa Khi Chỉ Cần Thông Tin Product
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
  findBy + Name + Containing + IgnoreCase
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
      // STATELESS: Spring Security Ko Tạo/Dùng HTTP Session -> Mỗi Request Phải Tự Xác Thực Qua Token
      .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      // Phân Quyền Từng Endpoint:
      .authorizeHttpRequests(a -> a
        .requestMatchers("/auth/**", "/public/**").permitAll() // Ai Cũng Vào Đc
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
@Profile: Bean Chỉ Đc Tạo Khi Profile Tương Ứng Đang Active
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