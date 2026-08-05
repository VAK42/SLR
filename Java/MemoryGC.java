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
  - Tất Cả Obj Instances & Arrays
  - Young Generation + Old Generation
  - Managed By GC

  Operand Stack: 1 Vùng Nhớ Nháp Dạng LIFO Nằm Trong Mỗi Stack Frame → JVM Dùng Làm Ko Gian Tính Toán Tạm Thời Để Thực Thi Các Lệnh Bytecode
  Return Address: Special Primitive Type Chứa Address Của Chỉ Thị Bytecode Kế Tiếp
  GC Overhead: Lượng Resource Hao Tốn Cho Dọn Rác
  Young Generation: Chứa Các Obj Ms Tạo → Eden + Survivor 0 + Survivor 1 → Minor GC Dọn Nhanh Khi Đầy
  Old Generation: Chứa Các Obj Sống Sót Qua Nhiều Lần MinorGC & Obj Size Lớn → Major GC/Full GC Dọn Lâu
  Metaspace: Phân Vùng Nhớ Store Metadata Của Class - Nằm Trong Native Memory (RAM)
  */
  private int field = 42;          // field Sống Trên Heap Cùng Với Instance
  public int compute(int param) {
    int local = param * 2;         // param & local Sống Trên Stack Frame Của Hàm Này
    MemoryGC obj = new MemoryGC(); // Reference 'obj' Trên Stack - Obj Trên Heap
    return local + obj.field;
  }

  /*
  GC Roots & Reachability:
  GC Root: Điểm Bắt Đầu Của Obj Graph
  - Active Local Variables Trong Stack Frames
  - Static Fields
  - Active Threads
  - JNI References
  JNI: Java Native Interface - Bridge Between Java & C/C++
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
  staticRoot Trên Heap Trỏ Vào A → A Dc Trỏ Bởi a & staticRoot

  B3: Set null Cho a, b
  a, b Trên Stack Bị Xóa → B Unreachable
  staticRoot Trỏ A + c Trỏ C

  B4: GC
  Thu Hồi Bộ Nhớ Của B

  B5: Set null Cho staticRoot
  staticRoot Trên Heap Bị Xóa → A Unreachable

  B6: GC
  Thu Hồi Bộ Nhớ Của A
  C Vẫn Sống → reachability() Kết Thúc → Stack Frame Bị Xóa → c Biến Mất → C Bị Thu Hồi Ở Lần Tiếp Theo
  */

  /*
  * GC Algorithms:
  G1GC - Default:
  Heap Chia Thành Regions Bằng Nhau (1MB-32MB)
  Every GC Cycle Sẽ Ưu Tiên Các Region Có Nhiều Garbage Để Reclaim (Descending Order)
  Các Region Ít Garbage Hơn Có Thể Dc Xử Lý Ở Các Cycle Sau
  Live Objs Dc Copy Sang Region Khác Để Compact Memory
  Target Pause Goal: -XX:MaxGCPauseMillis=200
  Cân Bằng Throughput & Latency

  ZGC:
  Sub-millisecond Pauses - Ko Scale Theo Heap Size
  Ko Dừng App Lâu Khi GC
  Mark Objs + Relocate Objs Chủ Yếu Concurrent Với Application
  Live Objs Dc Di Chuyển Sang Region Ms
  Region Cũ Dc Release Sau Khi Relocation Hoàn Tất
  Phù Hợp Latency-Sensitive Services
  -XX:+UseZGC

  Shenandoah:
  Concurrent Compaction Tương Tự ZGC
  Mark + Evacuate + Compact Heap Trong Khi Application Vẫn Chạy
  Move Live Objs Sang Vùng Nhớ Ms
  Giảm Fragmentation Mà Ko Cần Stop-The-World Lâu
  -XX:+UseShenandoahGC

  Serial GC - Single-Thread & Stop-The-World:
  Dừng Toàn Bộ Application Khi GC
  Scan Toàn Bộ Heap = 1 Thread
  Xóa Garbage Objs
  Compact Live Objs Lại Để Tạo Vùng Nhớ Trống Liền Kề
  Phù Hợp Batch Processing + Small Heap
  -XX:+UseSerialGC

  Parallel GC - Multi-Thread & Stop-The-World:
  Similar To Serial GC Nhưng Dùng Multi GC Thread
  Chia Công Việc Scan/Cleanup Heap Cho Multi Thread
  Dọn Garbage Nhanh Hơn Nhưng Pause Time Có Thể Lớn
  Tối Ưu Throughput Thay Vì Latency
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

  /*
  * Reference Types:
  Strong Reference:
  Normal Reference Trong Java
  Obj Có Strong Reference Thì GC Ko Dc Phép Thu Hồi
  Obj Sống Cho Đến Khi Ko Còn Any Strong Reference Nào
  VD: Obj obj = new Obj();

  WeakReference:
  Khi Obj Ko Còn Strong Reference → GC Có Thể Thu Hồi
  WeakReference Ko Giữ Obj Sống
  Thường Dùng Cho Cache | Mapping Ko Muốn Ngăn GC
  VD: WeakHashMap

  SoftReference:
  GC Chỉ Thu Hồi Khi JVM Thiếu Memory
  Thường Dùng Cho Memory-Sensitive Cache
  Ko Đảm Bảo Obj Sẽ Luôn Được Giữ

  PhantomReference:
  Reference Mạnh Hơn Obj Bị Xóa Nhưng Yếu Hơn WeakReference
  Obj Đã Dc GC Xác Định Để Thu Hồi
  Ko Thể Truy Cập Lại Obj Thông Qua PhantomReference
  JVM Đưa Reference Vào ReferenceQueue Trc Khi Reclaim Memory Hoàn Toàn
  Dùng Để Cleanup Native Resource Ngoài Java Heap
  VD: Native Memory, File Handle, GPU Resource
  */
  static class ExpensiveObj {
    private final int id;
    ExpensiveObj(int id) { this.id = id; }
    @Override
    public String toString() { return "ExpensiveObj(" + id + ")"; }
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
  * Memory Leak Patterns:
  P1: Static Collection Giữ References:
  static final Map<String, Obj> CACHE = new HashMap<>();
  static: Belong To The Class → Class Metadata: Live In Metaspace & Exist Throughout App Lifetime
  HashMap: Hold Strong References → All Objs Added To The Map Remain Alive Until App Termination → Never Be Cleared
  → Solution: WeakHashMap

  P2: Inner Class Giữ Outer Reference:
  class Outer { class Inner {} }
  Non-static Inner Class: Inner Obj Giữ Hidden Reference Tới Outer Obj (Outer.this)
  + Nếu Inner Obj Còn Sống Thì Outer Obj Ko Thể Bị GC Thu Hồi
  + Có Thể Gây Memory Leak Khi Inner Obj Sống Lâu Hơn Outer Obj
  Static Inner Class: Inner Obj Ko Giữ Reference Tới Outer Obj
  + Chỉ Thuộc Về Outer Class (Class Metadata Trong Metaspace)
  + Ko Ảnh Hưởng Đến Vòng Đời Của Outer Instance
  → Solution: Static Inner Class

  P3: Listeners Ko Dc Unregister:
  eventBus.subscribe(listener) Mà Ko Gọi unsubscribe → Leak
  → Solution: Phải unregister/unsubscribe

  P4: Thread Local Ko Dc Remove:
  ThreadLocal<Obj> local = new ThreadLocal<>(); → Nếu Ko Xóa → Thread Ko Empty → Ko Thể Reuse
  → Solution: Trong Thread Pool: local.remove() Sau Khi Dùng Xong
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