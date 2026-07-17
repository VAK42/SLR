import java.util.*;
import java.util.stream.*;
import java.util.function.*;

/*
List: Cần Thứ Tự -> Cho Trùng Lặp
Set: Không Cần Thứ Tự -> Cấm Trùng Lặp
Map: Lưu Dạng Cặp Khóa - Giá Trị -> Khóa Không Được Trùng
*/
public class CollectionsStreams {
  // ArrayList, LinkedList, Immutable List
  public static void lists() {
    /*
    ArrayList: Iteration, Search, Add (Thêm Cuối)
    LinkedList: Queue, Stack, Deque (Thao Tác Đầu/Cuối)
    */
    List<String> list = new ArrayList<>(List.of("Alpha", "Beta"));
    list.add("Gamma");
    System.out.println("ArrayList: " + list + " | Get(0): " + list.get(0));

    // Immutable List: List.of() Ko Cho Phép Chỉnh Sửa (Java 9+)
    List<String> immutable = List.of("A", "B");
    System.out.println("Immutable: " + immutable);
  }

  // HashSet, LinkedHashSet, TreeSet & Operations
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

    // Set Operations: retainAll (Giao/Intersection), addAll (Hợp/Union), removeAll (Hiệu/Difference)
    Set<Integer> a = new HashSet<>(List.of(1, 2, 3));
    Set<Integer> b = new HashSet<>(List.of(2, 3, 4));
    Set<Integer> intersection = new HashSet<>(a);
    intersection.retainAll(b);
    System.out.println("Intersection: " + intersection);
  }

  /*
  HashMap, LinkedHashMap, TreeMap
  HashMap Internals: Capacity = 16, Load Factor = 0.75, Chaining Bằng Linked List
  Nếu Buckets <64 -> Phóng To Map (Resize) Để Phân Tán Lại Các Key - Hiệu Quả Hơn Việc Chuyển Sang Tree
  Nếu Buckets >= 64 & Node Trong 1 Bucket >= 8 (TREEIFY_THRESHOLD) -> Chuyển Sang Red-Black Tree
  Nếu Buckets >= 64 & Node Trong 1 Bucket <= 6 (UNTREEIFY_THRESHOLD) -> Chuyển Lại Linked List
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

  // StreamAPI: Source -> Intermediate Ops (Lazy) -> Terminal Op
  record Employee(String name, String dept, double salary) {}
  public static void streams() {
    List<Employee> employees = List.of(
      new Employee("Alice", "Eng", 95000),
      new Employee("Bob", "Mkt", 75000),
      new Employee("Charlie", "Eng", 105000)
    );

    // Pipeline: filter (Lọc) -> sorted (Sắp Xếp) -> map (Biến Đổi) -> collect (Gộp)
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

  // Optional<T>: Container Tránh NullPointerException
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
    queues();
    streams();
    optional();
    collectionsUtil();
  }
}