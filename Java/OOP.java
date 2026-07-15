import java.util.List;
import java.util.ArrayList;
// OOP: Encapsulation + Inheritance + Polymorphism + Abstraction

// Encapsulation: Che Giấu Dữ Liệu + Kiểm Soát Truy Cập
class BankAccount {
  private String owner;
  private double balance;         // private: Ko Ai Truy Cập Trực Tiếp
  private final String accountId; // final: Ko Thể Gán Lại
  public BankAccount(String owner, double initialBalance, String accountId) {
    this.owner = owner;
    this.accountId = accountId;
    setBalance(initialBalance);   // Dùng Setter Để Validate Ngay Trong Constructor
  }
  public double getBalance() { return balance; }
  public String getAccountId() { return accountId; }
  private void setBalance(double amount) {
    if (amount < 0) throw new IllegalArgumentException("Balance Must Not Be Negative");
    this.balance = amount;
  }
  public void deposit(double amount) {
    if (amount <= 0) throw new IllegalArgumentException("Deposit Must Be Positive");
    balance += amount;
  }
  public void withdraw(double amount) {
    if (amount <= 0) throw new IllegalArgumentException("Withdrawal Must Be Positive");
    if (amount > balance) throw new IllegalStateException("Insufficient Funds");
    balance -= amount;
  }
  @Override
  public String toString() {
    return "BankAccount: " + owner + " Balance: " + balance;
  }
}

// Inheritance: Kế Thừa Với extends & super
class Vehicle {
  private String brand;
  protected int speed;
  public Vehicle(String brand, int speed) {
    this.brand = brand;
    this.speed = speed;
  }
  public String getBrand() { return brand; }
  public String describe() {
    return "Brand: " + brand + " Speed: " + speed;
  }
}
class Car extends Vehicle {
  private int doors;
  public Car(String brand, int speed, int doors) {
    super(brand, speed); // Gọi Constructor Của Vehicle
    this.doors = doors;
  }
  @Override
  public String describe() {
    return super.describe() + " Doors: " + doors; // Mở Rộng + Không Thay Thế
  }
}
class ElectricCar extends Car {
  private int batteryKwh;
  public ElectricCar(String brand, int speed, int doors, int batteryKwh) {
    super(brand, speed, doors);
    this.batteryKwh = batteryKwh;
  }
  @Override
  public String describe() {
    return super.describe() + " Battery: " + batteryKwh;
  }
}

// Abstract Class: Ko Thể Khởi Tạo - Hỗn Hợp Abstract + Concrete
abstract class Shape {
  private String color;
  public Shape(String color) { this.color = color; }
  // Abstract Method — Subclass Bắt Buộc Phải Implement
  public abstract double area();
  public abstract double perimeter();
  // Concrete Method — Subclass Kế Thừa
  public String getColor() { return color; }
  public String describe() {
    return getClass().getSimpleName() + color + String.format("%.2f", area());
  }
}
class Circle extends Shape {
  private double radius;
  public Circle(String color, double radius) {
    super(color);
    this.radius = radius;
  }
  @Override
  public double area() { return Math.PI * radius * radius; }
  @Override
  public double perimeter() { return 2 * Math.PI * radius; }
}
class Rectangle extends Shape {
  private double width, height;
  public Rectangle(String color, double width, double height) {
    super(color);
    this.width = width;
    this.height = height;
  }
  @Override
  public double area() { return width * height; }
  @Override
  public double perimeter() { return 2 * (width + height); }
}


/*
Access Modifiers:
public: Truy Cập Từ Bất Kỳ Đâu
protected: Cùng Package + Subclass (Dù Khác Package)
default: Chỉ Cùng Package
private: Chỉ Trong Class Đó
*/
public class OOP {
  // Đa Hình Compile-Time (Overloading): Nạp Chồng Phương Thức
  public static int add(int a, int b) { return a + b; }
  public static double add(double a, double b) { return a + b; }
  public static String add(String a, String b) { return a + b; }

  // Reusable Code: Write 1 Hàm Nhận Shape (Cha) Để Dùng Chung Cho Mọi Lớp Con
  public static void showArea(Shape s) {
    /*
    Có Polymorphism -> Code Tái Sử Dụng Đc (showArea Nhận Bất Cứ Subclass Nào Của Shape)
    Ko Có Polymorphism -> Phải Write Hàm Riêng Cho Từng Subclass (showAreaCircle, showAreaRectangle) -> Lặp Code
    */
    System.out.println("Area: " + s.area());
  }

  public static void main(String[] args) {
    // Runtime Polymorphism (Dynamic Method Dispatch - Method Overriding)
    Shape c = new Circle("Red", 5);
    Shape r = new Rectangle("Blue", 4, 6);
    
    // B1: Unified Collections: Group Different Types Together - Cùng Kiểu Shape -> List | Khác Kiểu -> Ko List Đc
    List<Shape> shapes = new ArrayList<>();
    shapes.add(new Circle("Red", 5));
    shapes.add(new Rectangle("Blue", 4, 6));
    
    // B2: Reusable Code: Gọi Hàm Dùng Chung
    showArea(c);
    showArea(r);

    // B3: Loose Coupling
    Shape shape = new Circle("Green", 3);
    System.out.println("Color: " + shape.getColor() + " Area: " + shape.area());
    shape = new Rectangle("Yellow", 2, 8); // Swap Sang Class Khác Dễ Dàng - Ko Cần Phải Tạo Biến Mới
    System.out.println("Color: " + shape.getColor() + " Area: " + shape.area());

    // Compile-Time Polymorphism (Method Overloading)
    System.out.println(add(10, 20));
    System.out.println(add(1.5, 2.5));
    System.out.println(add("VAK ", "42"));
  }
}

/*
Java Chỉ Cho Kế Thừa Từ 1 Class Nhưng Nhiều Interface
Interface Chỉ Chứa Methods & Constants + Ko Đc Phép Chứa Properties

Dùng Abstract Method Khi Muốn Bắt Lớp Con Phải Override/Implement Nhưng Lớp Con Đó Vẫn Cần Thừa Hưởng Properties & Concrete Methods Khác Từ Lớp Cha
Dùng Interface Method Khi Chỉ Thuần Túy Muốn Đặt Ra Contract + Ko Liên Quan Gì Đến Property Hay Class Hierarchy Của Object

Lý Do Dùng Interface:
- Giảm Sự Phụ Thuộc Trực Tiếp Giữa Các Class (Loose Coupling)
- Giúp Class Gọi Code (Caller) Không Cần Thay Đổi Khi Hệ Thống Đc Mở Rộng Bằng Cách Thêm Class Mới
- Gom Nhóm Các Class Khác Nhau Dưới 1 Kiểu Dữ Liệu Chung (Interface Type) Để Quản Lý & Xử Lý Đồng Bộ Thông Qua Polymorphism
*/