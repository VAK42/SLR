# FastAPI Xây Trên Starlette ASGI + Pydantic Validation
# uvicorn: ASGI Server Chạy App
from fastapi import FastAPI, Depends, HTTPException, status, BackgroundTasks, Request
from fastapi.security import OAuth2PasswordBearer, OAuth2PasswordRequestForm
from pydantic import BaseModel, Field, model_validator, field_validator
from fastapi.middleware.cors import CORSMiddleware
from typing import Optional, Generator
import asyncio
import time

# Tạo App — Tự Động Sinh OpenAPI Docs Tại /docs
app = FastAPI(title="VAK", version="1.0.0")

# Middleware Bao Bọc Mọi Request/Response
# Last In First Out Với Request + First In First Out Với Response
# CORSMiddleware Phải Đăng Ký Trước Route
app.add_middleware(
  CORSMiddleware,
  allow_origins=["https://frontend.com"],
  allow_credentials=True,
  allow_methods=["*"],
  allow_headers=["*"]
)

@app.middleware("http")
async def timingMiddleware(request: Request, callNext):
  start = time.perf_counter()
  response = await callNext(request)
  duration = time.perf_counter() - start
  response.headers["X-Process-Time"] = str(round(duration * 1000, 2))
  return response

class ItemResponse(BaseModel):
  id: int
  name: str
  price: float

# Path Param: {itemId} | Query Param: includeTax
# /items/42 -> includeTax = False
# /items/42?includeTax=true -> includeTax = True
@app.get("/items/{itemId}", response_model=ItemResponse)
async def getItem(itemId: int, includeTax: bool = False):
  price = 9.99
  if includeTax:
    price *= 1.1
  return ItemResponse(id=itemId, name="API", price=round(price, 2))

@app.post("/items", response_model=ItemResponse, status_code=201)
async def createItem(item: ItemResponse):
  return item

class UserCreate(BaseModel):
  username: str = Field(min_length=3, max_length=50, pattern=r"^[a-zA-Z0-9]+$")
  email: str = Field(pattern=r"^[^@]+@[^@]+\.[^@]+$")
  password: str = Field(min_length=8)
  age: Optional[int] = Field(default=None, ge=0, le=150)

  # Validate Per Field
  @field_validator("username")
  @classmethod
  def validateUsername(cls, value: str) -> str:
    if value.lower() in ("admin", "root"):
      raise ValueError("Reserved Username")
    return value.lower()
  
  # Validate Sau Khi Tất Cả Fields Được Parse
  @model_validator(mode="after")
  def validateModel(self) -> "UserCreate":
    if self.age is not None and self.age < 13 and self.password:
      raise ValueError("Underage Not Permitted")
    return self

class UserResponse(BaseModel):
  username: str
  email: str
  age: Optional[int] = None

# Depends(): Reusable Dependencies
# Dùng return Khi Cần Lấy Giá Trị & Ko Cần Dọn Dẹp
# Dùng yield Khi Cần Quản Lý Vòng Đời - Khai Báo -> Sử Dụng -> Giải Phóng
class Database:
  def query(self, table: str) -> list:
    return [{"id": 1, "table": table}]

  def close(self) -> None:
    pass

def getDb() -> Generator:
  db = Database()
  try:
    yield db
  finally:
    db.close()

def getCurrentUser(token: str = "defaultToken") -> dict:
  if token != "validToken":
    raise HTTPException(
      status_code=status.HTTP_401_UNAUTHORIZED,
      detail="Invalid Token"
    )
  return {"id": 1, "role": "admin"}

# Chaining Dependencies
def requireAdmin(user: dict = Depends(getCurrentUser)) -> dict:
  if user.get("role") != "admin":
    raise HTTPException(
      status_code=status.HTTP_403_FORBIDDEN,
      detail="Admin Only"
    )
  return user

@app.get("/adminData")
async def getAdminData(
  db: Database = Depends(getDb),
  user: dict = Depends(requireAdmin)
):
  return db.query("secretTable")

# async def: Chạy Trên Event Loop & Không Block
async def sendEmail(to: str, subject: str) -> None:
  await asyncio.sleep(0.1)
  print(f"Email Sent To {to}: {subject}")

@app.post("/register/{email}")
async def registerUser(email: str, tasks: BackgroundTasks):
  # BackgroundTasks: Chạy Sau Khi Response Được Gửi
  # Response Trả Về Ngay — Email Đc Gửi Sau Đó
  tasks.add_task(sendEmail, email, "Welcome To Service")
  return {"status": "Registered", "email": email}