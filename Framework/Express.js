import express from "express"
const app = express()
// Built-in Middleware
app.use(express.json({ limit: "1mb" }))         // Parse JSON Body
app.use(express.urlencoded({ extended: true })) // Parse Form Data
// Custom Middleware
app.use((req, res, next) => {
  req.requestId = crypto.randomUUID()
  req.startTime = Date.now()
  res.on("finish", () => {
    const duration = Date.now() - req.startTime
    console.log(`${req.method} ${req.path} ${res.statusCode} ${duration}ms [${req.requestId}]`)
  })
  next() // Chuyền Control Sang Middleware Tiếp Theo
  /*
  Nếu Ko Dùng next() Thì Tuy Hàm Đã Chạy Xong Nhưng Socket Mạng (TCP Connection) Vẫn Mở
  Connection Ở Trạng Thái Pending -> Client Đợi Cho Đến Khi Timeout
  */
})

const router = express.Router()
// .param -> Preprocess :userId Cho Tất Cả Routes Có :userId Trong router
router.param("userId", async (req, res, next, id) => {
  const parsedId = parseInt(id)
  if (isNaN(parsedId)) {
    return res.status(400).json({ error: "Invalid UserId" })
  }
  req.userId = parsedId // Đính Kèm Vào req Để Handler Dùng
  next()
})
// .route -> Chaining Methods Trên Cùng Path — Tránh Lặp Lại
router.route("/users/:userId")
  .get(async (req, res, next) => {
    try {
      res.json({ id: req.userId, name: "User" })
    } catch (err) {
      next(err) // Skip Tất Cả Normal Middleware -> Forward Tới Error Handler
    }
  })
  .put(async (req, res, next) => {
    try {
      res.json({ id: req.userId, ...req.body })
    } catch (err) {
      next(err)
    }
  })
  .delete(async (req, res, next) => {
    try {
      res.status(204).send()
    } catch (err) {
      next(err)
    }
  })
router.get("/users", async (req, res) => {
  const { page = "1", limit = "20", search = "" } = req.query
  res.json({ page: parseInt(page), limit: parseInt(limit), search, data: [] })
})
app.use("/api", router) // Mount router Tại /api

// req: Tất Cả Incoming Data (params, query, body, headers, cookies)
// res: Các Hàm Phản Hồi (send, json, status, sendStatus, redirect, cookie, sendFile, end, write)
app.get("/stream", (req, res) => {
  res.setHeader("Content-Type", "text/plain")
  res.setHeader("Transfer-Encoding", "chunked")
  let count = 0
  const timer = setInterval(() => {
    res.write(`Chunk ${count}\n`)
    count++
    if (count >= 5) {
      clearInterval(timer)
      res.end()
    }
  }, 100)
})

// Error Middleware Phải Có 4 Params (err, req, res, next) & Là Middleware Cuối Cùng
class ValidationError extends Error {
  constructor(message, field) {
    // super(): Ủy Quyền Cho Lớp Cha Tạo Ra Đối Tượng this -> Lớp Con Lấy Đối Tượng this Đó Để Đắp Thêm Các Thuộc Tính Của Riêng Mình
    super(message)
    this.name = "ValidationError"
    this.field = field
    this.status = 400
  }
}
// asyncHandler — Wrap Async Route Để Tự Động Forward Error
const asyncHandler = (fn) => (req, res, next) =>
  Promise.resolve(fn(req, res, next)).catch(next)
/*
NodeJS Đơn Luồng -> Chạy JS Main Thread
I/O Tasks Đc Giao Cho OS/libuv -> Đẩy Kết Quả Vào Event Loop Khi Hoàn Thành -> Event Loop Đưa Callback Về JS Main Thread Xử Lý Nốt
JS Runtime Environment: Call Stack + Microtask/Macrotask + Event Loop
Single Thread: Chỉ Vận Hành Call Stack - Nơi Biên Dịch/Chạy Code JS Đồng Bộ -> Thực Thi Duy Nhất 1 Dòng Code Ở Đỉnh Stack Tại 1 Thời Điểm
Queues: Nơi Lưu Trữ Callbacks Chờ Trong RAM -> Không Tự Thực Thi Code + Chỉ Là Danh Sách Xếp Hàng Chờ
Event Loop: Bộ Điều Phối Chạy Liên Tục + Chờ Call Stack Trống -> Đẩy Callbacks Từ Queues Lên Call Stack Để Single Thread Xử Lý Nốt
*/
app.get("/items/:id", asyncHandler(async (req, res) => {
  const id = parseInt(req.params.id)
  if (isNaN(id)) throw new ValidationError("Invalid Id", "id")
  res.json({ id, name: "Item" })
}))
// Error Handler
app.use((err, req, res, next) => {
  const httpStatus = err.status || 500
  const body = {
    error: err.message || "Internal Server Error",
    status: httpStatus
  }
  if (err instanceof ValidationError) {
    body.field = err.field
  }
  res.status(httpStatus).json(body)
})

/*
JWT: Header.Payload.Signature
Header: Chứa Metadata Về Token -> Thường Có alg (HS256, RS256, ...) + typ (JWT)
Payload: Chứa Thông Tin Muốn Lưu Trữ -> Thường Có Thông Tin Người Dùng, Hệ Thống...
Signature: Đảm Bảo Token Ko Bị Chỉnh Sửa Trên Đường Đi - alg(Header (Base64) + Payload (Base64) + Secret Key)
JWT Token Ở Client Hoàn Toàn Giải Mã Đc Do Base64Url Ko Phải Mã Hóa Bảo Mật -> Ko Lưu Thông Tin Nhạy Cảm
Nên Dùng Refresh Token Để Cấp Lại Access Token Mới + Set Access Token Expiration Ngắn Hạn
*/
import jwt from "jsonwebtoken"
const secret = "VAK"
const signAccessToken = (payload) =>
  jwt.sign(payload, secret, { expiresIn: "15m" })
const authMiddleware = (req, res, next) => {
  const header = req.headers.authorization
  if (!header?.startsWith("Bearer ")) {
    return res.status(401).json({ error: "Missing Token" })
  }
  try {
    req.user = jwt.verify(header.slice(7), secret)
    next()
  } catch {
    res.status(401).json({ error: "Invalid Token" })
  }
}
const requireRole = (...roles) => (req, res, next) => {
  if (!roles.includes(req.user?.role)) {
    return res.status(403).json({ error: "Insufficient Permissions" })
  }
  next()
}
app.get("/protected", authMiddleware, requireRole("admin"), (req, res) => {
  res.json({ user: req.user, data: "VAK42" })
})
app.post("/login", (req, res) => {
  const token = signAccessToken({ username: "VAK", role: "user" })
  res.json({ token })
})

app.listen(3000, () => console.log(`OK`))