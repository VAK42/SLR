/*
JDK (Java Development Kit) Chứa: javac, JRE, Development Tools
JRE (Java Runtime Environment) Chứa: JVM + Standard Class Libraries
JVM (Java Virtual Machine) Thực Thi Bytecode, Quản Lý Memory, Threading, GC
Flow: .java -[javac]-> .class (Bytecode) -[JVM]-> Native Code
JIT Compiler: Phát Hiện Hot Code Paths - Đoạn Code Đc Thực Thi Lặp Nhiều Lần -> Compile Bytecode (Đc Đọc & Chạy Bởi Interpreter) Thành Native Machine Code
Nhiều .class Files -> Đóng Gói Thành .jar File
*/
public class JVMArchitecture {
  public static int computeSquare(int input) {
    return input * input;
  }
  /*
  Class Loading:
  Bootstrap Loader: Core Java Classes - rt.jar (Java 8-) / Module Hệ Thống (Java 9+) - Core JDK Classes
  Platform Loader (Java 9+) / Extension Loader (Java 8-) - Extended JDK Classes
  Application Loader: Classpath - User Custom Classes

  Delegation Model: Application -> Platform/Extension -> Bootstrap
  Mỗi Loader Hỏi Parent Trước Khi Tự Load
  */
  public static void classLoading() {
    Class<?> thisClass = JVMArchitecture.class;
    ClassLoader loader = thisClass.getClassLoader();
    System.out.println("Application Loader: " + loader);
    ClassLoader parent = loader.getParent();
    System.out.println("Extension Loader: " + parent);
    System.out.println("Bootstrap Loader: " + String.class.getClassLoader());
    /*
    Loading Phases:
    Loading: Đọc Bytecode Từ .class File
    Linking: Verify (Kiểm Tra Bytecode) + Prepare (Cấp Phát static) + Resolve (Symbol)
    Initialization: Chạy Static Initializer
    */
    System.out.println(thisClass.getName());
    System.out.println(thisClass.getSimpleName());
    System.out.println(thisClass.getPackageName());
  }
  static final int initializedValue; // Assign Default Value = 0 - Phase Preparation (In Linking)
  static {
    initializedValue = computeSquare(7); // Ran & Assigned - Phase Initialization
    /*
    initializedValue = 49 - Phase Preparation (Value Trực Tiếp & Ko Cần Đợi Hàm Trả Result)
    initializedValue = computeSquare(7) - Phase Initialization (Cần Đợi Hàm Trả Result)
    */
    System.out.println(initializedValue);
  }
  /*
  JVM Flags CLI Quan Trọng:

  Memory:
  -Xms512m -> Heap Khởi Đầu 512MB
  -Xmx2g -> Heap Tối Đa 2GB
  -Xss256k -> Stack Size Mỗi Thread

  GC:
  -XX:+UseG1GC -> Dùng G1 GC (Default Java 9+)
  -XX:+UseZGC -> Dùng ZGC (Low-Latency)
  -Xlog:gc* -> Bật GC Logging

  JIT:
  -Xint -> Interpreter-Only Mode (Tắt JIT)
  -server -> JIT Aggressiveness Cao Hơn

  Debug:
  -ea -> Bật Assertions
  -verbose:class -> Log Mỗi Class Được Load
  */
  public static void main(String[] args) {
    classLoading();
  }
}