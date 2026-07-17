import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.*;

public class Concurrency {
  /*
  Thread Lifecycle: New -> (Runnable | Blocked | Waiting | Timed Waiting) -> Terminated
  Race Condition: Xảy Ra Khi Threads (>= 2) Cùng Share & Override Lên Shared Resource Mà Ko Đồng Bộ -> Dẫn Đến Kết Quả Sai Lệch Phụ Thuộc Vào Thứ Tự Chạy Luồng (VD: Thread 2 Dùng Giá Trị Cũ Vì Thread 1 Ch Chạy Xong)
  synchronized (Intrinsic/Monitor Lock): Method-Level Khóa this + Block-Level Khóa Lock Object -> Method/Block Nào Chạy Thì Dùng Khóa Này -> Method/Block Khác Ko Có -> Bị Blocked Ko Chạy Dc
    - Backed Bằng AQS (AbstractQueuedSynchronizer): Quản Lý State (volatile) (0 Rảnh - 1 Bận) & Hàng Đợi FIFO -> Sử Dụng CAS (Compare-And-Swap) Của CPU Để Đổi State
  volatile: Giải Quyết 2 Vấn Đề Lớn
    - Visibility: Ép Read/Write Trực Tiếp Từ Main RAM (Bypass - Skip CPU Cache) -> Cập Nhật Ngay Lập Tức Cho Các Core CPU Khác Thấy
    - Ngăn Instruction Reordering: Tạo Memory Barrier Cấm Compiler/CPU Đảo Thứ Tự Các Lệnh Xung Quanh Vì CPU Để Optimize Có Thể Đảo Thứ Tự
    - Happens-Before: Ghi Vào volatile Happens-Before Đọc Từ volatile Tiếp Theo
  */
  static class SafeCounter {
    private int count = 0;
    private volatile boolean running = true;
    public synchronized void syncIncrement() { count++; }
    public void blockIncrement() { synchronized(this) { count++; } }
    public int getCount() { return count; }
  }
  public static void threadSafety() throws InterruptedException {
    SafeCounter counter = new SafeCounter();
    Thread t1 = new Thread(counter::syncIncrement);
    Thread t2 = new Thread(counter::blockIncrement);
    t1.start(); t2.start(); t1.join(); t2.join();
    System.out.println("ThreadSafety Count: " + counter.getCount());
  }

  /*
  ExecutorService: Tách Task Submission Khỏi Thread Management - Thread Pool Quản Lý Số Lượng Thread Có Sẵn -> Thread Nào Rảnh Thì Thực Hiện Task -> Sau Khi Xong Quay Lại Pool Đợi Task Mới -> Tránh Việc Create/Destroy Threads Nhiều Lần
  Tuning Formula: NThreads = NCpu * UCpu * (1 + W/C) (NCpu: Số Core CPU, UCpu: 0 -> 1: % CPU Muốn Tận Dụng, W/C: Tỷ Lệ Wait / Compute)
  Pool Starvation: Task Bị Đợi Task Khác Trong Cùng Pool Khi Hết Thread Trống
  VD: 2 Threads + Task A Ở Thread 1 Đợi Result Từ Task B Nhưng Task B Nằm Trong Queue Vì Ko Còn Thread Nào Trống -> Đợi Vĩnh Viễn (Deadlock)
  */
  public static void executors() throws Exception {
    ExecutorService pool = Executors.newFixedThreadPool(2);
    Future<Integer> f = pool.submit(() -> 42);
    System.out.println("Executor Result: " + f.get());
    pool.shutdown();

    // ScheduledExecutorService: Chạy Định Kỳ Task
    ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(1);
    scheduled.schedule(() -> System.out.println("Scheduled Task"), 100, TimeUnit.MILLISECONDS);
    Thread.sleep(150);
    scheduled.shutdown();
  }

  /*
  CompletableFuture: Async Chaining & Composition
  Future: Muốn Lấy Result -> .get() -> Block Current Thread To Wait -> Phức Tạp Khi Write Nhiều Conditions => Dùng CompletableFuture (Similar To JS Promise)
  thenCompose: Chaining Async Operations (Flatmap Cho Future)
  thenApply: Transform Kết Quả (Map Cho Future)
  thenCombine: Gộp 2 Future Độc Lập
  allOf: Chờ Tất Cả Hoàn Thành
  anyOf: Chờ 1 Trong Các Future Hoàn Thành
  */
  public static void completableFuture() throws Exception {
    CompletableFuture<String> f = CompletableFuture.supplyAsync(() -> "User")
      .thenCompose(user -> CompletableFuture.supplyAsync(() -> user + "@mail.com"))
      .thenApply(String::toUpperCase)
      .exceptionally(err -> "fallback@mail.com");
    System.out.println("CompletableFuture: " + f.get());
  }

  /*
  Java.util.concurrent Utilities:
  CountDownLatch: 1/N Threads Chờ N Tasks Hoàn Thành - 1 Task Done -> Countdown - 1 -> Countdown = 0 -> Continue - (countDown(), await()) -> Non-Reuse
  CyclicBarrier: N Threads Chờ Nhau Tại Barrier -> N Threads Reach Barrier -> Continue - await() -> Reuse
  Semaphore: Giới Hạn Số Threads Đc Phép Access Limited Resource Cùng Lúc (acquire(), release())
  BlockingQueue: Thread-Safe Queue - Blocks put() Khi Full & take() Khi Empty - Solve Producer - Consumer Problem
  AtomicInteger: Increase/Decrease/Update 1 Cách Thread-Safe Mà Ko Cần Dùng Lock (lock-Free) Dựa Trên CAS (Compare-And-Swap)
  */
  public static void concurrentUtils() throws InterruptedException {
    // CountDownLatch & Semaphore & Atomic
    CountDownLatch latch = new CountDownLatch(1);
    Semaphore sem = new Semaphore(1);
    AtomicInteger atomic = new AtomicInteger(0);

    new Thread(() -> {
      try {
        sem.acquire();
        atomic.incrementAndGet();
        sem.release();
        latch.countDown();
      } catch (InterruptedException ignored) {}
    }).start();

    latch.await();
    System.out.println("Atomic Val: " + atomic.get());

    // CyclicBarrier
    CyclicBarrier barrier = new CyclicBarrier(2);
    new Thread(() -> { try { barrier.await(); } catch (Exception ignored) {} }).start();
    try { barrier.await(); } catch (Exception ignored) {}

    // BlockingQueue
    BlockingQueue<String> queue = new LinkedBlockingQueue<>(1);
    queue.put("Item");
    System.out.println("Queue Take: " + queue.take());
  }

  /*
  Virtual Threads (Java 21+): Lightweight JVM-Managed Threads
  - Platform Threads: Luồng Truyền Thống + Tỉ Lệ 1:1 Với OS Thread + Chạy Sát Phần Cứng + Khởi Tạo Nặng (~1MB Stack) + Ôm Chặt 1 Task Từ Đầu Đến Cuối (Ko Swap Được) -> Chờ I/O Sẽ Khóa Cứng Luồng OS
  - Carrier Threads: Bản Chất Là Platform Threads Đóng Vai Trò Làm Slot Cho Virtual Threads (VT Giữ Task) -> Có Thể Swap (Luân Phiên Chạy) Nhiều Virtual Threads Khác Nhau
  - Cơ Chế: Khi Virtual Thread Gặp Block I/O -> JVM Tự Động Unmount Virtual Thread Đưa Vào Heap -> Nhường Carrier Thread Cho Virtual Thread Khác -> Khi Xong I/O -> JVM Gắn Lại Virtual Thread Vào Carrier Thread Rảnh
  - newVirtualThreadPerTaskExecutor: Tạo Virtual Thread Mới Mỗi Task - Thay Thế Fixed Thread Pool Cho I/O-Bound Workloads
  - Cảnh Báo Pinning: Tránh Dùng synchronized Trong Virtual Thread Vì Sẽ Găm Chặt Vào Carrier Thread -> Khiến JVM Ko Thể Tháo Rời & Khóa Cứng Carrier Thread Khi Chờ I/O -> Giải Pháp: Thay Bằng ReentrantLock
  - synchronized (JVM Layer - Native C++ - Intrinsic Lock) Hoạt Động Ở Tầng Sâu Hơn So Vs ReentrantLock (Application Layer - Java - AQS)
  */
  public static void virtualThreads() throws Exception {
    // Platform vs Virtual Thread
    Thread vt = Thread.ofVirtual().start(() -> System.out.println("IsVirtual: " + Thread.currentThread().isVirtual()));
    vt.join();

    try (ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor()) {
      Future<String> f = exec.submit(() -> "Virtual Thread Executor");
      System.out.println(f.get());
    }
  }

  public static void main(String[] args) throws Exception {
    threadSafety();
    executors();
    completableFuture();
    concurrentUtils();
    virtualThreads();
  }
}