import java.io.*;
import java.util.*;

// Custom Exception Hierarchy
class AppException extends RuntimeException {
  private final int code;
  public AppException(String message, int code) {
    super(message);
    this.code = code;
  }
  public AppException(String message, int code, Throwable cause) {
    super(message, cause);
    this.code = code;
  }
  public int getCode() { return code; }
}
class ValidationException extends AppException {
  private final String field;
  public ValidationException(String field, String message) {
    super(message, 400);
    this.field = field;
  }
  public String getField() { return field; }
}
class NotFoundException extends AppException {
  public NotFoundException(String resource, Object id) {
    super(resource + " Not Found: " + id, 404);
  }
}
class ServiceException extends Exception {
  public ServiceException(String message, Throwable cause) {
    super(message, cause);
  }
}

public class ExceptionHandling {
  /*
  Checked: Extend Exception Trực Tiếp — Phải catch/throws
  Unchecked: Extend RuntimeException — Tùy Chọn catch
  Try With Resources: Auto Cleanup Ko Cần finally + Resources Đóng Theo LIFO - AutoCloseable
  */
  public static String readFile(String path) throws IOException {
    StringBuilder sb = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
      // BufferedReader Đã Implement Sẵn Closeable - 1 Interface Con Của AutoCloseable
      String line;
      while ((line = reader.readLine()) != null) {
        sb.append(line).append("\n");
      }
    }
    return sb.toString();
  }
  public static int parseDivide(String input, int divisor) {
    try {
      int value = Integer.parseInt(input);
      return value / divisor;
    } catch (NumberFormatException | ArithmeticException e) {
      System.out.println(e.getClass().getSimpleName() + ": " + e.getMessage());
      return 0;
    }
  }
  static class ManagedResource implements AutoCloseable {
    private final String name;
    ManagedResource(String name) {
      this.name = name;
      System.out.println("Opening " + name);
    }
    public void use() {
      System.out.println("Using " + name);
    }
    @Override
    public void close() {
      System.out.println("Closing " + name);
    }
  }
  static class FailingResource implements AutoCloseable {
    @Override
    public void close() throws Exception {
      throw new Exception("Close Failure");
    }
  }

  // Exception Chaining: Friendly Log Cho End User + Techninal Log Cho Dev
  public static void connectToDb(String host) throws IOException {
    throw new IOException("Connection Refused: " + host);
  }
  public static void runQuery(String host) throws ServiceException {
    try {
      connectToDb(host);
    } catch (IOException e) {
      throw new ServiceException("Database Unavailable", e);
    }
  }

  // Custom Exception Usage
  public static String getUser(int userId) {
    if (userId <= 0) {
      throw new ValidationException("userId", "Must Be Positive");
    }
    if (userId > 1000) {
      throw new NotFoundException("User", userId);
    }
    return "User: " + userId;
  }

  public static void main(String[] args) {
    // Checked Exception
    try {
      String content = readFile("VAK.txt");
    } catch (IOException e) {
      System.out.println("IOException: " + e.getMessage());
    }

    // Multi-Catch
    System.out.println(parseDivide("vak", 5));
    System.out.println(parseDivide("42", 0));

    // Try With Resources
    try (ManagedResource resA = new ManagedResource("Database");
       ManagedResource resB = new ManagedResource("Cache")) {
      resA.use();
      resB.use();
    }

    // Suppressed Exceptions: Khi Cả Body & close() Đều Throw
    try (FailingResource fr = new FailingResource()) {
      throw new RuntimeException("BodyException");
    } catch (RuntimeException e) {
      System.out.println("Primary: " + e.getMessage());
      for (Throwable suppressed : e.getSuppressed()) {
        System.out.println("Suppressed: " + suppressed.getMessage());
      }
      /*
      Primary: BodyException
      Suppressed: Close Failure
      */
    } catch (Exception e) {
      System.out.println("Exception: " + e.getMessage());
    }

    // Exception Chaining
    try {
      runQuery("db.backend.com");
    } catch (ServiceException e) {
      System.out.println("ServiceException: " + e.getMessage()); // ServiceException: Database Unavailable
      System.out.println("Cause: " + e.getCause().getMessage()); // Cause: Connection Refused: db.backend.com
      System.out.println("Root Cause Class: " + e.getCause().getClass().getSimpleName()); // Root Cause Class: IOException
    }

    // Custom Exception Hierarchy
    int[] testIds = {-1, 0, 999, 9999, 42};
    for (int id : testIds) {
      try {
        System.out.println(getUser(id));
      } catch (ValidationException e) {
        System.out.println(e.getField() + ": " + e.getCode() + ": " + e.getMessage());
      } catch (NotFoundException e) {
        System.out.println(e.getCode() + ": " + e.getMessage());
      } catch (AppException e) {
        System.out.println(e.getCode() + ": " + e.getMessage());
      }
    }
    try {
      throw new RuntimeException("Test Error");
    } catch (RuntimeException e) {
      System.out.println("Caught: " + e.getMessage());
    } finally {
      System.out.println("Luôn Chạy Dù Có Lỗi");
    }
  }

  // Lưu Ý: return/throw Trong finally Sẽ Override & Nuốt Chửng return/throw Trong try/catch -> Ko return/throw Trong finally Nếu Ko Cần Thiết
  static int vak = 0;
  static String Finally() {
    try {
      vak = 18; // Vẫn Chạy
      return "try";
    } finally {
      return "finally"; // Override return Trong try
    }
  }
}