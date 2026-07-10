/*
app/: Hệ Thống Route Dựa Trên Cấu Trúc Thư Mục (File-System Based Router)
layout.tsx: Giao Diện Chung Bền Vững (Persistent Shell), Không Bị Unmount Khi Chuyển Route
template.tsx: Tương Tự layout.tsx Nhưng Sẽ Bị Remount (Khởi Tạo Lại Toàn Bộ State/Effect) Mỗi Khi Chuyển Route
page.tsx: Giao Diện Riêng Biệt Cho Từng Route (Unique UI)
loading.tsx: Giao Diện Chờ Tạm Thời (Instant Suspense Fallback) Khi Trang Đang Tải Dữ Liệu
error.tsx: Bộ Lọc Lỗi (Error Boundary) Cho Riêng Phân Đoạn (Segment) Gặp Sự Cố
(group): Nhóm Các Route Lại Với Nhau Để Tiện Quản Lý Mà Không Làm Thay Đổi Đường Dẫn URL
[id]: Tham Số Đường Dẫn Động (Dynamic Route Segment)
[...slug]: Khớp Tất Cả Các Đường Dẫn Phía Sau (Catch-All Route Segment)
[[...slug]]: Catch-All Nhưng Không Bắt Buộc (Optional Catch-All)

Server Component (Mặc Định): Fetch Data Trực Tiếp + Không Gửi Mã Nguồn JS Về Trình Duyệt + Không Thể Dùng useState/useEffect/Browser APIs
Client Component ("use client"): Component Chạy Trên Trình Duyệt + Dùng useState/useEffect + Sử Dụng Được Các Web APIs
Props Server -> Client: Bắt Buộc Phải Định Dạng Được (Serializable) & Không Truyền Được Function/Class Instance
Quy Tắc Vận Hành: Giữ Client Components Ở Mức Lá Cuối Cùng (Leaf Nodes) Để Giảm Kích Thước JS Bundle Cho Trình Duyệt

cache: "force-cache" (Mặc Định): Lưu Cache Vô Hạn (Tương Đương SSG - Static Site Generation)
cache: "no-store": Không Lưu Cache, Luôn Lấy Dữ Liệu Mới Mỗi Lần Request (Tương Đương SSR - Server-Side Rendering)
next: { revalidate: 3600 }: Tự Động Làm Mới Cache Sau Mỗi Khoảng Thời Gian (Tương Đương ISR - Incremental Static Regeneration)
next: { tags: ["name"] }: Đặt Nhãn Tag Cho Cache Để Kích Hoạt Làm Mới Cache Chủ Động (On-Demand Revalidation)
Promise.all: Thực Hiện Gọi Nhiều API Song Song Để Tối Ưu Tốc Độ Load Page
unstable_cache: Cache Cho Bất Kỳ Hàm Bất Đồng Bộ Nào
Request Deduplication: Tự Động Gộp Các Request fetch Trùng URL Trong Cùng Một Vòng Đời Request

"use server": Async Functions Chạy Trực Tiếp Trên Server & Có Thể Được Gọi Từ Phía Client
No API Routes: Thực Hiện Gửi Dữ Liệu/Thao Tác DB Trực Tiếp Mà Không Cần Định Nghĩa Endpoint API Riêng
useActionState: Hook Phía Client Giúp Theo Dõi Kết Quả Trả Về & Trạng Thái Loading Của Action
useFormStatus: Hook Đọc Trạng Thái Đang Gửi Form Để Disable Nút Submit
revalidatePath / revalidateTag: Xóa Cache Cũ & Làm Mới Giao Diện Ngay Lập Tức Sau Khi Có Thao Tác Thay Đổi Dữ Liệu

generateStaticParams: Pre-render Trước Các Trang Dynamic Route Từ Lúc Build
dynamicParams = true (Mặc Định): Cho Phép Tự Động Sinh Trang Mới Ở Runtime Nếu ID Chưa Được Pre-render Lúc Build
generateMetadata: Hàm Sinh Metadata SEO (Title, Description, OpenGraph) Động Tương Ứng Với Params Của Route

GET, POST, PUT, DELETE, PATCH: Các Hàm HTTP Method Được Export Ra Để Xử Lý Request Tương Ứng
NextRequest: Lớp Kế Thừa Web Request API Mặc Định + Các Tiện Ích Check Cookies, SearchParams
NextResponse: Đối Tượng Hỗ Trợ Trả Về JSON + Redirect + Thiết Lập Cookies
export const dynamic = "force-dynamic": Cấu Hình Ép Route API Luôn Luôn Chạy Động - Không Lưu Cache
Tự Động Chuyển Sang Dynamic: Nếu Sử Dụng Các Hàm cookies(), headers()... + Đọc searchParams

middleware.ts: Chạy Trước Khi Request Đi Vào Route Handler/Page + Hoạt Động Trên Edge Runtime
matcher: Cấu Hình Khớp Đường Dẫn Chỉ Định Để Kích Hoạt Chạy Middleware
NextResponse.redirect: Chuyển Hướng Người Dùng Sang URL Mới
NextResponse.rewrite: Trả Về Nội Dung Từ URL Khác Nhưng Giữ Nguyên URL Hiện Tại Của Trình Duyệt

next/image: Tự Động Định Dạng WebP/AVIF + Lazy Load Ảnh + Ngăn Ngừa Hiện Tượng Lệch Giao Diện
next/font: Tự Động Self-Host Fonts Từ Lúc Build + Tránh Lỗi FOUT - Flash Of Unstyled Text + Không Cần Tải Font Từ Google CDN
next/dynamic: Phân Tách Code -> Tải Component Khi Cần Thiết Để Giảm Kích Thước Bundle Ban Đầu
priority: Thiết Lập Cho Ảnh LCP - Largest Contentful Paint -> Để Load Trước Ngay Lập Tức -> Tắt Lazy Load
ssr: false: Cấu Hình Component Client-Only + Bỏ Qua SSR Cho Các Thư Viện Vẽ Biểu Đồ, Bản Đồ
*/