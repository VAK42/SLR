import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;
import java.util.function.*;

/*
List: Cần Thứ Tự → Cho Trùng Lặp
Set: Ko Cần Thứ Tự → Cấm Trùng Lặp
Map: Lưu Dạng Cặp Khóa - Giá Trị → Khóa Ko Dc Trùng
*/
public class CollectionsStreams {
  // ArrayList | LinkedList | Immutable List
  public static void lists() {
    /*
    ArrayList: Iteration + Search + Add (Thêm Cuối)
    LinkedList: Queue + Stack + Deque (Thao Tác Đầu/Cuối)
    */
    List<String> list = new ArrayList<>(List.of("Alpha", "Beta"));
    list.add("Gamma");
    System.out.println("ArrayList: " + list + " | Get(0): " + list.get(0));

    // Immutable List: List.of() Ko Cho Phép Chỉnh Sửa (Java 9+)
    List<String> immutable = List.of("A", "B");
    System.out.println("Immutable: " + immutable);
  }

  // HashSet | LinkedHashSet | TreeSet & Operations
  public static void sets() {
    /*
    HashSet: Ko Giữ Thứ Tự - Hash Map
    LinkedHashSet: Giữ Thứ Tự Chèn - Hash Map + Doubly Linked List
    TreeSet: Tự Động Sắp Xếp - Red-Black Tree
    */
    Set<String> hashSet = new HashSet<>(List.of("banana", "apple", "cherry"));
    Set<String> linkedSet = new LinkedHashSet<>(List.of("banana", "apple", "cherry"));
    Set<String> treeSet = new TreeSet<>(List.of("banana", "apple", "cherry"));
    System.out.println(hashSet);
    System.out.println(linkedSet);
    System.out.println(treeSet);

    // Set Operations: retainAll (Giao/Intersection) | addAll (Hợp/Union) | removeAll (Hiệu/Difference)
    Set<Integer> a = new HashSet<>(List.of(1, 2, 3));
    Set<Integer> b = new HashSet<>(List.of(2, 3, 4));
    Set<Integer> intersection = new HashSet<>(a);
    intersection.retainAll(b);
    System.out.println("Intersection: " + intersection);
  }

  /*
  HashMap: Key-Value Map + Backed Bằng Array<Node<K,V>> (Buckets)
  HashMap Internals: Default Capacity = 16 (Power Of 2) + Default Load Factor = 0.75
  - Bucket Index = (n - 1) & hash (& Nhanh Hơn %)
  - Collision → Chaining (Linked List)
  - Size > Capacity * LoadFactor → Resize (Capacity * 2) + Rehash Tất Cả Entries

  Treeification:
  - Bucket Count < 64 (MIN_TREEIFY_CAPACITY): Resize Trc (Phân Tán Lại Hash) Thay Vì Treeify
  - Bucket Count >= 64
    + Node >= 8 (TREEIFY_THRESHOLD): Linked List → Red-Black Tree
    + Node <= 6 (UNTREEIFY_THRESHOLD): Red-Black Tree → Linked List
    → Dùng 8 & 6 (Ko Phải 8 & 7) Để Tránh Chuyển Đổi Liên Tục (Hysteresis)

  Resize: Capacity * 2 → Recalculate Bucket Index → Hash Ko Đổi - Chỉ Bucket Index Thay Đổi
  Hash Collision: 2 Keys Khác Nhau Nhưng Sau Hash → Cùng Bucket → equals(): Xác Định Có Phải Cùng Key Hay Ko

  hashCode() → Xác Định Bucket
  equals() → So Sánh Chính Xác Key Trong Bucket
  → Override equals() Phải Override hashCode()
  */
  public static void maps() {
    /*
    HashMap: Ko Giữ Thứ Tự - Hash Table
    LinkedHashMap: Giữ Thứ Tự Chèn - Hash Table + Doubly Linked List
    TreeMap: Tự Động Sắp Xếp Key - Red-Black Tree
    */
    Map<String, Integer> map = new HashMap<>();
    map.put("Alice", 90);
    map.putIfAbsent("Alice", 99);
    map.merge("Alice", 10, Integer::sum);
    System.out.println("HashMap: " + map);
    Map<String, Integer> treeMap = new TreeMap<>(map);
    System.out.println("TreeMap: " + treeMap);
  }

  /*
  ConcurrentHashMap: Allow Nhiều Thread Read/Write Đồng Thời An Toàn → Tối Ưu Cho High-Concurrency
  - Java 7: Segment Locking: Chia Map Thành Nhiều Segment (16) - Mỗi Segment Có Lock Riêng → Giảm Contention Vì Ko Lock Toàn Bộ Map
  - Java 8+: Node + CAS + synchronized Bucket Lock: Dùng CAS Khi Bucket Trống + synchronized Lock Bucket Khi Có Collision → Fine-Grained Locking + Tăng Khả Năng Concurrent Access
  - Cấm Null Key & Null Value: Vì null Gây Ambiguity → Ko Differentiate Dc Key Ko Tồn Tại Hay Key Tồn Tại Nhưng Value = null
  */
  public static void concurrentHashMap() {
    ConcurrentMap<String, Integer> concurrentMap = new ConcurrentHashMap<>();
    concurrentMap.put("K1", 100);
    concurrentMap.putIfAbsent("K2", 200);
    concurrentMap.computeIfPresent("K1", (k, v) -> v + 50);
    System.out.println("ConcurrentHashMap: " + concurrentMap);
  }

  // ArrayDeque, PriorityQueue
  public static void queues() {
    // ArrayDeque: Preferred Stack (LIFO) & Queue (FIFO) - Tốc Độ Nhanh Hơn LinkedList & Stack Cũ
    Deque<String> deque = new ArrayDeque<>();
    deque.offer("Alpha"); // FIFO
    deque.offer("Beta");
    System.out.println("Queue Poll: " + deque.poll()); // Alpha

    // PriorityQueue: Heap Tự Sắp Xếp + Default Min Heap (Nhỏ Nhất Ra Trước) + Dùng Comparator.reverseOrder() Cho Max Heap
    PriorityQueue<Integer> minHeap = new PriorityQueue<>(List.of(30, 10, 20));
    System.out.println("Min Heap Poll: " + minHeap.poll()); // 10
  }

  // StreamAPI: Source → Intermediate Ops (Lazy) → Terminal Op
  record Employee(String name, String dept, double salary) {}
  public static void streams() {
    List<Employee> employees = List.of(
      new Employee("Alice", "Eng", 95000),
      new Employee("Bob", "Mkt", 75000),
      new Employee("Charlie", "Eng", 105000)
    );

    // Pipeline: filter (Lọc) → sorted (Sắp Xếp) → map (Biến Đổi) → collect (Gộp)
    List<String> engNames = employees.stream()
      .filter(e -> e.dept().equals("Eng"))
      .sorted(Comparator.comparingDouble(Employee::salary).reversed())
      .map(Employee::name)
      .collect(Collectors.toList());
    System.out.println("Sort: " + engNames);

    // flatMap: Làm Phẳng Nested Collections
    List<List<Integer>> nested = List.of(List.of(1, 2), List.of(3, 4));
    List<Integer> flat = nested.stream().flatMap(Collection::stream).toList();
    System.out.println("FlatMap: " + flat);

    // reduce: Gộp Các Phầm Tử Thành 1 Giá Trị
    int sum = Stream.of(1, 2, 3, 4).reduce(0, Integer::sum);
    System.out.println("Sum: " + sum);

    // Collectors: groupingBy (Nhóm), partitioningBy (Phân Tách Theo True/False), joining (Nối Chuỗi)
    Map<String, List<Employee>> grouped = employees.stream().collect(Collectors.groupingBy(Employee::dept));
    System.out.println("Dept: " + grouped.keySet());
  }

  /*
  Optional<T>: Container Biểu Diễn Value Có Thể Null (Avoid NullPointerException + Explicit Null Handling)
  Problem Với Null: null Làm Code Khó Đọc + Dễ Quên Check → Runtime Exception: NullPointerException
  VD: user.getAddress().getCity() → Nếu address = null → NPE

  Optional: Wrapper Object Có Thể Chứa:
  + Value (Present)
  + Empty (Absent)
  → Thay Vì Return null → Return Optional<T>
  → Caller Bắt Buộc Xử Lý Case Ko Có Data

  Create Optional:
  + Optional.of(value): Value Bắt Buộc Non-Null + Nếu value = null → NPE
  + Optional.ofNullable(value): Cho Phép Null + Nếu value = null → Optional.empty()
  + Optional.empty(): Tạo Optional Ko Có Value

  Check Value:
  + isPresent(): KTra Có Value Hay Ko
  + isEmpty() (Java 11+): KTra Ko Có Value

  Get Value:
  + get(): Lấy Value Directly + Nếu Empty → NoSuchElementException | Ko Khuyến Khích Dùng
  + orElse(defaultValue): Return Default Nếu Empty + Default Expression Luôn Được Evaluate
  + orElseGet(() → defaultValue): Lazy Evaluation + Chỉ Tạo Default Khi Empty
  + orElseThrow(): Throw Exception Nếu Empty

  Transform:
  + map(): Transform Value Nếu Present + Optional<T> → Optional<R> | Similar Stream.map()
  + flatMap(): Transform Sang Optional Khác + Flatten → Tránh Optional<Optional<T>> | Similar Stream.flatMap()

  Filter: filter(condition): Giữ Value Nếu Match Condition + Ko Match → Optional.empty()
  Consume:
  + ifPresent(value → action): Execute Action Nếu Có Value
  + ifPresentOrElse(value → action, emptyAction) (Java 9+)
  */
  public static void optional() {
    // Khởi Tạo: Optional.empty(), Optional.of(nonNull), Optional.ofNullable(nullable)
    Optional<String> email = Optional.ofNullable(null); 
    
    // Sử Dụng: ifPresent, orElse (Eager), orElseGet (Lazy - Supplier), orElseThrow
    String finalEmail = email.orElseGet(() -> "noEmail@example.com");
    System.out.println("Email: " + finalEmail);
  }

  // Các Phương Thức Tiện Ích Từ Class Collections
  public static void collectionsUtil() {
    List<Integer> list = new ArrayList<>(List.of(5, 2, 8, 1));
    // Sắp Xếp Tăng Dần
    Collections.sort(list);
    System.out.println("Sorted: " + list);
    // unmodifiableList: Tạo View Chỉ Đọc (Mutate Sẽ Ném UnsupportedOperationException)
    List<Integer> readOnly = Collections.unmodifiableList(list);
    System.out.println("ReadOnly Min Value: " + Collections.min(readOnly));
  }

  public static void main(String[] args) {
    lists();
    sets();
    maps();
    concurrentHashMap();
    queues();
    streams();
    optional();
    collectionsUtil();
  }
}