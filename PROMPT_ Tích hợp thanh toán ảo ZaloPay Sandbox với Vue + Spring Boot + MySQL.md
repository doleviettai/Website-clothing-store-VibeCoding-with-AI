# PROMPT: Tích hợp thanh toán ảo ZaloPay Sandbox với Vue + Spring Boot + MySQL

Tôi đang phát triển một website bán hàng với kiến trúc:

- Frontend: **Vue 3**
- Backend: **Spring Boot Java**
- Database: **MySQL**
- Payment Gateway: **ZaloPay Sandbox**
- REST API giữa Vue và Spring Boot

Tôi muốn tích hợp **thanh toán ZaloPay Sandbox/ảo** để phục vụ học tập, demo và kiểm thử. Không sử dụng thanh toán tiền thật.

---

## 1. Mục tiêu

Xây dựng hoàn chỉnh quy trình:

```text
Vue 3
  ↓
Giỏ hàng
  ↓
Checkout
  ↓
Chọn ZaloPay
  ↓
Vue gọi Spring Boot
  ↓
Spring Boot tạo Order
  ↓
Order = PENDING
  ↓
Spring Boot tạo app_trans_id
  ↓
Tạo MAC bằng KEY1
  ↓
Gửi request tới ZaloPay Sandbox
  ↓
ZaloPay trả order_url
  ↓
Spring Boot trả order_url cho Vue
  ↓
Vue redirect tới ZaloPay Sandbox
  ↓
Người dùng thực hiện thanh toán ảo
  ↓
ZaloPay xử lý
  ↓
ZaloPay Callback về Spring Boot
  ↓
Spring Boot xác thực callback bằng KEY2
  ↓
Nếu hợp lệ
  ↓
Payment = PAID
  ↓
Vue lấy trạng thái từ Backend
  ↓
Hiển thị kết quả thanh toán
```

---

# 2. Công nghệ Frontend

Sử dụng:

```text
Vue 3
Vue Router
Axios
```

Không sử dụng React.

Cấu trúc frontend:

```text
src/
├── components/
│   └── payment/
│       └── ZaloPayButton.vue
│
├── views/
│   ├── CheckoutView.vue
│   └── PaymentResultView.vue
│
├── services/
│   └── paymentService.js
│
├── router/
│   └── index.js
│
└── App.vue
```

---

# 3. Backend Spring Boot

Cấu trúc:

```text
src/main/java/
└── com.example.project/

    ├── config/
    │   └── ZaloPayConfig.java
    │
    ├── controller/
    │   └── ZaloPayController.java
    │
    ├── service/
    │   └── ZaloPayService.java
    │
    ├── repository/
    │   ├── OrderRepository.java
    │   └── PaymentRepository.java
    │
    ├── entity/
    │   ├── Order.java
    │   └── Payment.java
    │
    ├── dto/
    │   ├── ZaloPayCreateRequest.java
    │   ├── ZaloPayCreateResponse.java
    │   └── ZaloPayCallbackRequest.java
    │
    └── util/
        └── ZaloPayMacUtil.java
```

---

# 4. ZaloPay credentials

Backend sử dụng:

```properties
zalopay.app-id=${ZALOPAY_APP_ID}
zalopay.key1=${ZALOPAY_KEY1}
zalopay.key2=${ZALOPAY_KEY2}

zalopay.create-order-url=${ZALOPAY_CREATE_ORDER_URL}

zalopay.callback-url=${ZALOPAY_CALLBACK_URL}

zalopay.redirect-url=${ZALOPAY_REDIRECT_URL}
```

Giải thích:

```text
APP_ID
→ định danh ứng dụng ZaloPay

KEY1
→ tạo MAC khi gửi request

KEY2
→ xác thực callback

CALLBACK_URL
→ Spring Boot nhận callback từ ZaloPay

REDIRECT_URL
→ Vue nhận người dùng quay lại sau thanh toán
```

Không đưa `KEY1` hoặc `KEY2` vào Vue.

---

# 5. Database

Thiết kế bảng:

```text
orders
```

Các trường:

```text
id
user_id
order_code
app_trans_id
total_amount
payment_method
payment_status
created_at
updated_at
```

Trạng thái:

```text
PENDING
PAID
FAILED
CANCELLED
```

Bảng:

```text
payments
```

Các trường:

```text
id
order_id
transaction_id
provider
amount
status
response_code
response_message
created_at
updated_at
```

Provider:

```text
ZALOPAY
```

---

# 6. API Backend

Tạo các API:

### Tạo thanh toán

```http
POST /api/payments/zalopay/create
```

Request:

```json
{
    "orderId": 123
}
```

Backend phải:

1. Kiểm tra Order.
2. Kiểm tra Order thuộc user.
3. Kiểm tra Order chưa thanh toán.
4. Lấy amount từ Database.
5. Tạo `app_trans_id`.
6. Tạo `app_time`.
7. Tạo `item`.
8. Tạo `embed_data`.
9. Tạo MAC bằng KEY1.
10. Gửi request tới ZaloPay.
11. Nhận `order_url`.
12. Lưu payment.
13. Trả `order_url` cho Vue.

Response:

```json
{
    "success": true,
    "orderUrl": "https://...",
    "appTransId": "..."
}
```

---

# 7. API Callback

Tạo:

```http
POST /api/payments/zalopay/callback
```

Backend nhận callback từ ZaloPay.

Thực hiện:

```text
Receive callback
       ↓
Get data
       ↓
Get mac
       ↓
Verify MAC bằng KEY2
       ↓
Nếu MAC sai
       ↓
Reject

Nếu MAC đúng
       ↓
Lấy app_trans_id
       ↓
Tìm Order
       ↓
Kiểm tra amount
       ↓
Kiểm tra trạng thái
       ↓
Update PAID
```

Callback phải có tính **idempotent**.

Nếu callback được gửi nhiều lần:

```text
PAID
 ↓
PAID
 ↓
PAID
```

không được tạo payment mới.

---

# 8. Query trạng thái thanh toán

Tạo:

```http
GET /api/payments/zalopay/{appTransId}
```

Backend gọi ZaloPay Query Order API khi cần.

Mục đích:

```text
Callback lỗi
     ↓
Frontend vẫn có thể kiểm tra
     ↓
Backend query ZaloPay
     ↓
Xác định trạng thái
```

---

# 9. Vue Payment Service

Tạo:

```text
src/services/paymentService.js
```

Sử dụng Axios.

Ví dụ API:

```javascript
import axios from "axios";

const API_URL = "http://localhost:8080/api";

export const createZaloPayPayment = async (orderId) => {
    const response = await axios.post(
        `${API_URL}/payments/zalopay/create`,
        {
            orderId
        }
    );

    return response.data;
};

export const getPaymentStatus = async (appTransId) => {
    const response = await axios.get(
        `${API_URL}/payments/zalopay/${appTransId}`
    );

    return response.data;
};
```

---

# 10. Vue ZaloPay Button

Tạo:

```text
src/components/payment/ZaloPayButton.vue
```

Button:

```text
Thanh toán bằng ZaloPay
```

Khi click:

```text
Vue
 ↓
createZaloPayPayment(orderId)
 ↓
Backend
 ↓
ZaloPay
 ↓
order_url
 ↓
window.location.href = order_url
```

Ví dụ:

```javascript
const payment = async () => {

    const response = await createZaloPayPayment(props.orderId);

    if (response.success) {
        window.location.href = response.orderUrl;
    }
};
```

---

# 11. Vue Checkout

Tạo:

```text
src/views/CheckoutView.vue
```

Hiển thị:

```text
Thông tin đơn hàng

Sản phẩm
Số lượng
Đơn giá
Tổng tiền

Phương thức thanh toán:

○ COD

● ZaloPay

[Thanh toán bằng ZaloPay]
```

Khi chọn ZaloPay:

```text
CheckoutView
       ↓
ZaloPayButton
       ↓
paymentService.js
       ↓
Spring Boot
```

---

# 12. Vue Router

Tạo route:

```javascript
{
    path: "/payment-result",
    name: "PaymentResult",
    component: PaymentResultView
}
```

URL:

```text
http://localhost:5173/payment-result
```

---

# 13. PaymentResultView.vue

Tạo:

```text
src/views/PaymentResultView.vue
```

Trang này phải:

1. Lấy `orderId` hoặc `appTransId` từ URL.
2. Gọi Backend.
3. Backend kiểm tra Database.
4. Hiển thị trạng thái.

Ví dụ:

```text
Đang kiểm tra thanh toán...
```

Sau đó:

```text
✓ Thanh toán thành công

Mã đơn hàng:
ORD-001

Số tiền:
500.000 VNĐ
```

Nếu thất bại:

```text
✕ Thanh toán thất bại
```

Nếu vẫn đang xử lý:

```text
⏳ Đang xử lý thanh toán...
```

Không được chỉ dựa vào URL redirect để kết luận thanh toán thành công.

---

# 14. Luồng đầy đủ Vue → Spring Boot → ZaloPay

```text
┌──────────────────────┐
│       Vue 3           │
│      Checkout         │
└──────────┬───────────┘
           │
           │ POST /create
           ▼
┌──────────────────────┐
│    Spring Boot        │
│                       │
│ Validate Order        │
│ Create Payment        │
│ Generate app_trans_id │
│ Generate MAC KEY1     │
└──────────┬───────────┘
           │
           │ HTTPS Request
           ▼
┌──────────────────────┐
│    ZaloPay Sandbox    │
└──────────┬───────────┘
           │
           │ order_url
           ▼
┌──────────────────────┐
│       Vue 3           │
│ window.location.href  │
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│ ZaloPay Sandbox       │
│ Thanh toán ảo         │
└──────────┬───────────┘
           │
           │ Callback
           ▼
┌──────────────────────┐
│    Spring Boot        │
│ Verify KEY2           │
│ Update Database       │
│ PENDING → PAID        │
└──────────┬───────────┘
           │
           │ GET payment status
           ▼
┌──────────────────────┐
│       Vue 3           │
│ PaymentResultView     │
│                       │
│ ✓ Thanh toán thành công│
└──────────────────────┘
```

---

# 15. Xử lý Callback khi chạy localhost

Backend:

```text
localhost:8080
```

ZaloPay không thể gọi trực tiếp:

```text
http://localhost:8080
```

Do đó sử dụng:

```text
ngrok
```

hoặc:

```text
Cloudflare Tunnel
```

Ví dụ:

```text
Spring Boot:
http://localhost:8080

ngrok:
https://abc123.ngrok-free.app
```

Callback:

```text
https://abc123.ngrok-free.app/api/payments/zalopay/callback
```

---

# 16. Bảo mật

Thiết kế phải đảm bảo:

```text
KEY1
KEY2
APP_SECRET
```

chỉ nằm ở Backend.

Không được:

```text
Vue
 ↓
KEY1 ❌
KEY2 ❌
```

Mà phải:

```text
Vue
 ↓
Spring Boot
 ↓
KEY1 / KEY2
 ↓
ZaloPay
```

Backend cũng phải:

- Không tin `amount` từ Vue.
- Lấy amount từ Database.
- Kiểm tra user sở hữu Order.
- Verify callback.
- Verify MAC.
- Kiểm tra `app_trans_id`.
- Kiểm tra amount.
- Chống duplicate callback.
- Sử dụng transaction khi update database.
- Không commit secret lên GitHub.

---

# 17. Các trường hợp cần test

### Test 1 — Thành công

```text
Checkout
 ↓
ZaloPay
 ↓
Thanh toán Sandbox
 ↓
Callback
 ↓
PAID
```

### Test 2 — Hủy thanh toán

```text
Checkout
 ↓
ZaloPay
 ↓
Cancel
 ↓
Return Vue
```

### Test 3 — Callback sai MAC

```text
Callback
 ↓
KEY2 verify
 ↓
MAC không hợp lệ
 ↓
Reject
```

### Test 4 — Callback nhiều lần

```text
Callback
 ↓
PAID

Callback lần 2
 ↓
Không tạo Payment mới
```

### Test 5 — Frontend sửa amount

Ví dụ Vue cố gửi:

```json
{
    "orderId": 123,
    "amount": 1
}
```

Backend vẫn phải lấy:

```text
amount = Database
```

không lấy:

```text
amount = Frontend
```

### Test 6 — Callback không tới

Sử dụng Query Order API để kiểm tra trạng thái giao dịch.

---

# 18. Yêu cầu cách hướng dẫn

Không đưa toàn bộ code cùng lúc.

Hãy hướng dẫn tôi theo thứ tự:

```text
STEP 1
Chuẩn bị ZaloPay Sandbox
        ↓
STEP 2
APP_ID / KEY1 / KEY2
        ↓
STEP 3
Cấu hình Spring Boot
        ↓
STEP 4
Database
        ↓
STEP 5
ZaloPayMacUtil
        ↓
STEP 6
ZaloPayService
        ↓
STEP 7
Create Payment API
        ↓
STEP 8
Vue paymentService
        ↓
STEP 9
Vue ZaloPayButton
        ↓
STEP 10
Checkout
        ↓
STEP 11
Redirect
        ↓
STEP 12
Callback
        ↓
STEP 13
Verify KEY2
        ↓
STEP 14
PaymentResultView
        ↓
STEP 15
Query Order
        ↓
STEP 16
ngrok
        ↓
STEP 17
Test toàn bộ
```

Ở mỗi bước:

- Giải thích mục đích.
- Cho biết file nào cần tạo/sửa.
- Đưa code hoàn chỉnh của bước đó.
- Giải thích từng phần quan trọng.
- Cho biết cách chạy.
- Cho biết kết quả mong đợi.
- Cho biết lỗi thường gặp.
- Không chuyển sang bước tiếp theo cho đến khi bước hiện tại hoàn thành.

Hãy sử dụng **tài liệu ZaloPay chính thức và API Sandbox hiện tại**, không sử dụng endpoint hoặc parameter đã lỗi thời từ các tutorial cũ.