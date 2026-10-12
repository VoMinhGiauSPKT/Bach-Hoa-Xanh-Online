# BẢNG THIẾT KẾ CHI TIẾT API ROUTE (`/employee`, `/user` & `/payment`)

> **LƯU Ý:** Để xem tài liệu đặc tả đầy đủ toàn bộ 11 module dành cho Frontend (FE) tích hợp, vui lòng xem tại file: [API_DOCUMENTATION.md](file:///d:/ghichumonhoc/laptrinhweb/project/BachHoaXanhOnline/API_DOCUMENTATION.md).

> **Mô tả:** Tài liệu đặc tả kỹ thuật chi tiết các API Route cho các router `/employee`, `/user` và `/payment` (hệ thống Bách Hóa Xanh Online).

---

## MỤC LỤC & TỔNG QUAN API ROUTES

| STT | Method | Endpoint | Mô tả chức năng | Quyền truy cập (Middleware) |
| :---: | :---: | :--- | :--- | :--- |
| **1.1** | `POST` | `/employee/` | Tạo tài khoản nhân viên mới | `AuthFilter`, `AdminRoleFilter` |
| **1.2** | `GET` | `/employee/` | Lấy danh sách nhân viên (phân trang, lọc, tìm kiếm) | `AuthFilter`, `AdminRoleFilter` |
| **1.3** | `GET` | `/employee/:id` | Xem thông tin chi tiết một nhân viên | `AuthFilter`, `AdminRoleFilter` |
| **1.4** | `PUT` | `/employee/:id` | Admin cập nhật thông tin nhân viên / Đặt lại mật khẩu | `AuthFilter`, `AdminRoleFilter` |
| **1.5** | `PATCH`| `/employee/:id` | Khóa hoặc kích hoạt lại tài khoản nhân viên | `AuthFilter`, `AdminRoleFilter` |
| **2.1** | `GET` | `/user/profile` | Lấy thông tin chi tiết tài khoản đang đăng nhập | `AuthFilter` (Customer / Staff / Admin) |
| **2.2** | `PUT` | `/user/change-password` | Người dùng tự đổi mật khẩu cá nhân | `AuthFilter` |
| **3.1** | `POST` | `/payment/create/:orderId` | Tạo liên kết & Mã QR VietQR thanh toán PayOS | `AuthFilter` (Customer chính chủ / Staff / Admin) |
| **3.2** | `POST` | `/payment/webhook` | Webhook PayOS xác nhận chuyển khoản tự động (IPN) | Public (PayOS Server Webhook) |

---

## 1. ROUTER: `/employee` (Quản lý tài khoản nhân viên - Dành cho ADMIN)

### 1.1. Tạo tài khoản nhân viên mới

- **Endpoint:** `/employee/`
- **Mô tả:** Tạo tài khoản nhân viên (chỉ tài khoản Admin mới có quyền thực hiện)
- **Method:** `POST`

#### Input (`req.body`):
```json
{
  "fullName": "Nguyen Van B",
  "username": "staff_kho",
  "password": "Password123!",
  "phoneNumber": "0987654321",
  "position": "STAFF"
}
```

#### Output (`res.body`):
```json
{
  "status": 201,
  "message": "Tạo nhân viên thành công",
  "data": {
    "employeeId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "fullName": "Nguyen Van B",
    "username": "staff_kho",
    "phoneNumber": "0987654321",
    "position": "STAFF",
    "status": true
  }
}
```

#### Middleware:
- `AuthFilter` (Verify Bearer Access Token)
- `AdminRoleFilter` (Chỉ cho phép role ADMIN)

#### Errors (Unhappy path):
- `400 Bad Request`: Thiếu thông tin bắt buộc (`fullName`, `username`, `password`, `phoneNumber`).
- `401 Unauthorized`: Chưa đăng nhập hoặc token không hợp lệ / hết hạn.
- `403 Forbidden`: Người gọi không phải là ADMIN.
- `409 Conflict`: `username` hoặc `phoneNumber` đã tồn tại.

#### Notes:
- Tự sinh UUID cho `employeeId` (`maNhanVien`).
- Thêm bản ghi vào bảng `NguoiDung` và `NhanVien` kèm theo chức vụ `chucVu` (`'ADMIN'` hoặc `'STAFF'`).
- Mật khẩu được băm bằng BCrypt trước khi INSERT.
- Output tuyệt đối không trả về trường `password`.

- **Packages:** `jsonwebtoken`, `bcrypt`, `uuid`

---

### 1.2. Lấy danh sách nhân viên

- **Endpoint:** `/employee/`
- **Mô tả:** Lấy danh sách nhân viên có phân trang, tìm kiếm và lọc theo trạng thái
- **Method:** `GET`

#### Input:
- **Body:** Không có body
- **Query params:**
  - `page`: number (mặc định `1`)
  - `limit`: number (mặc định `10`)
  - `keyword`: string (tìm theo tên, username, số điện thoại)
  - `position`: string (`'ADMIN'` hoặc `'STAFF'`)
  - `status`: boolean (`true`: đang hoạt động, `false`: đã khóa)

#### Output (`res.body`):
```json
{
  "status": 200,
  "message": "Lấy danh sách nhân viên thành công",
  "data": {
    "total": 2,
    "page": 1,
    "limit": 10,
    "employees": [
      {
        "employeeId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
        "fullName": "Nguyen Van A",
        "username": "admin",
        "email": "admin@bachhoaxanh.com",
        "phoneNumber": "0900000000",
        "position": "ADMIN",
        "hireDate": "2026-01-01",
        "status": true
      },
      {
        "employeeId": "8cb67a12-1423-4b67-91ef-7e3f8921bca4",
        "fullName": "Le Van B",
        "username": "staff_kho",
        "email": "staff.kho@gmail.com",
        "phoneNumber": "0987654321",
        "position": "STAFF",
        "hireDate": "2026-02-15",
        "status": true
      }
    ]
  }
}
```

#### Middleware:
- `AuthFilter` (Verify Bearer Access Token)
- `AdminRoleFilter` (Chỉ cho phép role ADMIN)

#### Errors (Unhappy path):
- `401 Unauthorized`: Chưa đăng nhập hoặc token hết hạn.
- `403 Forbidden`: Người gọi không phải là ADMIN.

#### Notes:
- JOIN `NhanVien` với `NguoiDung` dựa trên `maNhanVien = maNguoiDung`.
- `status` ánh xạ từ `!Deleted` (`Deleted = false` nghĩa là `status = true`).
- Sắp xếp mặc định theo `ngayVaoLam` giảm dần (mới nhất lên đầu).
- Tuyệt đối không trả về trường `password` và `refreshToken`.

- **Packages:** `jsonwebtoken`

---

### 1.3. Xem thông tin chi tiết một nhân viên

- **Endpoint:** `/employee/:id`
- **Mô tả:** Xem thông tin chi tiết của một nhân viên cụ thể theo mã nhân viên
- **Method:** `GET`

#### Input:
- **Body:** Không có body
- **URL Params:**
  - `id`: string (UUID của nhân viên, `maNhanVien`)

#### Output (`res.body`):
```json
{
  "status": 200,
  "message": "Lấy thông tin nhân viên thành công",
  "data": {
    "employeeId": "8cb67a12-1423-4b67-91ef-7e3f8921bca4",
    "fullName": "Le Van B",
    "username": "staff_kho",
    "email": "staff.kho@gmail.com",
    "phoneNumber": "0987654321",
    "birthDate": "1998-03-20",
    "position": "STAFF",
    "hireDate": "2026-02-15",
    "status": true
  }
}
```

#### Middleware:
- `AuthFilter` (Verify Bearer Access Token)
- `AdminRoleFilter` (Chỉ cho phép role ADMIN)

#### Errors (Unhappy path):
- `401 Unauthorized`: Chưa đăng nhập hoặc token không hợp lệ / hết hạn.
- `403 Forbidden`: Người gọi không phải là ADMIN.
- `404 Not Found`: Không tìm thấy nhân viên với ID tương ứng.

#### Notes:
- JOIN `NhanVien` và `NguoiDung` để lấy đầy đủ ngày sinh, ngày vào làm, chức vụ.
- Tuyệt đối không trả về mật khẩu.

- **Packages:** `jsonwebtoken`

---

### 1.4. Admin cập nhật thông tin nhân viên / Đặt lại mật khẩu

- **Endpoint:** `/employee/:id`
- **Mô tả:** Admin cập nhật thông tin cá nhân, chức vụ hoặc đặt mật khẩu mới trực tiếp cho nhân viên
- **Method:** `PUT`

#### Input:
- **URL Params:**
  - `id`: string (UUID nhân viên)
- **Body (`req.body`):**
```json
{
  "fullName": "Le Van B (Cập nhật)",
  "email": "staff.kho_new@gmail.com",
  "phoneNumber": "0987654322",
  "birthDate": "1998-03-20",
  "position": "STAFF",
  "newPassword": "NewPassword123!"
}
```

#### Output (`res.body`):
```json
{
  "status": 200,
  "message": "Cập nhật tài khoản nhân viên thành công",
  "data": {
    "employeeId": "8cb67a12-1423-4b67-91ef-7e3f8921bca4",
    "fullName": "Le Van B (Cập nhật)",
    "username": "staff_kho",
    "email": "staff.kho_new@gmail.com",
    "phoneNumber": "0987654322",
    "position": "STAFF",
    "status": true
  }
}
```

#### Middleware:
- `AuthFilter` (Verify Bearer Access Token)
- `AdminRoleFilter` (Chỉ cho phép role ADMIN)

#### Errors (Unhappy path):
- `400 Bad Request`: Dữ liệu gửi lên không đúng định dạng.
- `401 Unauthorized`: Chưa đăng nhập hoặc token không hợp lệ / hết hạn.
- `403 Forbidden`: Người gọi không phải là ADMIN (nhân viên STAFF không có quyền sửa).
- `404 Not Found`: Không tìm thấy nhân viên cần cập nhật.
- `409 Conflict`: Số điện thoại hoặc email mới đã bị trùng với tài khoản khác trong hệ thống.

#### Notes:
- Chỉ ADMIN có quyền chỉnh sửa nhân viên; nhân viên không được tự chỉnh sửa tài khoản nhân viên khác.
- Nếu `req.body` có trường `newPassword`: sử dụng BCrypt để băm mật khẩu mới trước khi cập nhật vào cột `matKhauHashed` trong bảng `NguoiDung`.
- Khi mật khẩu mới được cập nhật, tự động cập nhật `refreshToken = NULL` của nhân viên đó trong database để thu hồi phiên làm việc, buộc nhân viên đăng nhập lại bằng mật khẩu mới.
- Gửi email ngầm (`nodemailer`) thông báo đến email nhân viên: *"Tài khoản của bạn vừa được Admin cập nhật thông tin/mật khẩu mới"*.
- Tuyệt đối không trả về mật khẩu trong response.

- **Packages:** `jsonwebtoken`, `bcrypt`, `nodemailer`

---

### 1.5. Khóa hoặc kích hoạt lại tài khoản nhân viên

- **Endpoint:** `/employee/:id`
- **Mô tả:** Khóa hoặc kích hoạt lại tài khoản nhân viên (nghỉ việc hoặc vi phạm quy định)
- **Method:** `PATCH`

#### Input:
- **URL Params:**
  - `id`: string (UUID nhân viên)
- **Body (`req.body`):**
```json
{
  "status": false
}
```

#### Output (`res.body`):
```json
{
  "status": 200,
  "message": "Cập nhật trạng thái nhân viên thành công",
  "data": {
    "employeeId": "8cb67a12-1423-4b67-91ef-7e3f8921bca4",
    "status": false
  }
}
```

#### Middleware:
- `AuthFilter` (Verify Bearer Access Token)
- `AdminRoleFilter` (Chỉ cho phép role ADMIN)

#### Errors (Unhappy path):
- `400 Bad Request`: Thiếu hoặc sai định dạng `status` (phải là boolean).
- `401 Unauthorized`: Chưa đăng nhập hoặc token hết hạn.
- `403 Forbidden`: Người gọi không phải là ADMIN, hoặc Admin tự khóa chính mình.
- `404 Not Found`: Không tìm thấy nhân viên.

#### Notes:
- Không DELETE vật lý khỏi cơ sở dữ liệu để bảo toàn lịch sử dữ liệu (đơn hàng, nhập kho liên quan).
- Cập nhật `Deleted = true` trong cả 2 bảng `NhanVien` và `NguoiDung`.
- Đồng thời set `refreshToken = NULL` trong bảng `NguoiDung` để văng phiên đăng nhập của nhân viên đó ngay lập tức.

- **Packages:** `jsonwebtoken`

---

## 2. ROUTER: `/user` (Thông tin người dùng & Đổi mật khẩu)

### 2.1. Lấy thông tin chi tiết tài khoản đang đăng nhập

- **Endpoint:** `/user/profile`
- **Mô tả:** Lấy thông tin chi tiết tài khoản của người dùng đang đăng nhập (áp dụng chung cho cả Khách hàng và Nhân viên)
- **Method:** `GET`

#### Input:
- **Body:** Không có body
- **Header bắt buộc:**
  ```http
  Authorization: Bearer <access_token>
  ```

#### Output (`res.body`):

**Trường hợp 1: Tài khoản là Khách hàng (CUSTOMER)**
```json
{
  "status": 200,
  "message": "Lấy thông tin tài khoản thành công",
  "data": {
    "userId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "username": "customer01",
    "fullName": "Nguyen Van A",
    "email": "customer01@gmail.com",
    "phoneNumber": "0912345678",
    "birthDate": "2000-01-01",
    "userType": "CUSTOMER",
    "addresses": [
      {
        "addressId": 1,
        "receiverName": "Nguyen Van A",
        "phoneNumber": "0912345678",
        "street": "123 Đường Số 1",
        "ward": "Phường Linh Trung",
        "city": "TP. Thủ Đức",
        "isDefault": true
      }
    ]
  }
}
```

**Trường hợp 2: Tài khoản là Nhân viên (ADMIN hoặc STAFF)**  
*(Trả về các thông tin cá nhân kèm theo `hireDate`, `position`, không có trường `addresses`).*

#### Middleware:
- `AuthFilter` (Verify Bearer Access Token)

#### Errors (Unhappy path):
- `401 Unauthorized`: Chưa đăng nhập, thiếu token hoặc token đã hết hạn / không hợp lệ.
- `403 Forbidden`: Tài khoản đã bị vô hiệu hóa hoặc xóa mềm (`Deleted = true`).
- `404 Not Found`: Không tìm thấy thông tin tài khoản trong cơ sở dữ liệu.

#### Notes:
- Lấy `userId` trực tiếp từ Access Token đã được xác thực ở middleware `AuthFilter`.
- JOIN bảng `NguoiDung` với `KhachHang` hoặc `NhanVien` dựa vào cấu trúc dữ liệu để lấy thông tin chi tiết.
- Nếu là khách hàng: Truy vấn thêm danh sách địa chỉ nhận hàng từ bảng `DiaChi` (với `Deleted = FALSE`).
- Tuyệt đối không trả về mật khẩu (`matKhauHashed`) và `refreshToken` trong phản hồi.

- **Packages:** `jsonwebtoken`

---

### 2.2. Người dùng tự đổi mật khẩu

- **Endpoint:** `/user/change-password`
- **Mô tả:** Khách hàng/Người dùng tự đổi mật khẩu của chính mình khi đã đăng nhập (yêu cầu xác thực mật khẩu cũ) và gửi email cảnh báo bảo mật
- **Method:** `PUT`

#### Input:
- **Header bắt buộc:**
  ```http
  Authorization: Bearer <access_token>
  ```
- **Body (`req.body`):**
```json
{
  "currentPassword": "Password123!",
  "newPassword": "NewPassword456!",
  "confirmPassword": "NewPassword456!"
}
```

#### Output (`res.body`):
```json
{
  "status": 200,
  "message": "Đổi mật khẩu thành công. Vui lòng đăng nhập lại."
}
```

#### Middleware:
- `AuthFilter` (Verify Bearer Access Token)

#### Errors (Unhappy path):
- `400 Bad Request`: Thiếu thông tin bắt buộc, mật khẩu mới không trùng khớp với `confirmPassword`, hoặc mật khẩu mới trùng mật khẩu cũ.
- `401 Unauthorized`: Chưa đăng nhập, token hết hạn, hoặc mật khẩu hiện tại (`currentPassword`) không chính xác.
- `403 Forbidden`: Tài khoản đã bị khóa hoặc xóa mềm (`Deleted = true`).
- `404 Not Found`: Không tìm thấy tài khoản người dùng trong cơ sở dữ liệu.

#### Notes:
- Lấy `userId` trực tiếp từ Access Token đã được giải mã trong `AuthFilter`.
- Truy vấn bảng `NguoiDung` để lấy `matKhauHashed` hiện tại, dùng `bcrypt.compare()` để đối soát `currentPassword`.
- Dùng `bcrypt.hash()` để băm `newPassword` trước khi cập nhật vào cột `matKhauHashed` trong bảng `NguoiDung`.
- Sau khi đổi mật khẩu thành công:
  - Cập nhật cột `refreshToken = NULL` trong bảng `NguoiDung` để thu hồi toàn bộ phiên đăng nhập cũ trên mọi thiết bị.
  - Gọi `res.clearCookie('refreshToken')` để xóa cookie trên trình duyệt hiện tại.
  - Kích hoạt gửi email bất đồng bộ (`nodemailer`) thông báo: *"Mật khẩu tài khoản Bách Hóa Xanh Online của bạn vừa được thay đổi thành công vào lúc [Thời gian]. Nếu không phải bạn thực hiện, vui lòng liên hệ ngay với chúng tôi để khóa tài khoản khẩn cấp."*

- **Packages:** `jsonwebtoken`, `bcrypt`, `nodemailer`

---

## 3. ROUTER: `/payment` (Cổng thanh toán trực tuyến PayOS)

### 3.1. Tạo liên kết & Mã QR thanh toán PayOS

- **Endpoint:** `/payment/create/:orderId` *(Ví dụ: `/payment/create/DH000001`)*
- **Mô tả:** Khởi tạo yêu cầu thanh toán với cổng PayOS, trả về liên kết checkout và chuỗi mã VietQR để khách hàng thanh toán chuyển khoản qua ứng dụng ngân hàng.
- **Method:** `POST`

#### Input:
- **Path Param:** `orderId` (Mã đơn hàng, ví dụ: `DH000001`)
- **Body:** `{}` (hoặc rỗng)

#### Output (`res.body`):
```json
{
  "status": 200,
  "message": "Tạo liên kết thanh toán thành công",
  "data": {
    "orderId": "DH000001",
    "orderCode": 1728518400123,
    "checkoutUrl": "https://pay.payos.vn/web/f7e4...",
    "qrCode": "00020101021238540010A0000007270124...",
    "bin": "970422",
    "accountNumber": "0381000...",
    "accountName": "NGUYEN VAN A",
    "amount": 250000,
    "status": "PENDING"
  }
}
```

#### Middleware:
- `AuthFilter` (Yêu cầu đăng nhập; Khách hàng sở hữu đơn hàng, hoặc Nhân viên / Admin)

#### Errors (Unhappy path):
- `400 Bad Request`: Mã đơn hàng không hợp lệ, đơn hàng đã được thanh toán hoặc đã bị hủy.
- `401 Unauthorized`: Chưa đăng nhập hoặc token hết hạn.
- `403 Forbidden`: Khách hàng khác cố tình thanh toán đơn hàng không thuộc sở hữu của mình.
- `404 Not Found`: Không tìm thấy mã đơn hàng trong hệ thống.
- `500 Internal Error`: Lỗi kết nối PayOS hoặc cấu hình API Key / Checksum không chính xác.

#### Notes:
- Tự động sinh `orderCode` số nguyên duy nhất (`System.currentTimeMillis()`) theo chuẩn của PayOS.
- Tạo chuỗi chữ ký HMAC-SHA256 (`SignatureUtils`) từ dữ liệu `amount`, `cancelUrl`, `description`, `orderCode`, `returnUrl` với `PAYOS_CHECKSUM_KEY`.
- Gửi HTTP POST tới `https://api-merchant.payos.vn/v2/payment-requests`.
- Khi PayOS phản hồi thành công (`code == "00"`), lưu `orderCode` vào đơn hàng (`ghiChu`) và tạo bản ghi `Payment` với trạng thái `PENDING`.
- Cung cấp chuỗi VietQR (`qrCode`) trực tiếp để Frontend render mã QR trên giao diện modal thanh toán tại chỗ mà không cần bắt buộc người dùng chuyển hướng sang trang khác.

---

### 3.2. Webhook PayOS xử lý thanh toán tự động (IPN)

- **Endpoint:** `/payment/webhook`
- **Mô tả:** Nhận tín hiệu IPN (Instant Payment Notification) từ máy chủ PayOS khi người dùng chuyển khoản thành công.
- **Method:** `POST`

#### Input (`req.body`):
```json
{
  "code": "00",
  "desc": "success",
  "data": {
    "orderCode": 1728518400123,
    "amount": 250000,
    "description": "Don hang DH000001",
    "accountNumber": "0381000...",
    "reference": "FT24...",
    "transactionDateTime": "2026-10-10 00:00:00",
    "paymentLinkId": "...",
    "code": "00",
    "desc": "success"
  },
  "signature": "3a18a994ef7e068b..."
}
```

#### Output (`res.body`):
```json
{
  "status": 200,
  "message": "Xử lý webhook thanh toán thành công",
  "data": null
}
```

#### Middleware:
- Public (Không yêu cầu JWT, được gọi từ máy chủ PayOS qua Internet)

#### Errors (Unhappy path):
- `400 Bad Request`: Dữ liệu webhook không hợp lệ, chữ ký `signature` không khớp với `PAYOS_CHECKSUM_KEY`, hoặc không tìm thấy đơn hàng tương ứng với `orderCode`.

#### Notes:
- Backend xác thực tính toàn vẹn của chữ ký `signature` bằng HMAC-SHA256 với `PAYOS_CHECKSUM_KEY` trước khi xử lý.
- Khi chữ ký hợp lệ và `code == "00"`:
  - Cập nhật bản ghi `Payment` sang trạng thái `SUCCESS` và lưu mã giao dịch tham chiếu `reference`.
  - Cập nhật trạng thái đơn hàng `Order` sang `CONFIRMED` (Đã thanh toán).
