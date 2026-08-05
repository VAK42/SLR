import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.*;

public class Concurrency {
  /*
  Thread Lifecycle: New → Runnable <→ (Blocked | Waiting | Timed Waiting) → Terminated
  Race Condition: Xảy Ra Khi Threads (>= 2) Cùng Share & Override Lên Shared Resource Mà Ko Đồng Bộ → Dẫn Đến Kết Quả Sai Lệch Phụ Thuộc Vào Thứ Tự Chạy Luồng (VD: Thread 2 Dùng Giá Trị Cũ Vì Thread 1 Ch Chạy Xong)
  synchronized (Intrinsic Lock | Monitor Lock): Dùng Để Đảm Bảo Mutual Exclusion: Tại 1 Thời Điểm Chỉ 1 Thread Dc Thực Thi 1 Đoạn Code synchronized Dc Bảo Vệ Bởi Cùng 1 Monitor
  → Every Obj Trong Java Đều Có 1 Monitor Gắn Kèm (Intrinsic Lock)

  Method-Level Lock:
  + Instance Method: Lock Obj Instance (this)
  + Static Method: Lock Class Obj (ClassName.class)

  Block-Level Lock:
  + Lock Chính Obj Dc Truyền Vào
  + Nên Dùng Obj Riêng Để Giảm Lock Contention
  
  Cơ Chế: Thread Acquire Monitor Lock
  - Nếu Lock Free: Thread Vào Critical Section
  - Nếu Lock Dg Bị Giữ: Thread Bị Blocked + Đưa Vào Monitor Entry Queue
  - Thread Giữ Lock Exit → JVM Release Lock → Thread Khác Có Thể Acquire

  synchronized Lock:
  + Reentrant: Thread Dg Giữ Lock Có Thể Vào Lại synchronized Cùng Lock (Ko Bị Deadlock Với Chính Nó)
  + Ko Fair: JVM Ko Đảm Bảo Thread Waiting Lâu Nhất Dc Chạy Trc

  synchronized Implementation: JVM-Level Mechanism (Intrinsic Monitor)
  + Ko Dựa Trên AQS
  + Dc JVM Implement Bằng Obj Monitor (Native Code)
  + Có Thể Block Thread Ở OS Level Khi Contention Cao

  ReentrantLock: Backed Bằng AQS (AbstractQueuedSynchronizer)
  AQS:
  + Quản Lý State (volatile int): 0 = Lock Free | 1 = Locked
  + CLH Queue Quản Lý Thread Waiting (Ko Phải FIFO Tuyệt Đối Trong Mọi Trường Hợp)
  + Dùng CAS (Compare-And-Swap) Của CPU → Atomically Update State
  ReentrantLock > synchronized Lock: tryLock() | Timeout | Interruptible | Fair Lock | Multiple Conditions

  volatile: Đảm Bảo Visibility + Ordering (Ko Đảm Bảo Atomicity)
  + Visibility: Thread A Write volatile variable → Value Dc Publish Theo Java Memory Model → Thread B Read Cùng Variable Sẽ Thấy Value Ms (Memory Barrier + CPU Cache Coherence)
  + Prevent Instruction Reordering: Compiler/CPU Có Thể Reorder Instruction Để Optimize → volatile Tạo Memory Barrier: Cấm Reorder Các Operation Xung Quanh volatile Access + Đảm Bảo Thứ Tự Execution Theo Java Memory Model
  + Happens-Before Guarantee: Write Vào volatile Variable → Happens-Before → Read volatile Variable Sau Đó → Mọi Thread Đọc Dc Value volatile Đồng Thời Nhìn Thấy Các Write Trc Đó Của Thread Ghi

  Happens-Before Relationship (Java Memory Model): Operation A Happens-Before Operation B → Result Của A Visible Với B + Ordering Dc Đảm Bảo
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
  ExecutorService: Tách Task Submission Khỏi Thread Management - Thread Pool Quản Lý Số Lượng Thread Có Sẵn → Thread Nào Rảnh Thì Thực Hiện Task → Sau Khi Xong Quay Lại Pool Đợi Task Mới → Tránh Việc Create/Destroy Threads Nhiều Lần
  Tuning Formula: NThreads = NCpu * UCpu * (1 + W/C) (NCpu: Số Core CPU, UCpu: 0 → 1: % CPU Muốn Tận Dụng, W/C: Tỷ Lệ Wait / Compute)

  Thread Pool|Starvation Deadlock:
  Task A Submit Task B Vào Chính Executor + Future.get()|join() Đồng Bộ Chờ Result
  → Task A Giữ Thread Trong Lúc Chờ
  → Task B Dc Đưa Vào Queue + Cần 1 Thread Để Execute
  → Thread Pool Hết Thread Trống → Task B Ko Bh Dc Schedule
  → Task A Ko Bh Nhận Dc Result → Wait Vĩnh Viễn (Deadlock)
  Điều Kiện:
  + Task Cha & Task Con Dùng Chung Executor
  + Thread Pool Quá Nhỏ Or Đã Full
  + Blocking Bằng Future.get()|join()
  Giải Pháp:
  + Tách Executor Khác Cho Task Con
  + Dùng CompletableFuture Chaining (thenCompose|thenCombine...) Thay Vì Blocking
  + Tránh .get()|join() Trong Thread Pool Nhỏ
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
  CompletableFuture (Java 8+): Async Programming + Chaining + Composition (CompletionStage API) (Similar To JS Promise)
  Future:
  + Muốn Lấy Result → .get() (Blocking Current Thread)
  + Ko Hỗ Trợ Chaining | Composition | Exception Handling Tốt
  + Dễ Dẫn Đến Callback Hell | Nested Blocking

  CompletableFuture: Future + CompletionStage
  + Non-Blocking Chaining (Khai Báo Flow Trc + Tự Chạy Khi Có Result)
  + Hỗ Trợ Composition, Exception Handling, Parallel Execution

  Create:
  supplyAsync(Supplier<T>): Async Có Return Value (~ Promise.resolve(asyncFn()))
  runAsync(Runnable): Async Ko Return Value (~ Promise<void>)

  thenApply (~ Promise.then()):
  + Transform Result (Map Cho Future)
  + Future<T> → Future<U>

  thenCompose (~ Promise.then() Return Promise):
  + Chaining Async Operations (FlatMap Cho Future)
  + Future<T> → Future<Future<U>> → Flatten → Future<U>
  + Dùng Khi Function Return CompletableFuture

  thenCombine (~ Promise.all([A, B]).then()):
  + Merge 2 CompletableFuture Độc Lập Chạy Song Song
  + Future<A> + Future<B> → Future<C>

  allOf (~ Promise.all()): Chờ Tất Cả Future Hoàn Thành → Trả CompletableFuture<Void> → join()|get() Từng Future Để Lấy Result
  anyOf (~ Promise.race()): Chờ Future Đầu Tiên Hoàn Thành (Success | Exception) → Trả CompletableFuture<Object>

  thenAccept (~ Promise.then(v => {...})):
  + Consume Result Ko Return Value
  + Future<T> → Future<Void>

  thenRun (~ Promise.then(() => {...})):
  + Chạy Task Tiếp Theo Ko Quan Tâm Result Trc
  + Future<T> → Future<Void>

  exceptionally (~ Promise.catch()): Handle Exception + Return Default Value
  handle (~ Promise.then(success, error)): Luôn Chạy (Success | Exception) → Có Thể Transform Result | Recover Exception
  whenComplete (~ Promise.finally() + Có Access Result): Luôn Chạy Sau Completion (Success/Fail) → Chủ Yếu Logging / Cleanup + Ko Thay Đổi Result

  Async Variants:
  thenApply(): Có Thể Chạy Trên Thread Hoàn Thành Stage Trc
  thenApplyAsync() | thenComposeAsync() | thenAcceptAsync() | thenRunAsync(): Luôn Dispatch Sang Executor Khác

  join(): Unchecked Exception (CompletionException)
  get(): Checked Exception (InterruptedException, ExecutionException)
  */
  public static void completableFuture() throws Exception {
    CompletableFuture<String> f = CompletableFuture.supplyAsync(() -> "User")
      .thenCompose(user -> CompletableFuture.supplyAsync(() -> user + "@mail.com"))
      .thenApply(String::toUpperCase)
      .exceptionally(err -> "fallback@mail.com");
    System.out.println("CompletableFuture: " + f.get());
  }

  /*
  java.util.concurrent Utilities:
  CountDownLatch: 1/N Threads Chờ N Tasks Hoàn Thành (One-Time Synchronization)
  + Initialize Với Count = N
  + Mỗi Task Done → countDown() - 1
  + Count = 0 → Tất Cả Threads Đang await() Tiếp Tục Chạy
  + Chỉ Giảm Count (Ko Reset Dc) → Non-Reusable
  Use Cases:
  + Main Thread Chờ N Worker Threads
  + Chờ N Microservices/API Calls Hoàn Thành
  + Integration Testing (Đồng Bộ N Threads)

  CyclicBarrier: N Threads Chờ Nhau Tại 1 Điểm Đồng Bộ (Two-Way Synchronization)
  + Initialize Với Parties = N
  + Mỗi Thread await() Tại Barrier
  + Đủ N Threads → Barrier Mở → Tất Cả Tiếp Tục
  + Barrier Tự Reset → Reusable (Cyclic)
  + Có Thể Chạy Barrier Action Trc Khi Mở Barrier
  Use Cases:
  + Parallel Algorithms
  + Multi-Phase Computation
  + Batch Processing Theo Từng Round

  Semaphore: Giới Hạn Số Threads Dc Access Resource Cùng Lúc
  + Initialize Với N Permits
  + acquire() → Xin Permit (Hết Permit → Block)
  + release() → Trả Permit
  + Binary Semaphore (1 Permit) ~ Mutex
  + Counting Semaphore (>1 Permits) → Rate Limiting | Resource Pool
  Use Cases:
  + DB Connection Pool
  + API Rate Limiting
  + Giới Hạn Concurrent Downloads

  BlockingQueue:
  + Thread-Safe Queue (Producer - Consumer Pattern)
  + put() → Queue Full → Block Producer
  + take() → Queue Empty → Block Consumer
  + Internal Synchronization (Ko Cần synchronized Bên Ngoài)
  + Implementations: ArrayBlockingQueue (Bounded), LinkedBlockingQueue (Optional Bounded), PriorityBlockingQueue, DelayQueue
  Use Cases:
  + Message Queue
  + Logging Pipeline
  + Task Scheduling

  AtomicInteger:
  + Thread-Safe Integer (Lock-Free)
  + Methods: incrementAndGet(), decrementAndGet(), compareAndSet(expect, update)
  + Backed Bằng CAS + CPU Atomic Instructions
  + Retry (Spin) Nếu CAS Fail
  + Ko Block Threads (Non-Blocking)
  Use Cases:
  + Request Counter
  + Sequence Generator
  + Metrics / Statistics

  AtomicInteger vs synchronized:
  + AtomicInteger: Lock-Free + CAS & Retry + Tốt Cho Single Variable
  + synchronized: Lock-Based + Block Threads + Dùng Cho Critical Section | Nhiều Shared Variables

  Memory Visibility: Atomic Classes Đảm Bảo Visibility + Atomicity → Internally Dùng volatile + CAS
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
  Virtual Threads (Java 21+):
  Platform Threads: Java Thread Scale 1:1 Với OS|Kernel Thread + Every Thread Có Native Stack (Def ~1MB) + Context Switch (OS Thread Switch) Do OS Scheduler Quản Lý
  + 1 Task Giữ 1 Platform Thread Từ Đầu Đến Cuối (Ko Thể Unmount)
  + Blocking I/O → OS Thread Bị Block Cho Đến Khi I/O Hoàn Thành
  + Tạo Quá Nhiều Platform Threads → Tốn RAM (Stack) + Context Switching Cost + Scheduler Overhead

  Virtual Threads: JVM-Managed Threads (Ko Mapping 1:1 Với OS Thread) + Lightweight + Stack Frames|Continuation Chủ Yếu Lưu Trên Heap
  + 1 Logical Task = 1 Virtual Thread
  + Tạo Hàng Triệu Virtual Threads Với Memory Overhead Rất Thấp

  Carrier Threads: Bản Chất Là Platform Threads (1:1 OS Thread) Đóng Vai Trò Worker Execute Virtual Threads
  + JVM Scheduler Tạo Số Carrier Threads Xấp Xỉ Available Processors (jdk.virtualThreadScheduler.parallelism)
  + 1 Carrier Thread Mount/Unmount & Execute Rất Nhiều Virtual Threads Khác Nhau

  Mount|Unmount Mechanism: VT Ready → JVM Mount Lên Carrier Thread + Gặp Loom-Friendly Blocking I/O → JVM Capture Continuation Về Heap & Unmount VT
  + Carrier Thread Dc Trả Về Scheduler Để Execute Virtual Thread Khác
  + Khi I/O Hoàn Thành → JVM Mount Lại VT Lên Carrier Thread Rảnh (Ko Nhất Thiết Carrier Cũ)
  + Toàn Bộ Quá Trình Transparent Với Dev

  Scheduler: JVM Scheduler Quản Lý Virtual Threads + OS Scheduler Quản Lý Carrier Threads Lên CPU
  + Virtual Thread Scheduling (JVM) & Platform Thread Scheduling (OS) Hoạt Động Phối Hợp

  newVirtualThreadPerTaskExecutor(): Executors.newVirtualThreadPerTaskExecutor() Tạo 1 Virtual Thread Cho Mỗi Task
  + Ko Cần Fixed Thread Pool Cho Phần Lớn I/O-Bound Workloads
  + Thread Pool Thực Tế Bên Dưới Chỉ Còn Là Carrier Threads

  Pinning: VT Ko Thể Unmount Khỏi Carrier Thread → Carrier Thread Bị Giữ Cứng (Pinned) → Giảm Khả Năng Concurrent
  + Nguyên Nhân Thường Gặp: synchronized + Blocking I/O | Native Methods (JNI) | Foreign Function & Memory API (FFM/Panama)
  + Pinning Ko Phải Deadlock Nhưng Làm Mất Lợi Thế Của Virtual Threads

  synchronized: Intrinsic Monitor Lock (Obj Monitor - JVM|Native Implementation)
  + Ko Phải Lúc Nào Cũng Xấu - Chỉ Đáng Lo Khi Giữ Monitor Trong Lúc Blocking I/O → Gây Pin Carrier Thread

  ReentrantLock: Java-Level Lock (Backed Bằng AQS - AbstractQueuedSynchronizer)
  + Hỗ Trợ Linh Hoạt Hơn synchronized
  + Lựa Chọn Tốt Hơn Nếu Critical Section Có Khả Năng Blocking

  CPU-Bound Workloads: Platform Threads (Recommended)
  + Heavy Computation (Compression, Encryption, Image Processing...) Nên Dùng Fixed Thread Pool ≈ Số CPU Cores
  + Virtual Threads Vẫn Chạy Dc Nhưng Ko Mang Lại Lợi Ích Về Performance Vì CPU Cuối Cùng Vẫn Chỉ Thực Thi Trên Số Carrier Threads ≈ Số CPU Cores
  + Virtual Threads Còn Có Thêm 1 Lớp Scheduling|Mapping (Virtual Thread → Carrier Thread) Tạo Thêm Overhead Mà Ko Mang Lại Lợi Ích Về Concurrency

  I/O-Bound Workloads: Virtual Threads + Carrier Threads (Recommended)
  + Phù Hợp Cho Spring Boot REST APIs | JDBC/DB Calls | HTTP Clients | File I/O | Socket | Kafka/RabbitMQ Consumers Vì Phần Lớn Thời Gian Thread Chỉ Waiting
  + Khi Virtual Thread Block I/O → JVM Park Virtual Thread & Giải Phóng Carrier Thread Để Chạy Task Khác
  + Giúp Scale Lên Hàng Triệu Concurrent Tasks Với Ít OS Threads Hơn

  Structured Concurrency (Java 21 Preview → Java 25 Standard): Quản Lý Nhóm Virtual Threads Theo Scope
  + Chuyển Flat Peer-To-Peer Model → Hierarchical Parent-Child Model
  + Child Tasks Cùng Lifecycle → Hủy Đồng Loạt Khi Parent Hủy
  + Exception Propagation & Cancellation Dễ Quản Lý Hơn CompletableFuture

  Limitations:
  + Ko Thay Thế Non-Blocking Framework (Netty, Reactive...) Trong Mọi Trường Hợp → Virtual Threads Tối Ưu Blocking I/O Chứ Ko Hoàn Toàn Loại Bỏ Blocking Như Các Non-Blocking Framework
  + Ko Giúp CPU-Bound Chạy Nhanh Hơn
  + Ko Loại Bỏ Race Condition | Deadlock | Synchronization Problems
  + Pinning Vẫn Có Thể Làm Mất Hiệu Quả Nếu Design Sai (synchronized, JNI...)

  Best Practices:
  + 1 Request / 1 Task / 1 Virtual Thread
  + Dùng Virtual Threads Cho Blocking I/O Thay Vì Tạo Thread Pools Lớn
  + Tránh synchronized Bao Quanh Blocking I/O + Giữ Critical Section Ngắn
  + CPU-Bound → Ưu Tiên Fixed Thread Pool
  + Monitor Pinning Bằng JDK Flight Recorder Khi Cần
*/
  public static void virtualThreads() throws Exception {
    // Platform vs Virtual Thread
    Thread vt = Thread.ofVirtual().start(() -> System.out.println("Virtual?: " + Thread.currentThread().isVirtual()));
    vt.join();
    try (ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor()) {
      Future<String> f = exec.submit(() -> "Virtual Thread Executor");
      System.out.println(f.get());
    }
  }

  /*
  Deadlock: Trạng Thái (>=2) Threads Bị Block Vĩnh Viễn Chờ Nhau Release Lock
  Điều Kiện (Coffman Conditions):
  + Mutual Exclusion: Resource Ko Thể Share Đồng Thời
  + Hold & Wait: Thread Dg Giữ Resource A & Chờ Resource B
  + No Preemption: Lock Ko Thể Bị Cướp Ép Buộc (Chỉ Release Bởi Thread Dg Giữ)
  + Circular Wait: Thread 1 Chờ Thread 2 & Thread 2 Chờ Thread 1
  Giải Pháp:
  + Lock Ordering: Luôn Lock Theo 1 Thứ Tự Cố Định
  + Lock Timeout (ReentrantLock.tryLock()): Set Timeout - Nếu Hết Thời Gian Ko Lấy Dc Lock → Release All Lock Cũ & Retry
  */
  public static void deadlockPrevention() throws InterruptedException {
    ReentrantLock lockA = new ReentrantLock();
    ReentrantLock lockB = new ReentrantLock();
    Thread t1 = new Thread(() -> {
      try {
        if (lockA.tryLock(50, TimeUnit.MILLISECONDS)) {
          try {
            Thread.sleep(10);
            if (lockB.tryLock(50, TimeUnit.MILLISECONDS)) {
              try {
                System.out.println("T1 Acquired Both Locks - No Deadlock");
              } finally { lockB.unlock(); }
            }
          } finally { lockA.unlock(); }
        }
      } catch (InterruptedException ignored) {}
    });
    Thread t2 = new Thread(() -> {
      try {
        if (lockB.tryLock(50, TimeUnit.MILLISECONDS)) {
          try {
            Thread.sleep(10);
            if (lockA.tryLock(50, TimeUnit.MILLISECONDS)) {
              try {
                System.out.println("T2 Acquired Both Locks - No Deadlock");
              } finally { lockA.unlock(); }
            }
          } finally { lockB.unlock(); }
        }
      } catch (InterruptedException ignored) {}
    });
    t1.start(); t2.start();
    t1.join(); t2.join();
  }

  /*
  ThreadLocal vs ScopedValue (Java 21+ Modern Context Propagation):
  ThreadLocal:
  + Cho Phép Lưu Variable Riêng Cho Từng Thread
  + Risk Memory Leak Trong ThreadPool: Nếu Ko .remove() Sau Khi Dùng → Thread Trả Về Pool Vẫn Giữ Variable Cũ
  + Phí Memory Cho Virtual Threads: Tạo Hàng Triệu Virtual Threads Sẽ Tốn Hàng Triệu ThreadLocal Maps
  ScopedValue (Incubator | Preview Feature):
  + Biến Context Là Immutable & Gắn Trực Tiếp Theo Scope
  + Tự Động Clean Khi Out Scope → Ko Lo Memory Leak
  + Phù Hợp Hoàn Hảo Cho Virtual Threads & Structured Concurrency
  */
  private static final ThreadLocal<String> userContext = new ThreadLocal<>();
  public static void threadLocal() {
    try {
      userContext.set("VAK42");
      System.out.println("ThreadLocal: " + userContext.get());
    } finally {
      userContext.remove(); // Bắt Buộc Clean Tránh Memory Leak Trong Thread Pool
    }
  }

  /*
  ForkJoinPool & Work-Stealing Algorithm:
  Designed Cho Tasks Dạng Divide-And-Conquer (Chia Để Trị)
  Work-Stealing: Mỗi Worker Thread Có 1 Double-Ended Queue (Deque) Riêng
  + Thread Thêm & Lấy Tasks Từ Đầu Queue Của Nó (LIFO)
  + Khi 1 Thread Rảnh → Nó Sẽ Steal Task Từ Cuối Queue Của Threads Khác (FIFO) → Tận Dụng Max Performance Tất Cả Cores
  */
  static class SumTask extends RecursiveTask<Long> {
    private final long start;
    private final long end;
    private static final long THRESHOLD = 1000;
    SumTask(long start, long end) { this.start = start; this.end = end; }
    @Override
    protected Long compute() {
      if ((end - start) <= THRESHOLD) {
        long sum = 0;
        for (long i = start; i <= end; i++) sum += i;
        return sum;
      }
      long mid = (start + end) / 2;
      SumTask left = new SumTask(start, mid);
      SumTask right = new SumTask(mid + 1, end);
      left.fork();
      return right.compute() + left.join();
    }
  }
  public static void forkJoin() {
    try (ForkJoinPool pool = new ForkJoinPool()) {
      long result = pool.invoke(new SumTask(1, 5000));
      System.out.println("ForkJoin Sum: " + result);
    }
  }

  public static void main(String[] args) throws Exception {
    threadSafety();
    executors();
    completableFuture();
    concurrentUtils();
    virtualThreads();
    deadlockPrevention();
    threadLocal();
    forkJoin();
  }
}