/*
useState: Lưu Trữ State Nội Bộ Của Component + Trigger Re-render Khi Thay Đổi
useEffect: Chạy Side Effects Sau Khi DOM Đã Được Render
  - Dependency Array []: Chỉ Chạy 1 Lần Sau Lần Render Đầu Tiên
  - Dependency Array [a, b]: Chạy Lại Mỗi Khi a Hoặc b Thay Đổi
  - Không Có Dependency Array: Chạy Lại Sau Mỗi Lần Re-render
  - Cleanup Function (return () => ...): Chạy Trước Effect Tiếp Theo & Lúc Unmount -> Hủy Timer, Subscription
useRef: Lưu Trữ Giá Trị Mutable Mà Không Trigger Re-render + Truy Cập DOM Element Trực Tiếp
useMemo: Cache Kết Quả Tính Toán Nặng -> Chỉ Tính Lại Khi Dependencies Thay Đổi
useCallback: Cache Reference Hàm -> Tránh Re-render Thừa Cho Child Component Bọc Bằng React.memo
useContext: Đọc Giá Trị Từ Context Provider Gần Nhất Phía Trên Trong Cây Component
useReducer: Thay Thế useState Khi State Logic Phức Tạp Hoặc Có Nhiều Sub-Values Liên Quan
React.memo: HOC Bọc Component -> Bỏ Qua Re-render Nếu Props Không Thay Đổi (Shallow Compare)
*/
import { useState, useEffect, useRef, useMemo, useCallback, createContext, memo } from "react"
import { persist, devtools } from "zustand/middleware"
import { create } from "zustand"

// useState
function Counter() {
  const [count, setCount] = useState(0)
  const [user, setUser] = useState({ name: "VAK", age: 20 })
  return (
    <div>
      <button onClick={() => setCount(prev => prev + 1)}>+1</button>
      <button onClick={() => setUser(prev => ({ ...prev, age: prev.age + 1 }))}>Age+</button>
    </div>
  )
}

// useEffect
function Timer({ userId }) {
  const [seconds, setSeconds] = useState(0)
  const [data, setData] = useState(null)
  // Interval + Cleanup Khi Unmount
  useEffect(() => {
    const id = setInterval(() => setSeconds(prev => prev + 1), 1000)
    /*
    setInterval: Cứ Sau Đúng x ms -> Tự Động Kích Hoạt + Chạy Hàm Callback Đc Truyền Vào Ở Đối Số Thứ Nhất

    () => setSeconds(seconds + 1)
    Lần Render 1 -> Tạo Execution Context 1 -> useState Khởi Tạo seconds=0 + useEffect Chạy & Đăng Ký Callback -> Callback Liên Kết Với Execution Context Này
    Lần Render 2 -> Tạo Execution Context 2 -> useState Khởi Tạo seconds=1 + useEffect Ko Chạy & Ko Đăng Ký Callback
    x ms Trôi Qua -> Callback Cũ Đc Gọi -> Tìm Thấy seconds=0 Do Callback Này Đc Liên Kết Với Execution Context 1
    
    () => setSeconds(prev => prev + 1)
    prev Ko Bị Giới Hạn + Ko Phụ Thuộc Vào Execution Context Nào Cả + Đc React Lưu Trữ Ở Kho State Riêng Độc Lập & Cập Nhật Giá Trị Mới Nhất
    x ms Trôi Qua -> Callback Cũ Đc Gọi -> Tìm prev
    */
    return () => clearInterval(id) // Dừng & Xóa Vĩnh Viễn -> Liên Kết Giữa Browser & Callback Bị Đứt -> Callback & Execution Context Chứa Nó Ko Còn Path Để Truy Cập -> Thu Hồi Bộ Nhớ (Garbage Collected)
  }, [])
  // Fetch + Abort Khi userId Thay Đổi Hoặc Unmount
  useEffect(() => {
    let aborted = false
    fetch(`/api/users/${userId}`)
      .then(r => r.json())
      .then(json => { if (!aborted) setData(json) })
    return () => { aborted = true }
    /*
    Trường Hợp Khi Gọi fetch userId=1 Nhưng Chưa Xử Lý Xong Đã Gọi fetch userId=2 -> Cleanup Đc Chạy Cho API 1
    Nhận Dc Kết Quả Từ Cả 2 Nhưng API 1 Aborted -> Chỉ Trả Kết Quả Của API 2
    */
  }, [userId])
  return <div>Time: {seconds}s | User: {data?.name}</div>
}

// useRef
function FocusInput() {
  const inputRef = useRef(null)
  const prevCount = useRef(0)
  const [count, setCount] = useState(0)
  useEffect(() => { prevCount.current = count }, [count])
  /*
  Click Focus -> Cursor Sẽ Focus Vào Input Field -> Thay Đổi State Nhưng Ko Re-render Lần Nx
  Click + -> count Thay Đổi -> Re-render -> useEffect Cập Nhật prevCount Nhưng Ko Re-render Lần Nx
  */
  return (
    <div>
      <input ref={inputRef} />
      <button onClick={() => inputRef.current.focus()}>Focus</button>
      <p>Prev: {prevCount.current} | Now: {count}</p>
      <button onClick={() => setCount(c => c + 1)}>+</button>
    </div>
  )
}

// useMemo + useCallback + React.memo
const ExpensiveList = memo(function ExpensiveList({ items, onDelete }) {
  // Chỉ Re-render Khi items Hoặc onDelete Thay Đổi Reference
  return <ul>{items.map(i => <li key={i.id}>{i.name} <button onClick={() => onDelete(i.id)}>X</button></li>)}</ul>
})
function Parent() {
  const [query, setQuery] = useState("")
  const [items] = useState([{ id: 1, name: "A" }, { id: 2, name: "B" }])
  // useMemo: Cache Kết Quả Filter Mảng - Không Tính Lại Khi query Không Đổi -> Tối Ưu Việc Tính Toán Nặng
  const filtered = useMemo(
    () => items.filter(i => i.name.toLowerCase().includes(query)),
    [items, query]
  )
  // useCallback: Cache Reference Hàm - Không Tạo Hàm Mới Mỗi Lần Re-render -> Tối Ưu Việc Re-render Component Con
  const handleDelete = useCallback((id) => {
    console.log("Remove?", id)
  }, [])
  return (
    <div>
      <input value={query} onChange={e => setQuery(e.target.value)} />
      <ExpensiveList items={filtered} onDelete={handleDelete} />
    </div>
  )
}

// useContext + createContext
const ThemeContext = createContext("light")
function ThemedButton() {
  const theme = useContext(ThemeContext)
  return <button style={{ background: theme === "dark" ? "#000" : "#fff" }}>Button</button>
}
/*
useContext Giúp Truyền props Xuyên Nhiều Components Mà Ko Cần Nhận/Truyền Liên Tục Giữa Các Components
Nếu Có Nhiều Provider Bọc Đè Lên Nhau Thì Lấy Giá Trị Từ Provider Gần Nhất
Nằm Ngoài Provider Sẽ Tự Động Dùng Giá Trị Đc Khai Báo Ban Đầu Ở createContext()
value Của Provider Thay Đổi -> Tất Cả Các Component Đang Dùng useContext Đó Sẽ Bắt Buộc Phải Re-render
*/
function App() {
  return (
    <ThemeContext.Provider value="dark">
      <ThemedButton />
    </ThemeContext.Provider>
  )
}

// useReducer
const cartReducer = (state, action) => {
  switch (action.type) {
    case "add":
      const existing = state.find(i => i.id === action.item.id)
      if (existing) return state.map(i => i.id === action.item.id ? { ...i, qty: i.qty + 1 } : i)
      return [...state, { ...action.item, qty: 1 }]
    case "remove":
      return state.filter(i => i.id !== action.id)
    case "clear":
      return []
    default:
      return state
  }
}
function Cart() {
  const [items, dispatch] = useReducer(cartReducer, [])
  return (
    <div>
      <button onClick={() => dispatch({ type: "add", item: { id: 1, name: "Book" } })}>Add</button>
      <button onClick={() => dispatch({ type: "remove", id: 1 })}>Remove</button>
      <button onClick={() => dispatch({ type: "clear" })}>Clear</button>
      <pre>{JSON.stringify(items, null, 2)}</pre>
    </div>
  )
}

// Zustand
const useCountStore = create(
  devtools(
    persist(
      (set, get) => ({
        count: 0,
        items: [],
        increment: () => set(s => ({ count: s.count + 1 })),
        addItem: (item) => set(s => ({ items: [...s.items, item] })),
        removeItem: (id) => set(s => ({ items: s.items.filter(i => i.id !== id) })),
        getTotal: () => get().items.length,
        reset: () => set({ count: 0, items: [] })
      }),
      { name: "countStorage" }
    ),
    { name: "CountStore" }
  )
)
function ZustandCounter() {
  // Selector: Chỉ Re-render Khi count Thay Đổi (Không Re-render Khi items Thay Đổi)
  const count = useCountStore(s => s.count)
  const increment = useCountStore(s => s.increment)
  const reset = useCountStore(s => s.reset)
  return (
    <div>
      <p>Count: {count}</p>
      <button onClick={increment}>+1</button>
      <button onClick={reset}>Reset</button>
    </div>
  )
}
function ZustandItems() {
  // Selector: Chỉ Re-render Khi items Thay Đổi (Không Re-render Khi count Thay Đổi)
  const items = useCountStore(s => s.items)
  const addItem = useCountStore(s => s.addItem)
  const removeItem = useCountStore(s => s.removeItem)
  return (
    <div>
      <button onClick={() => addItem({ id: Date.now(), name: "Item" })}>Add Item</button>
      <ul>
        {items.map(i => (
          <li key={i.id}>{i.name} <button onClick={() => removeItem(i.id)}>X</button></li>
        ))}
      </ul>
    </div>
  )
}