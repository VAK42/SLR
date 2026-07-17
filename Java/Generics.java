import java.util.*;
import java.util.function.*;

// Generic: Hoạt Động Với Kiểu Do Caller Chỉ Định
class Pair<TFirst, TSecond> {
  private final TFirst first;
  private final TSecond second;
  public Pair(TFirst first, TSecond second) {
    this.first = first;
    this.second = second;
  }
  public TFirst getFirst() { return first; }
  public TSecond getSecond() { return second; }
  // TResult Tự Sinh + fold: Thao Tác Biến Đổi Các Phần Tử Thành 1 Kết Quả Duy Nhất Thông Qua 1 Hàm Xử Lý (Mapper)
  public <TResult> TResult fold(BiFunction<TFirst, TSecond, TResult> mapper) {
    return mapper.apply(first, second);
  }
  public Pair<TSecond, TFirst> swap() {
    return new Pair<>(second, first);
  }
  @Override
  public String toString() { return first + second; }
}

// Generic Stack
class GenericStack<T> {
  private final List<T> elements = new ArrayList<>();
  public void push(T item) { elements.add(item); }
  public T pop() {
    if (isEmpty()) throw new EmptyStackException();
    return elements.remove(elements.size() - 1);
  }
  public T peek() {
    if (isEmpty()) throw new EmptyStackException();
    return elements.get(elements.size() - 1);
  }
  public boolean isEmpty() { return elements.isEmpty(); }
  public int size() { return elements.size(); }
  @Override
  public String toString() { return elements.toString(); }
}

public class Generics {
  /*
  Generic Methods
  <T extends Comparable<T>>: Khai Báo Kiểu Có Điều Kiện - Bounded Type Parameter
  T: Kiểu Trả Về - Return Type
  */
  public static <T extends Comparable<T>> T max(T a, T b) {
    return a.compareTo(b) >= 0 ? a : b;
  }
  public static <T> List<T> repeat(T item, int times) {
    List<T> result = new ArrayList<>();
    for (int i = 0; i < times; i++) result.add(item);
    return result;
  }
  public static <T extends Comparable<T> & Cloneable> T maxCloneable(T a, T b) {
    return a.compareTo(b) >= 0 ? a : b;
  }

  /*
  Wildcard (?): Integer Là Con Của Number Nhưng List<Integer> Ko Là Con Của List<Number> -> ? Để Đại Diện Cho Kiểu Dữ Liệu Ch Bt
  PECS: Producer Extends - Consumer Super
  ? extends Number: Producer - Read-Only - Chấp Nhận List Của Bất Kỳ Lớp Con Nào Của Number + Cấm Ghi
  ? super Number: Consumer - Write-Only - Chấp Nhận List Của Bất Kỳ Lớp Cha Nào Của Number + Cho Đọc Nhưng Ko An Toàn
  <T> Thay Cho ? Khi Cần Đọc & Ghi
  */
  public static double sum(List<? extends Number> numbers) {
    double total = 0;
    for (Number num : numbers) {
      total += num.doubleValue(); // Chỉ Đọc — Không Thể add() Vào
    }
    return total;
  }
  public static void copyInto(List<? super Integer> dest, List<Integer> src) {
    dest.addAll(src); // Chỉ Ghi — Đọc Ra Chỉ Được Object
  }
  public static <T> void swap(List<T> list, int i, int j) {
    T temp = list.get(i);
    list.set(i, list.get(j));
    list.set(j, temp);
  }

  // Type Erasure — Kiểm Tra Lúc Compile + Biến Mất Lúc Runtime
  public static void typeErasure() {
    List<String> strings = new ArrayList<>();
    List<Integer> integers = new ArrayList<>();
    // Cùng Class Lúc Runtime Vì Erasure
    System.out.println(strings.getClass() == integers.getClass()); // True
    // instanceof Chỉ Kiểm Tra Raw Type
    System.out.println(strings instanceof List<?>);
    // Vì Ở Runtime JVM Ko Hề Bt Kiểu T Hay String Trong List Là Gì -> Ko Thể Dùng instanceof Để Kiểm Tra Kiểu Cụ Thể + Ko Thể new Để Initialize Object
    @SuppressWarnings("rawtypes")
    List raw = strings;
    @SuppressWarnings("unchecked")
    var _ = raw.add(42); // Compiler Cảnh Báo Nhưng Cho Phép
    // ClassCastException Xảy Ra Khi Đọc
    try {
      String value = strings.get(0);
      // String value = (String) strings.get(0); -> Element 0: 42 (Integer) -> Failed! Java Chỉ Cast Giữa Các Lớp Cha - Con + Ko Phải Anh - Em
    } catch (ClassCastException e) {
      System.out.println("ClassCastException Do Erasure: " + e.getMessage());
    }
  }

  // Bounded Type Parameters — Nhiều Ràng Buộc
  interface Printable { void print(); }
  interface Saveable { String save(); }
  static class Document implements Printable, Saveable, Comparable<Document> {
    private final String content;
    Document(String content) { this.content = content; }
    @Override public void print() { System.out.println("TL: " + content); }
    @Override public String save() { return "{content:" + content + "}"; }
    @Override public int compareTo(Document other) {
      return this.content.compareTo(other.content);
    }
  }
  public static <T extends Printable & Saveable> void processDocument(T item) {
    item.print();
    System.out.println("Saved: " + item.save());
  }

  public static void main(String[] args) {
    // Generic Class
    Pair<String, Integer> pair = new Pair<>("Alice", 30);
    System.out.println("Pair: " + pair);
    System.out.println("Fold: " + pair.fold((name, age) -> name + age));
    System.out.println("Swap: " + pair.swap());
    // Generic Stack
    GenericStack<String> stack = new GenericStack<>();
    stack.push("Alpha");
    stack.push("Beta");
    stack.push("Gamma");
    System.out.println("Stack: " + stack);
    System.out.println("Pop: " + stack.pop());
    System.out.println("Peek: " + stack.peek());
    // Generic Methods
    System.out.println(max(10, 20));
    System.out.println(max("Apple", "Banana"));
    System.out.println(repeat("VAK", 4));
    // Wildcards & PECS
    List<Integer> ints = Arrays.asList(1, 2, 3);
    List<Double> doubles = Arrays.asList(1.5, 2.5, 3.5);
    System.out.println(sum(ints));    // List<Integer> OK
    System.out.println(sum(doubles)); // List<Double> OK
    List<Number> numbers = new ArrayList<>();
    copyInto(numbers, new ArrayList<>(ints)); // List<Number> OK (? super Integer)
    System.out.println(numbers);
    List<String> swapList = new ArrayList<>(Arrays.asList("A", "B", "C"));
    swap(swapList, 0, 2);
    System.out.println(swapList);
    // Type Erasure
    typeErasure();
    // Bounded Type Parameters
    processDocument(new Document("VAK42"));
  }
}