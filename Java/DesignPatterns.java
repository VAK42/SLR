import java.util.*;
import java.lang.reflect.*;

/*
Creational Patterns:
- Builder: Tách Quá Trình Khởi Tạo Đối Tượng Phức Tạp (Nhiều Fields + Tham Số Tùy Chọn) Khỏi Đại Diện Của Nó -> Tránh Telescoping Constructor (Write Quá Nhiều Constructor Chồng Chéo Nhau Cho Các Tham Số Tùy Chọn)
- Singleton: Đảm Bảo Class Chỉ Có Duy Nhất 1 Instance Trong JVM
  + Enum Singleton: Đơn Giản Nhất + Chống Phá Khóa Bằng Reflection & Serialization
  + Double-Checked Locking (DCL): Dùng volatile Ngăn Reordering (Khi Initialize Object Tránh Return Instance Chưa Tạo Xong) + synchronized Block
  + Holder Singleton (Lazy Initialization-On-Demand): Thread-Safe & Lazy Load Ko Cần Lock Bằng Cách Tận Dụng Cơ Chế Load Class Của JVM

Behavioral Patterns:
- Strategy: Định Nghĩa Bộ Thuật Toán + Đóng Gói Riêng Biệt + Cho Phép Thay Thế (Swap) Thuật Toán Linh Hoạt Lúc Runtime
- Observer: Định Nghĩa Quan Hệ 1-Nhiều Giữa Các Object - Khi Subject Thay Đổi Sẽ Tự Động Thông Báo Cho Tất Cả Observers Đã Đăng Ký (Decouple)

Structural Patterns:
- Proxy: Đại Diện (Placeholder) Kiểm Soát Quyền Truy Cập Đến Đối Tượng Gốc (Thêm Log, Transaction, Security...).
  + JDK Dynamic Proxy: Tạo Proxy Dựa Trên Interface - Sử Dụng Java Reflection
  + CGLIB Proxy: Tạo Proxy Bằng Cách Tạo Subclass Kế Thừa Target Class - Ko Thể Dùng Với Class/Method final
  + Spring AOP: Default Dùng JDK Proxy Nếu Class Có Interface - Ngc Lại Dùng CGLIB
*/

// Builder Pattern: Thay Vì Write Nhiều Contructors Vs Nhiều Args Khác Nhau -> Write 1 Static Class Có 1 Method (Constructor) Chứa Các Compulsory Fields + Nhiều Methods Chứa Các Optional Fields
class HttpRequest {
  private final String method;
  private final String url;
  private final Map<String, String> headers;
  private final String body;
  private HttpRequest(Builder builder) {
    this.method = builder.method;
    this.url = builder.url;
    this.headers = Collections.unmodifiableMap(new HashMap<>(builder.headers));
    this.body = builder.body;
  }
  public String getMethod() { return method; }
  public String getUrl() { return url; }
  public Map<String, String> getHeaders() { return headers; }
  public String getBody() { return body; }
  public static class Builder {
    private final String method;
    private final String url;
    private final Map<String, String> headers = new HashMap<>();
    private String body = "";
    public Builder(String method, String url) {
      if (method == null || method.isBlank()) throw new IllegalArgumentException("Method Required");
      if (url == null || url.isBlank()) throw new IllegalArgumentException("URL Required");
      this.method = method;
      this.url = url;
    }
    public Builder header(String key, String value) {
      this.headers.put(key, value);
      return this;
    }
    public Builder body(String body) {
      this.body = body;
      return this;
    }
    public HttpRequest build() {
      return new HttpRequest(this);
    }
  }
}

// Singleton Pattern: Đảm Bảo Chỉ Có Duy Nhất 1 Instance Của Class Trong Suốt App Lifecycle
/*
Enum Singleton: Đơn Giản Nhất
- Chống Lại Reflection Attack (Tấn Công Phá Khóa Private Constructor Bằng Reflection API) - Vì Java Cấm Dùng Reflection Để Tạo Instance Của Enum
- Chống Lại Serialization Attack (Khi Write & Read Object Ra File Rồi Read Lại Sẽ Sinh Ra Instance Mới) - Vì Java Có Cơ Chế Serialization Đặc Biệt Dành Riêng Cho Enum Để Luôn Return Đúng Constant Ban Đầu
*/
enum EnumSingleton {
  INSTANCE;
  public void doSomething() { System.out.println("EnumSingleton Work"); }
}

/*
Double-Checked Locking (DCL) Singleton: Thread-Safe + Lazy Init + High Performance
- synchronized Block: Chỉ Lock Khi instance Chưa Tạo (Tránh Bottleneck Hơn So Với Method-Level synchronized)
- volatile:
  + Ngăn Instruction Reordering: CPU Có Thể Đảo Lệnh Khởi Tạo (Cấp Vùng Nhớ -> Gán Reference -> Chạy Constructor) Khiến Thread Khác Lấy Phải Đối Tượng Rỗng -> volatile Ép Chạy Constructor Xong Ms Đc Gán Reference
  + Đảm Bảo Visibility: Ép Update Value instance Ngay Lập Tức Từ CPU Cache Về RAM Để Các Threads Khác Thấy Luôn
*/
final class DclSingleton {
  private static volatile DclSingleton instance;
  private DclSingleton() {}
  public static DclSingleton getInstance() {
    if (instance == null) {
      synchronized (DclSingleton.class) {
        if (instance == null) {
          instance = new DclSingleton();
        }
      }
    }
    return instance;
  }
}

/*
Initialization-On-Demand Holder Singleton: Thread-Safe + Lazy Init + Lock-Free
- Cơ Chế: Class Con Holder Chỉ Đc JVM Load Vào Bộ Nhớ Khi Hàm getInstance() Đc Gọi Lần Đầu (Lazy)
- Lock-Free: Tận Dụng Cơ Chế Load Class Của JVM Luôn Đảm Bảo Thread-Safe Tự Nhiên Ở Tầng Hệ Thống Mà Ko Cần Dùng Khóa synchronized
*/
final class HolderSingleton {
  private HolderSingleton() {}
  private static class Holder {
    private static final HolderSingleton INSTANCE = new HolderSingleton();
  }
  public static HolderSingleton getInstance() {
    return Holder.INSTANCE;
  }
}

// Strategy Pattern
@FunctionalInterface
interface PaymentStrategy {
  void pay(int amount);
}
class ShoppingCart {
  private PaymentStrategy strategy;
  public void setPaymentStrategy(PaymentStrategy strategy) { this.strategy = strategy; }
  public void checkout(int amount) {
    strategy.pay(amount);
  }
}

/*
Observer Pattern (Generic Event Bus): Quan Hệ 1-Many Between Objects
- Mục Tiêu: Khi Có Event Phát Ra -> Tự Động Notify Cho Tất Cả Subscribers (Decouple Hoàn Toàn Publisher & Subscriber)

- EventBus: Làm Broker Trung Gian Điều Phối
Thay Vì Call Directly Theo Đường Thẳng (VD: OrderService -> MailService -> InventoryService -> AuditService) -> Kết Dính Chặt Chẽ -> Thì Nay Tất Cả Làm Việc Qua EventBus
Khi MailService Hỏng Hoặc Thêm SmsService Thì Ko Cần Phải Đụng Vào Code Của OrderService (Fault Isolation & Dễ Mở Rộng)
*/
@FunctionalInterface
interface Observer<T> {
  void onEvent(String eventType, T data);
}
class EventBus<T> {
  private final Map<String, List<Observer<T>>> listeners = new HashMap<>();
  public void subscribe(String eventType, Observer<T> observer) {
    listeners.computeIfAbsent(eventType, k -> new ArrayList<>()).add(observer);
  }
  public void publish(String eventType, T data) {
    listeners.getOrDefault(eventType, List.of()).forEach(obs -> obs.onEvent(eventType, data));
  }
}

/*
Proxy Pattern: Đại Diện Cho 1 Target Object Để Kiểm Soát Truy Cập Hoặc Bổ Sung Logic (Spring AOP & @Transactional Core)
- Hoạt Động: Guard Đứng Trước Target - Thay Vì Gọi Trực Tiếp Target -> Client Gọi Qua Proxy -> Proxy Sẽ Tự Động Xử Lý Thêm Các Logic Trước & Sau (VD: Begin/Commit Transaction, Log, Security, Cache) Trc Khi Chuyển Tiếp Đến Target
- Phân Loại:
  + JDK Dynamic Proxy: Tạo Proxy Dựa Trên Interface Của Target Class - Sử Dụng Java Reflection - Cần Interface Để Cả Target + Proxy Cùng Chung Kiểu - Do Proxy Đã Kế Thừa Proxy Class Của JDK -> Ko Thể Kế Thừa Target Class
  + CGLIB Proxy: Tạo Proxy Bằng Cách Tạo Subclass Kế Thừa Target Class - Ko Thể Dùng Cho Class/Method final (Do Ko Thể Kế Thừa/Override) - Trực Tiếp Kế Thừa Target Class - Nhưng Vì Dùng extends/override -> Target Class & Methods Ko Dc final
  + Spring AOP: Default Dùng JDK Dynamic Proxy Nếu Class Có Interface - Ngược Lại Dùng CGLIB
*/
interface DatabaseService {
  void query(String sql);
}
class DatabaseServiceImpl implements DatabaseService {
  public void query(String sql) { System.out.println("Querying: " + sql); }
}
class TransactionHandler implements InvocationHandler {
  private final Object target;
  public TransactionHandler(Object target) { this.target = target; }
  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    System.out.println("JDK Proxy: Begin Tx");
    Object result = method.invoke(target, args);
    System.out.println("JDK Proxy: Commit Tx");
    return result;
  }
}
class UserPaymentService {
  public void pay(int amount) { System.out.println("Processing Payment: " + amount); }
}
class CglibProxySimulator extends UserPaymentService {
  private final UserPaymentService target;
  public CglibProxySimulator(UserPaymentService target) { this.target = target; }
  @Override
  public void pay(int amount) {
    System.out.println("CGLIB Proxy: Check Balance");
    target.pay(amount);
    System.out.println("CGLIB Proxy: Log Transaction");
  }
}

public class DesignPatterns {
  public static void main(String[] args) throws Exception {
    // Builder
    try {
      HttpRequest req = new HttpRequest.Builder("GET", "http://google.com")
        .header("Content-Type", "application/json")
        .body("Data")
        .build();
      System.out.println("Builder URL: " + req.getUrl() + ", Headers: " + req.getHeaders());
      new HttpRequest.Builder("", "").build(); // Sẽ Ném Exception
    } catch (IllegalArgumentException e) {
      System.out.println("Builder Validation: " + e.getMessage());
    }

    // Singleton
    EnumSingleton.INSTANCE.doSomething();
    System.out.println("DCL Same: " + (DclSingleton.getInstance() == DclSingleton.getInstance()));
    System.out.println("Holder Same: " + (HolderSingleton.getInstance() == HolderSingleton.getInstance()));

    // Strategy
    ShoppingCart cart = new ShoppingCart();
    cart.setPaymentStrategy(amt -> System.out.println("Paid With Visa: " + amt));
    cart.checkout(100);
    cart.setPaymentStrategy(amt -> System.out.println("Paid With Momo: " + amt));
    cart.checkout(200);

    // Observer (Event Bus)
    EventBus<String> bus = new EventBus<>();
    bus.subscribe("order", (type, data) -> System.out.println("Email Service: Notification for " + data));
    bus.subscribe("order", (type, data) -> System.out.println("Audit Service: Logged " + data));
    bus.publish("order", "Order Confirmed");

    // Proxy
    // JDK Dynamic Proxy
    DatabaseService realDb = new DatabaseServiceImpl();
    DatabaseService proxyDb = (DatabaseService) Proxy.newProxyInstance(
      DatabaseService.class.getClassLoader(),
      new Class<?>[]{DatabaseService.class},
      new TransactionHandler(realDb)
    );
    proxyDb.query("SELECT * FROM users");

    // CGLIB Proxy Simulation
    UserPaymentService realPayment = new UserPaymentService();
    UserPaymentService proxyPayment = new CglibProxySimulator(realPayment);
    proxyPayment.pay(500);
  }
}