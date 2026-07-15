import java.util.function.*;
import java.util.*;
/*
Interface:
Abstract Method: Phương Thức Khai Báo Ko Có Phần Thân -> Bắt Buộc Subclass Phải Implement
Default Method: Phương Thức Có Phần Thân Mặc Định Giúp Bổ Sung Tính Năng Mới Mà Ko Làm Lỗi Các Class Kế Thừa Trước Đó
Static Method: Phương Thức Tĩnh Thuộc Về Chính Interface Dùng Để Write Utility Methods
*/
interface Repository<T, TId> {
  T findById(TId id);
  void save(T entity);
  void delete(TId id);
  default boolean exists(TId id) {
    return findById(id) != null;
  }
  default void saveIfAbsent(T entity, TId id) {
    if (!exists(id)) save(entity);
  }
  static <T> Repository<T, String> empty() {
    return new Repository<>() {
      public T findById(String id) { return null; }
      public void save(T entity) {}
      public void delete(String id) {}
    };
  }
}

/*
Functional Interface: Interface Chỉ Chứa Duy Nhất 1 Abstract Method
-> Làm Target Type Để Java Compiler Ánh Xạ Biểu Thức Lambda/Method Reference Vào - Vì Lambda Ko Có Tên Method Nên Interface Bắt Buộc Chỉ Có 1 Abstract Method Để Compiler Ánh Xạ Chính Xác
Các Built-In Functional Interfaces Phổ Biến:
+ Predicate: Nhận T -> Trả Về boolean -> Kiểm Tra Điều Kiện
+ Function: Nhận T -> Trả Về R -> Biến Đổi Dữ Liệu
+ Consumer: Nhận T -> Ko Trả Về Giá Trị void -> Thực Thi Hành Động
+ Supplier: Ko Nhận Tham Số -> Trả Về T -> Cung Cấp Dữ Liệu -> Lazy Evaluation
+ BiFunction: Nhận T & U -> Trả Về R -> Hàm 2 Tham Số
+ UnaryOperator: Nhận T -> Trả Về T -> Biến Đổi Dữ Liệu Cùng Kiểu
+ BinaryOperator: Nhận T & T -> Trả Về T -> Phép Toán Gộp Cùng Kiểu

Lambda Expression: (arg) -> { body }
Là Cách Triển Khai Nhanh + Ngắn Gọn Cho Functional Interface Thay Thế Cho Anonymous Class
VD: Nếu Ko Dùng Lambda -> Phải Write Anonymous Class/Concrete Class Để Initialize Object Bọc Lấy Method - Vì Java Bắt Buộc Mọi Hành Động Phải Nằm Trong Object (Lưu Reference Ở Stack + Object Ở Heap + Bytecode Ở Metaspace)
Advantages:
+ Gọn + Tập Trung Vào Logic Muốn Xử Lý Thay Vì Boilerplate Code Cồng Kềnh
+ Tối Ưu Hiệu Năng: Dùng Cơ Chế invokedynamic Khi Run Để Initialize Thay Vì Sinh File .class Rác -> Save RAM & Bộ Nhớ
* Compiler Ko Sinh File .class Cho Lambda Mà Chỉ Đặt Lệnh invokedynamic -> JVM Dùng LambdaMetafactory Để Sinh Class Ẩn Trực Tiếp Trong RAM/Metaspace -> Giảm Dung Lượng File Jar & Giảm Tải Cho ClassLoader
+ Hỗ Trợ Stream API
+ Lazy Evaluation: Kết Hợp Với Supplier Để Tránh Tính Toán/Query Nặng Khi Chưa Thực Sự Cần Thiết
- Anonymous Class: Subclass Ko Có Defined Name & Initialize Object -> Dùng Khi Chỉ Tạo 1 Object Thực Thi/Kế Thừa Từ 1 Class/Interface Cho 1 Lần Sử Dụng Duy Nhất

Method Reference: ClassName::methodName | instance::methodName -> Write Lambda Gọn
Static Method:
  (x) -> Math.abs(x) | Math::abs
Instance Method Của Specific Object:
  (x) -> System.out.println(x) | System.out::println
Instance Method Của Arbitrary Object:
  (str) -> str.toUpperCase() | String::toUpperCase
Constructor:
  () -> new ArrayList<>() | ArrayList::new
*/
@FunctionalInterface
interface Transformer<TInput, TOutput> {
  TOutput transform(TInput input);
  default <TResult> Transformer<TInput, TResult> andThen(Transformer<TOutput, TResult> next) {
    return input -> next.transform(this.transform(input));
  }
}
sealed interface Result<T> permits Success, Failure {
  boolean isSuccess();
  T getValueOrNull();
}
record Success<T>(T value) implements Result<T> {
  @Override public boolean isSuccess() { return true; }
  @Override public T getValueOrNull() { return value; }
}
record Failure<T>(String error, int code) implements Result<T> {
  @Override public boolean isSuccess() { return false; }
  @Override public T getValueOrNull() { return null; }
}
public class InterfacesFunctional {
  public static void functionalInterfaces() {
    // Predicate Test Boolean Condition
    Predicate<String> isLong = str -> str.length() > 5;
    System.out.println(isLong.test("VAK"));
    // Function Transform T -> R
    Function<String, Integer> length = String::length;
    System.out.println(length.apply("VAK"));
    // Consumer Consume Ko Return
    Consumer<String> print = System.out::println;
    print.accept("VAK");
    // Supplier Produce Ko Cần Input
    Supplier<String> timestamp = () -> String.valueOf(System.currentTimeMillis());
    System.out.println(timestamp.get());
    // BiFunction Hai Input Một Output
    BiFunction<Integer, Integer, Integer> max = Math::max;
    System.out.println(max.apply(10, 20));
    // UnaryOperator Function
    UnaryOperator<String> toUpperCase = String::toUpperCase;
    System.out.println(toUpperCase.apply("vak"));
    // BinaryOperator BiFunction
    BinaryOperator<Integer> add = Integer::sum;
    System.out.println(add.apply(3, 4));
  }
  public static int staticDouble(int x) { return x * 2; }
  public static void methodReferences() {
    // Static Method Reference: ClassName::staticMethod
    Function<Integer, Integer> staticRef = InterfacesFunctional::staticDouble;
    System.out.println(staticRef.apply(5));
    // Instance Method Reference Specific Object: object::instanceMethod
    String prefix = "VAK";
    Function<String, String> greeter = prefix::concat;
    System.out.println(greeter.apply(" 42"));
    // Instance Method Reference Arbitrary Object of Type: Class::instanceMethod
    Function<String, String> upper = String::toUpperCase;
    System.out.println(upper.apply("vak"));
    // Constructor Reference: ClassName::new
    Supplier<ArrayList<String>> listFactory = ArrayList::new;
    System.out.println(listFactory.get().getClass().getSimpleName());
  }

  /*
  Custom Functional Interface Chaining: Tự Định Nghĩa Functional Interface & Sử Dụng default Method Để Nối Chuỗi Thực Thi
  Sealed Class Interface: Limit Các Subclass Đc Phép Kế Thừa Bằng sealed & permits
  Exhaustive Pattern Matching: Cho Phép Write switch Expression Khớp Hoàn Toàn Các Case Mà Ko Cần default Case
  */
  public static void customFunctional() {
    Transformer<String, Integer> strToLen = String::length;
    Transformer<Integer, String> intToStr = n -> "Number:" + n;
    Transformer<String, String> pipeline = strToLen.andThen(intToStr);
    System.out.println(pipeline.transform("VAK"));
  }
  public static String describeResult(Result<?> result) {
    // Compiler Bt Chỉ Có Success & Failure Ko Cần default
    return switch (result) {
      case Success<?> s -> "Success: " + s.value();
      case Failure<?> f -> "Failure: " + f.code() + f.error();
    };
  }
  public static void sealedClasses() {
    Result<Integer> ok = new Success<>(42);
    Result<Integer> err = new Failure<>("Not Found", 404);
    System.out.println(describeResult(ok));
    System.out.println(describeResult(err));
  }

  public static void main(String[] args) {
    functionalInterfaces();
    methodReferences();
    customFunctional();
    sealedClasses();
  }
}