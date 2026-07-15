import java.lang.ref.*;
import java.lang.management.*;
import java.util.*;
public class MemoryGC {
  /*
  Stack (Per Thread):
  - Stack Frame: Local Variables (Primitive + Reference) + Operand Stack + Return Address
  - Tự Động Alloc/Free Khi Method Enter/Exit
  - Ko Có GC Overhead

  Heap (Shared):
  - Tất Cả Object Instances & Arrays
  - Young Generation + Old Generation
  - Managed By GC

  Operand Stack: 1 Vùng Nhớ Nháp Dạng LIFO Nằm Trong Mỗi Stack Frame -> JVM Dùng Làm Ko Gian Tính Toán Tạm Thời Để Thực Thi Các Lệnh Bytecode
  Return Address: Special Primitive Type Chứa Address Của Chỉ Thị Bytecode Kế Tiếp
  GC Overhead: Lượng Resource Hao Tốn Cho Dọn Rác
  Young Generation: Chứa Các Object Ms Tạo -> Eden + Survivor 0 + Survivor 1 -> Minor GC Dọn Nhanh Khi Đầy
  Old Generation: Chứa Các Object Sống Sót Qua Nhiều Lần MinorGC & Object Size Lớn -> Major GC/Full GC Dọn Lâu
  Metaspace: Phân Vùng Nhớ Store Metadata Của Class - Nằm Trong Native Memory (RAM)
  */
  private int field = 42;          // field Sống Trên Heap Cùng Với Instance
  public int compute(int param) {
    int local = param * 2;         // param & local Sống Trên Stack Frame Của Hàm Này
    MemoryGC obj = new MemoryGC(); // Reference 'obj' Trên Stack - Object Trên Heap
    return local + obj.field;
  }

  /*
  GC Roots & Reachability:
  GC Root: Điểm Bắt Đầu Của Object Graph
  - Active Local Variables Trong Stack Frames
  - Static Fields
  - Active Threads
  - JNI References
  */
  static MemoryGC staticRoot; // static: GC Root
  public static void reachability() {
    MemoryGC a = new MemoryGC();
    MemoryGC b = new MemoryGC();
    MemoryGC c = new MemoryGC();
    staticRoot = a;
    a.field = b.hashCode();
    a = null;
    b = null;
    System.gc();
    staticRoot = null;
    System.gc();
  }
  /*
  B1: Khởi Tạo
  Stack: a, b, c - Biến Tham Chiếu
  Heap: A, B, C - Đối Tượng

  B2: staticRoot = a
  staticRoot Trên Heap Trỏ Vào A -> A Đc Trỏ Bởi a & staticRoot

  B3: Set null Cho a, b
  a, b Trên Stack Bị Xóa -> B Unreachable
  staticRoot Trỏ A + c Trỏ C

  B4: GC
  Thu Hồi Bộ Nhớ Của B

  B5: Set null Cho staticRoot
  staticRoot Trên Heap Bị Xóa -> A Unreachable

  B6: GC
  Thu Hồi Bộ Nhớ Của A
  C Vẫn Sống -> reachability() Kết Thúc -> Stack Frame Bị Xóa -> c Biến Mất -> C Bị Thu Hồi Ở Lần Tiếp Theo
  */

  /*
  GC Algorithms:
  G1GC - Default:
  Heap Chia Thành Regions Bằng Nhau (1MB-32MB)
  Thu Regions Có Nhiều Garbage Nhất Trước
  Target Pause Goal: -XX:MaxGCPauseMillis=200
  Cân Bằng Throughput & Latency

  ZGC:
  Sub-millisecond Pauses - Ko Scale Theo Heap Size
  Concurrent Relocation
  Phù Hợp Latency-Sensitive Services
  -XX:+UseZGC

  Shenandoah:
  Concurrent Compaction Tương Tự ZGC
  -XX:+UseShenandoahGC

  Serial GC - Single-Thread & Stop-The-World:
  Phù Hợp Batch Processing + Small Heap
  -XX:+UseSerialGC

  Parallel GC - Multi-Thread & Stop-The-World:
  Throughput Cao + Pause Lớn
  -XX:+UseParallelGC
  */
  public static void memoryInfo() {
    Runtime runtime = Runtime.getRuntime();
    System.out.println(runtime.maxMemory());
    System.out.println(runtime.totalMemory());
    System.out.println(runtime.totalMemory() - runtime.freeMemory());
    MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
    System.out.println(memBean.getHeapMemoryUsage());
    System.out.println(memBean.getNonHeapMemoryUsage());
    for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
      System.out.println(pool.getName());
      System.out.println(pool.getType());
      System.out.println(pool.getUsage());
    }
  }

  // Reference Types - Kiểm Soát GC Behavior
  static class ExpensiveObject {
    private final int id;
    ExpensiveObject(int id) { this.id = id; }
    @Override
    public String toString() { return "ExpensiveObject(" + id + ")"; }
    @Override
    protected void finalize() {
      System.out.println(this);
    }
  }
  public static void references() throws InterruptedException {
    // SoftReference: Chỉ Bị Thu Khi Memory Thấp
    SoftReference<byte[]> soft = new SoftReference<>(new byte[1024]);
    System.out.println(soft.get() != null);

    // WeakReference: Bị Thu Ngay Lần GC Tiếp Theo
    WeakReference<String> weak = new WeakReference<>(new String("WeakValue"));
    System.out.println(weak.get());
    System.gc();
    Thread.sleep(50);
    System.out.println(weak.get()); // null - Đã Bị Thu

    // WeakHashMap: Dùng Làm Cache Tạm Thời Tránh Memory Leak - Entry Tự Động Bị Xóa Khi Key Ko Còn Strong Reference
    Map<String, Integer> weakMap = new WeakHashMap<>();
    String key = new String("TemporaryKey"); // Strong Reference: key
    // String key = "TemporaryKey";          // Strong Reference: key + String Constant Pool
    weakMap.put(key, 100);
    System.out.println(weakMap.size()); // 1
    key = null; // Xóa Strong Reference
    System.gc();
    Thread.sleep(50);
    System.out.println(weakMap.size()); // 0

    // PhantomReference: Dùng Để Dọn Dẹp Tài Nguyên Hệ Thống (Post-Mortem Cleanup) Thay Thế Cho finalize() Đã Bị Khai Tử
    ReferenceQueue<Object> refQueue = new ReferenceQueue<>();
    Object obj = new Object();
    PhantomReference<Object> phantom = new PhantomReference<>(obj, refQueue);
    System.out.println(phantom.get()); // Luôn null
    obj = null;
    System.gc();
    Thread.sleep(50);
    Reference<?> enqueuedRef = refQueue.poll();
    System.out.println(enqueuedRef != null);
    System.out.println(enqueuedRef == phantom);
  }

  /*
  Memory Leak Patterns:
  P1: Static Collection Giữ References:
  static final Map<String, Object> CACHE = new HashMap<>();
  static Thuộc Về Class -> Sống Ở Metaspace & Tồn Tại Suốt Vòng Đời App -> Ko Bao Giờ Clear -> Leak
  Solution: WeakHashMap

  P2: Inner Class Giữ Outer Reference:
  class Outer { class Inner {} } -> Inner Class Giữ OuterClass.this -> Nếu Inner Class Đc Dùng Dù Outer Class Finished -> Ko Thể Bị GC Thu Hồi Vì Inner Class Giữ this
  Solution: Static Inner Class

  P3: Listeners Ko Đc Unregister:
  eventBus.subscribe(listener) Mà Ko Gọi unsubscribe -> Leak
  Solution: Phải unregister/unsubscribe

  P4: Thread Local Ko Đc Remove:
  ThreadLocal<Object> local = new ThreadLocal<>(); -> Nếu Ko Xóa -> Thread Ko Empty -> Ko Thể Tái Sử Dụng
  Trong Thread Pool: local.remove() Sau Khi Dùng Xong!
  */
  public static void main(String[] args) throws Exception {
    MemoryGC demo = new MemoryGC();
    System.out.println(demo.compute(10));
    reachability();
    memoryInfo();
    references();
    System.out.println();
    long before = Runtime.getRuntime().freeMemory();
    int[] bigArray = new int[1_000_000];
    bigArray = null;
    System.gc();
    System.out.println(Runtime.getRuntime().freeMemory() / 1024);
  }
}