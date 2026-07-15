public class PrimitivesTypes {
  // 8 Kiểu Nguyên Thủy Ko Phải Object + Đc Lưu Trực Tiếp Trên Stack
  public static void primitives() {
    byte byteVal = 127;                  // 8-bit signed: -128 -> 127
    short shortVal = 32767;              // 16-bit signed
    int intVal = 2147483647;             // 32-bit signed - Default: Integer Type
    long longVal = 9223372036854775807L; // 64-bit - Cần L Suffix
    float floatVal = 3.14f;              // 32-bit IEEE 754 - Cần f Suffix
    double doubleVal = 3.14159265358979; // 64-bit - Default: Floating Type
    char charVal = 'A';                  // 16-bit Unsigned Unicode Code Unit
    boolean boolVal = true;              // true/false

    // Numeric Separators — Dấu _ Giúp Đọc Số Lớn Dễ Hơn
    int million = 1_000_000;

    // Widening — Tự Động Mở Rộng (Nhỏ -> Lớn)
    int smallInt = 100;
    long widened = smallInt;        // int -> long: Tự Động

    // Narrowing — Phải Cast Tường Minh - Có Thể Mất Dữ Liệu
    double bigDouble = 9.99;
    int narrowed = (int) bigDouble; // Cắt Bỏ Phần Thập Phân -> 9

    // Overflow — Bọc Lại
    byte overflowed = (byte) 130;   // 130 > 127 -> Bọc -> -126
  }

  /*
  Autoboxing: primitive -> Wrapper
  Unboxing: Wrapper -> primitive
  -> Giúp Chuyển Đổi Giữa Primitive & Object -> Làm Cầu Nối Đưa Primitive Types Vào Làm Việc Với Generics & Collections
  */
  public static void autoboxing() {
    // Autoboxing: Compiler Tự Gọi Integer.valueOf(100)
    Integer a = 100;
    Integer b = 100;
    // Integer Cache: JVM Cache Integer Từ -128 To 127 -> a & b Cùng Trỏ Đến Cùng Object
    System.out.println(a == b);      // true - Cùng Object Cached
    System.out.println(a.equals(b)); // true
    Integer c = 200;
    Integer d = 200;
    // 200 Ngoài Cache -> 2 Object Khác Nhau
    System.out.println(c == d);      // false - Khác Object
    System.out.println(c.equals(d)); // true

    // Unboxing: Compiler Tự Gọi a.intValue()
    int primitive = a;
    // Lưu Ý: Unboxing null -> NullPointerException
    Integer nullable = null;
    try {
      int unboxed = nullable;
    } catch (NullPointerException e) {
      System.out.println("NullPointerException");
    }

    // Autoboxing Trong Collections
    java.util.List<Integer> numbers = new java.util.ArrayList<>();
    numbers.add(1); // Autoboxing: int -> Integer
    numbers.add(2);
    int sum = 0;
    for (int n : numbers) { // Unboxing: Integer -> int
      sum += n;
    }
    // Lưu Ý: Bad Performance Khi Nhiều Autoboxing Trong Loop
    Long total = 0L;
    for (long i = 0; i < 1000; i++) {
      total += i; // Unbox total -> Tính -> Box Lại -> Tạo Nhiều Object
    }
  }

  public static void var() {
    var list = new java.util.ArrayList<String>();
    list.add("Java");
    list.add("TypeScript");
    var message = "VAK";
    var number = 42;
    var decimal = 3.14;
    for (var item : list) {
      System.out.println("Item:" + item.toUpperCase());
    }
    /*
    var Ko Đc Dùng Cho:
    - Method Parameters
    - Return Types
    - Fields
    - Không Có Initializer
    var Ko Phải Dynamic Typing — Kiểu Vẫn Fixed Lúc Compile
    */
    var text = "VAK";
    // text = 42; // Lỗi Biên Dịch -> text: String
  }

  public static void stringPool() {
    String a = "OK";                   // String Pool Chưa Có "OK" -> Tạo Object Thêm Vào Pool
    String b = "OK";                   // String Pool Có "OK" Rồi -> Ko Tạo Object -> Trỏ Chung Vào Object Mà a Đg Trỏ
    String c = new String("OK");       // Tạo Object Trên Heap + Skip String Pool Check
    System.out.println(a == b);        // true  — Cùng Pool Object
    System.out.println(a == c);        // false — Khác Object
    System.out.println(a.equals(c));   // true  — Cùng Nội Dung
    // intern() — Đưa String Vào Pool Thủ Công
    String interned = c.intern();
    System.out.println(a == interned); // true
  }

  public static void main(String[] args) {
    primitives();
    autoboxing();
    var();
    stringPool();
  }
}