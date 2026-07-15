import java.util.*;
public class ModernFeatures {
  // Record: Special Class Khai Báo Nhanh Immutable Data Class + Tự Động Sinh Đầy Đủ Constructor & Boilerplate Methods Khi Compile
  record Point(double x, double y) {
    // Compact Constructor
    Point {
      if (Double.isNaN(x) || Double.isNaN(y)) {
        throw new IllegalArgumentException("Coordinates Must Be Finite");
      }
    }
    public double distanceTo(Point other) {
      double dx = this.x - other.x;
      double dy = this.y - other.y;
      return Math.sqrt(dx * dx + dy * dy);
    }
  }
  public static void records() {
    Point p1 = new Point(0, 0);
    Point p2 = new Point(3, 4);
    System.out.println(p1);
    System.out.println(p1.x());
    System.out.println(p1.equals(new Point(0, 0)));
    System.out.println(p1.distanceTo(p2));
  }

  /*
  Pattern Matching: Type Check + Type Cast + Var Assign
  sealed interface: Limit Các Subclass Có Thể Implement + Bắt Buộc Exhaustive Switch - Switch Cover Tất Cả Case
  instanceof: Loại Bỏ Cast Thủ Công Sau Type Check
  switch: Match Case + when: Điều Kiện Lọc
  */
  sealed interface Shape permits CircleShape, RectangleShape, TriangleShape {}
  record CircleShape(double radius) implements Shape {}
  record RectangleShape(double width, double height) implements Shape {}
  record TriangleShape(double base, double height) implements Shape {}
  public static String classifyShape(Shape shape) {
    return switch (shape) {
      case CircleShape c when c.radius() > 10 -> "Large Circle: " + c.radius();
      case CircleShape c -> "Small Circle: " + c.radius();
      case RectangleShape r when r.width() == r.height() -> "Square: " + r.width();
      case RectangleShape r -> "Rectangle: " + r.width() + "x" + r.height();
      case TriangleShape t -> "Triangle";
    };
  }
  public static Object describe(Object obj) {
    if (obj instanceof String str && str.length() > 5) {
      return str.toUpperCase();
    } else if (obj instanceof Integer n && n > 0) {
      return n;
    } else if (obj instanceof CircleShape c) {
      return c.radius();
    }
    return "Unknown: " + obj;
  }
  public static void patternMatching() {
    Shape[] shapes = {
      new CircleShape(12),
      new RectangleShape(5, 5),
      new TriangleShape(6, 4)
    };
    for (Shape s : shapes) {
      System.out.println(classifyShape(s));
    }
    System.out.println(describe("VAK"));
    System.out.println(describe(42));
    System.out.println(describe(new CircleShape(2.5)));
  }

  /*
  Switch Expressions
  ->: Ko Fall-Through + Ko Cần Break
  yield: Return Value
  */
  enum Day { Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday }
  public static String describeDay(Day day) {
    return switch (day) {
      case Saturday, Sunday -> "Weekend";
      case Monday -> {
        String msg = "Start Of Week";
        yield msg;
      }
      default -> "Weekday";
    };
  }
  public static void switchExpressions() {
    for (Day day : Day.values()) {
      System.out.println(day + ": " + describeDay(day));
    }
  }

  // Text Blocks: Multiline String Literals Ko Cần Escape
  public static void textBlocks() {
    String json = """
      {
        "name": "Alice",
        "age": 30,
        "active": true
      }
      """;
    System.out.println(json);
    String name = "Bob";
    int age = 25;
    String greeting = """
      Name: %s
      Age: %d
      """.formatted(name, age);
    System.out.println(greeting);
  }

  public static void main(String[] args) {
    records();
    patternMatching();
    switchExpressions();
    textBlocks();
  }
}