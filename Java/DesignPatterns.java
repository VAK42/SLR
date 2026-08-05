import java.util.*;
import java.lang.reflect.*;

/*
* Creational Patterns:
Builder: Tách Complex Obj Initialization Process (Many Fields + Optional Parameters) Ra Khỏi Class Đại Diện Của Nó → Tránh Telescoping Constructor (Too Many Constructors Với Parameters Khác Nhau)
- Required Fields Dc Truyền Qua Builder Constructor
- Optional Fields Dc Truyền Qua Qua Builder Methods
- Method build() Sẽ Validate Values & Tạo Ra Obj
- Thường Đi Kèm Immutable Obj: Fields Dc Khai Báo final + Ko Cho Mod State Sau Khi Obj Dc Khởi Tạo + Ensure Obj An Toàn Khi Shared Between Threads
- Fluent API: Allow Chaining Many Methods Consecutively + Tăng Readability Khi Initialize Obj

Singleton: Ensure Class Chỉ Có Duy Nhất 1 Instance Trong JVM → Tránh Tạo Many Objs Cùng Quản Lý 1 Resource Chung + Save Memory + Ensure Global Access Point Đến 1 Obj Duy Nhất
- Enum Singleton:
  + Cách Implement Singleton Đơn Giản & An Toàn Nhất Trong Java
  + JVM Tự Ensure Enum Constant Chỉ Có 1 Instance Duy Nhất
  + Thread-Safe Vì Enum Initialization Dc JVM Kiểm Soát
  + Chống Reflection Attack: Reflection Có Thể Phá Private Constructor Của Normal Class Nma Java Ko Allow Reflection Tạo Instance Ms Của Enum
  + Chống Serialization Attack: Deserialize Normal Obj Có Thể Tạo Instance Ms + Enum Có Special Serialization Mechanism + JVM Luôn Return Lại Enum Constant Ban Đầu

- Double-Checked Locking:
  + Lazy Initialization: Chỉ Tạo Instance Khi Method getInstance() Dc Call Lần Đầu + Ko Tốn Memory Khi Application Chưa Cần Sử Dụng
  + synchronized Block: Ensure Chỉ 1 Thread Dc Quyền Initialize Instance + Ngăn Multiple Threads Tạo Ra Many Singleton Objs
  + Double Check: Check Instance Trước Khi Lock + Check Lại Instance Sau Khi Có Lock + Avoid Lock Ở Những Lần Gọi Sau Khi Instance Đã Tồn Tại
  + volatile: Ngăn Instruction Reordering + Ensure Visibility Between Threads

- Holder Singleton (Initialization-On-Demand Holder):
  + Tận Dụng Cơ Chế Class Loading Của JVM Để Ensure Thread-Safe
  + Inner Holder Class: Chỉ Dc JVM Load Khi getInstance() Dc Call + Static Instance Chỉ Dc Tạo Khi Holder Class Dc Initialize
  + Ưu Điểm: Lazy Loading + Thread-Safe + Ko Cần synchronized + Ko Cần volatile
  + Lý Do An Toàn: JVM Ensure Class Initialization Chỉ Xảy Ra 1 Lần + Thread-Safe

Factory Method: Serialize Obj Initialization Bằng Cách Tách Logic Tạo Obj Ra Khỏi Client
- Giao Cho Subclass Decide Concrete Class Nào Sẽ Dc Instantiate
- Client Chỉ Làm Việc Với Interface|Abstract Class + Ko Cần Bt Implementation Cụ Thể Hay Cách Obj Dc Tạo Ra
- Giảm Coupling Giữa Caller & Concrete Classes + Dễ Mở Rộng Thêm Loại Obj Ms

* Behavioral Patterns:
Strategy: Define 1 Nhóm Algorithm Có Cùng Purpose Nhưng Different Implementation
- Encapsulate Every Algorithm Thành 1 Individual Class Thông Qua Common Interface
- Context Chỉ Làm Việc Vs Strategy Interface: Ko Bt Logic Bên Trong Của Algorithm + Ko Phụ Thuộc Concrete Implementation
- Allow Swap Algorithm Linh Hoạt Trong Runtime
- Purpose: Eliminate Conditional Statements → Tách Algorithm Khỏi Business Logic + Dễ Thêm Algorithm Ms Mà Ko Mod Existing Code
- Sử Dụng Composition Thay Vì Inheritance + Behavior Có Thể Thay Đổi Dynamic Lúc Runtime

Observer: Define Quan Hệ One-To-Many Between Subject & Nhiều Observers
- Khi Subject Thay Đổi State Hoặc Có Event Xảy Ra → Tự Động Notify Tất Cả Observers Đã Subscribe
- Subject: Quản Lý Danh Sách Observer + Allow Register|Remove Observer + Trigger Notification Khi Có Change
- Observer: Reg Nhận Event + Xử Lý Logic Khi Nhận Notification
- Purpose: Giảm Coupling Between Publisher & Subscriber + Publisher Ko Cần Bt Observer Là Ai + Có Thể Thêm Observer Ms Mà Ko Mod Publisher
- EventBus: Đóng Vai Trò Broker Trung Gian Between Publisher & Subscriber + Publisher Chỉ Publish Event + Subscriber Tự Subscribe Event Mà Nó Quan Tâm
- Decouple Hoàn Toàn Các Module + Dễ Scale Khi Thêm Feature Ms + Fault Isolation Khi 1 Subscriber Bị Lỗi

Template Method: Define Skeleton Của 1 Algorithm Trong Base Class
- Base Class Rules: Quy Định Step Order + Main Execution Flow
- Template Method: Thường Declare final To Prevent Subclasses Mod Alg Flow
- Abstract Methods: Represent Changeable Steps + Subclasses Override Specific Implementation
- Purpose: Reuse Common Workflow + Avoid Code Duplication + Centralize Process Control
- Vs Strategy: Template Method (Dùng Inheritance + Mod 1 Phần Alg + Flow Defined In Parent Class) vs Strategy (Dùng Composition + Mod Toàn Bộ Alg + Swap Runtime)

Chain Of Responsibility: Chuyển Request Qua 1 Chuỗi Các Handlers Để Xử Lý Lần Lượt
- Handler: Tự Handle Request Hoặc Forward Request Cho Next Handler (Hold Reference Đến Handler Kế Tiếp)
- Request Flow: Request Traverses Chain Until Accept & Handle Or Reaches End Of Chain
- Purpose: Tách Processing Steps Thành Independent Components + Avoid Monolithic Class + Dễ Add/Remove Handler Mà Ko Ảnh Hưởng Code Cũ
- Application: Authentication Pipeline + Authorization Checking + Request Validation + Middleware Processing

* Structural Patterns:
Proxy: Tạo 1 Obj Đại Diện (Placeholder) Đứng Trước Real Obj
- Flow: Client → Proxy → Real Obj (Client Call Proxy Thay Vì Call Direct Target)
- Proxy Role: Control Access Permission | Bổ Sung Logic Before/After Calling Target Obj
- Purpose: Protect Real Obj Khỏi Access Directly + Control When Obj Is Used + Add New Behavior Without Mod Original Class
- Common Proxy Logics: Logging (Log Method Call/Input/Output) + Security (Permission Check) + Transaction (Begin/Commit/Rollback) + Cache (Avoid Heavy Resource Calls) + Lazy Initialization (Create Real Obj Only When Needed)

JDK Dynamic Proxy: Java Tạo Proxy Obj Tự Động Dựa Trên Interface Using Reflection API
- Condition: Target Class & Proxy Obj Must Implement Same Interface
- Mechanism: Call Proxy Method → Forward Call To InvocationHandler (Add Pre-Logic + Call Real Obj Method + Add Post-Logic) → Return Result
- InvocationHandler: Intercept All Method Calls + Receive Info: Proxy Obj, Called Method, Arguments
- Pros: Built-in In JDK (No External Lib Needed) + Fit Interface-Based Design
- Cons: Only Proxies Interfaces (Cannot Proxy Concrete Class Directly Or Non-Interface Methods)

CGLIB Proxy: Tạo Proxy Bằng Cách Generate 1 Subclass Kế Thừa Target Class
- Mechanism: Inheritance-Based → Override Parent Class Methods To Inject Additional Logic (Client → CGLIB Proxy Subclass → Target Class)
- Pros: No Interface Required + Can Proxy Concrete Classes Directly
- Cons: Cannot Proxy final Class (Cannot Subclass final Class) + Cannot Override final Method
- Requirement: Target Class Must Allow Inheritance + Intercepted Methods Must Be Overridable

Spring AOP Proxy: Use Proxy Pattern To Implement Aspect-Oriented Programming (AOP)
- Purpose: Decouple Cross-Cutting Concerns (Transaction, Security, Logging, Caching, Monitoring) Khỏi Business Logic
- Mechanism: Create Proxy Wrapping Original Bean & Inject Proxy → Method Call Goes Through Proxy → Run Additional Logic → Delegate To Real Bean (Original Class Unmodified)
- Selection Strategy: Bean Has Interface → Use JDK Dynamic Proxy (Def) | Bean Has No Interface → Use CGLIB Proxy
- Benefits: Business Class Clean From Boilerplate Concerns + Reduce Code Duplication + Increase App Modularity

Adapter: Chuyển Đổi Interface Của 1 Class Thành Interface Khác Mà Client Expect
- Purpose: Help Classes With Incompatible Interfaces Work Together + Solve Unmodifiable Existing Class Constraints
- Mechanism: Call Adapter Via Target Interface → Adapter Translate Request To Existing Class Interface → Existing Class Execute Original Logic
- Role: Translator Between 2 Different Interfaces + Intermediate Layer Connecting Incompatible Components
- Types: Obj Adapter (Composition + Hold Ref To Existing Obj - Preferred In Java) vs Class Adapter (Inheritance - Rarely Used Due To No Multiple Inheritance)
- Preference: Priority Composition Over Inheritance To Reduce Coupling & Easily Change Implementation

Decorator: Dynamically Wrap Obj In Decorator Class To Add New Behavior At Runtime
- Mechanism: Decorator Implement Same Interface As Target (Client Can't Tell Decorator From Original) + Hold Ref To Inner Obj → Method Call Run Custom Logic + Delegate To Wrapped Obj
- Purpose: Add Features Flexibly + Avoid Subclass Explosion For Feature Combinations + Combine Behaviors At Runtime
- Features: Use Composition + Support Chaining Multiple Decorators (Decorator A → Decorator B → Original Obj) + Each Layer Add Individual Behavior
- Vs Proxy: Proxy Focus On Controlling Access (Security, Tx, Cache) vs Decorator Focus On Expanding Behaviors & Features
*/
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

interface Notification {
  void send(String message);
}
class EmailNotification implements Notification {
  public void send(String message) { System.out.println("Sending Email: " + message); }
}
class SmsNotification implements Notification {
  public void send(String message) { System.out.println("Sending SMS: " + message); }
}
abstract class NotificationFactory {
  public abstract Notification createNotification();
  public void notifyUser(String msg) {
    Notification n = createNotification();
    n.send(msg);
  }
}
class EmailFactory extends NotificationFactory {
  public Notification createNotification() { return new EmailNotification(); }
}
class SmsFactory extends NotificationFactory {
  public Notification createNotification() { return new SmsNotification(); }
}

enum EnumSingleton {
  INSTANCE;
  public void doSomething() { System.out.println("EnumSingleton Work"); }
}
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
final class HolderSingleton {
  private HolderSingleton() {}
  private static class Holder {
    private static final HolderSingleton INSTANCE = new HolderSingleton();
  }
  public static HolderSingleton getInstance() {
    return Holder.INSTANCE;
  }
}

interface ModernPrinter {
  void print(String text);
}
class LegacyPrinter {
  public void printOldStyle(String text) { System.out.println("Legacy Print: " + text); }
}
class PrinterAdapter implements ModernPrinter {
  private final LegacyPrinter legacyPrinter;
  public PrinterAdapter(LegacyPrinter legacyPrinter) { this.legacyPrinter = legacyPrinter; }
  @Override
  public void print(String text) { legacyPrinter.printOldStyle(text); }
}

interface Coffee {
  double getCost();
  String getDescription();
}
class SimpleCoffee implements Coffee {
  public double getCost() { return 2.0; }
  public String getDescription() { return "Simple Coffee"; }
}
abstract class CoffeeDecorator implements Coffee {
  protected final Coffee decoratedCoffee;
  public CoffeeDecorator(Coffee coffee) { this.decoratedCoffee = coffee; }
  public double getCost() { return decoratedCoffee.getCost(); }
  public String getDescription() { return decoratedCoffee.getDescription(); }
}
class MilkDecorator extends CoffeeDecorator {
  public MilkDecorator(Coffee coffee) { super(coffee); }
  @Override public double getCost() { return super.getCost() + 0.5; }
  @Override public String getDescription() { return super.getDescription() + " + Milk"; }
}

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

@FunctionalInterface
interface Observer<T> {
  void onEvent(String eventType, T data);
}
class EventBus<T> {
  private final Map<String, List<Observer<T>>> listeners = new HashMap<>();
  public void subscribe(String eventType, Observer<T> observer) {
    listeners.computeIfAbsent(eventType, k → new ArrayList<>()).add(observer);
  }
  public void publish(String eventType, T data) {
    listeners.getOrDefault(eventType, List.of()).forEach(obs → obs.onEvent(eventType, data));
  }
}

abstract class DataMiner {
  public final void mineData(String path) {
    openFile(path);
    extractData();
    closeFile();
  }
  protected abstract void openFile(String path);
  protected abstract void extractData();
  private void closeFile() { System.out.println("Closed File Resource"); }
}
class PdfDataMiner extends DataMiner {
  protected void openFile(String path) { System.out.println("Opening PDF: " + path); }
  protected void extractData() { System.out.println("Extracting PDF Text"); }
}

abstract class Handler {
  private Handler next;
  public Handler linkWith(Handler next) { this.next = next; return next; }
  public abstract boolean handle(String request);
  protected boolean handleNext(String request) {
    if (next == null) return true;
    return next.handle(request);
  }
}
class AuthHandler extends Handler {
  public boolean handle(String request) {
    if (!request.contains("Auth")) {
      System.out.println("AuthHandler: Failed");
      return false;
    }
    System.out.println("AuthHandler: Passed");
    return handleNext(request);
  }
}
class RoleHandler extends Handler {
  public boolean handle(String request) {
    System.out.println("RoleHandler: Passed");
    return handleNext(request);
  }
}

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
    NotificationFactory factory = new EmailFactory();
    factory.notifyUser("Hola!");
    EnumSingleton.INSTANCE.doSomething();
    System.out.println("DCL Same: " + (DclSingleton.getInstance() == DclSingleton.getInstance()));
    System.out.println("HLD Same: " + (HolderSingleton.getInstance() == HolderSingleton.getInstance()));
    ModernPrinter printer = new PrinterAdapter(new LegacyPrinter());
    printer.print("Adapter Modern Message");
    Coffee coffee = new MilkDecorator(new SimpleCoffee());
    System.out.println("Decorator Coffee: " + coffee.getDescription() + " | CST: $" + coffee.getCost());
    ShoppingCart cart = new ShoppingCart();
    cart.setPaymentStrategy(amt -> System.out.println("Paid With VISA: " + amt));
    cart.checkout(100);
    cart.setPaymentStrategy(amt -> System.out.println("Paid With Momo: " + amt));
    cart.checkout(200);
    EventBus<String> bus = new EventBus<>();
    bus.subscribe("order", (type, data) -> System.out.println("Email Service: NTF" + data));
    bus.subscribe("order", (type, data) -> System.out.println("Audit Service: Logged " + data));
    bus.publish("order", "Order Confirmed");
    DataMiner miner = new PdfDataMiner();
    miner.mineData("doc.pdf");
    Handler chain = new AuthHandler();
    chain.linkWith(new RoleHandler());
    chain.handle("RequestWithAuth");
    DatabaseService realDb = new DatabaseServiceImpl();
    DatabaseService proxyDb = (DatabaseService) Proxy.newProxyInstance(
      DatabaseService.class.getClassLoader(),
      new Class<?>[]{DatabaseService.class},
      new TransactionHandler(realDb)
    );
    proxyDb.query("SELECT * FROM users");
    UserPaymentService realPayment = new UserPaymentService();
    UserPaymentService proxyPayment = new CglibProxySimulator(realPayment);
    proxyPayment.pay(500);
  }
}