# PROMPT: Mô tả và triển khai quy trình thanh toán ảo MoMo Test/Sandbox

Tôi đang phát triển một website bán hàng với kiến trúc:

- Frontend: Vue 3
- Backend: Spring Boot Java
- Database: MySQL
- Payment Gateway: MoMo
- Môi trường: MoMo Test/Sandbox
- Giao tiếp giữa Frontend và Backend: REST API
- Mục đích: học tập, đồ án và demo
- Không sử dụng giao dịch tiền thật.

Tôi muốn xây dựng chức năng **thanh toán ảo bằng MoMo Test/Sandbox** hoàn chỉnh từ lúc người dùng checkout cho đến khi hệ thống xác nhận thanh toán thành công.

---

# 1. Mục tiêu

Hãy mô tả và hướng dẫn triển khai toàn bộ quy trình:

```text
Người dùng
    ↓
Vue 3 - Checkout
    ↓
Chọn "MoMo"
    ↓
Vue gọi Spring Boot
    ↓
Spring Boot kiểm tra Order
    ↓
Tạo Payment
    ↓
Payment = PENDING
    ↓
Backend tạo requestId
    ↓
Backend tạo orderId
    ↓
Backend lấy amount từ Database
    ↓
Backend tạo signature
    ↓
Backend gọi MoMo Test API
    ↓
MoMo trả về payUrl
    ↓
Spring Boot trả payUrl cho Vue
    ↓
Vue redirect người dùng tới MoMo
    ↓
Người dùng thanh toán bằng MoMo Test
    ↓
MoMo xử lý giao dịch
    ↓
MoMo gửi IPN/Callback về Backend
    ↓
Backend xác thực signature
    ↓
Kiểm tra orderId
    ↓
Kiểm tra amount
    ↓
Kiểm tra resultCode
    ↓
Nếu hợp lệ
    ↓
Payment = PAID
    ↓
Order = PAID / CONFIRMED
    ↓
Vue kiểm tra trạng thái
    ↓
Hiển thị "Thanh toán thành công"
```

---

# 2. Giải thích vai trò của từng thành phần

## Vue 3

Vue chịu trách nhiệm:

- Hiển thị Checkout.
- Hiển thị tổng tiền.
- Cho phép chọn MoMo.
- Gửi `orderId` tới Backend.
- Nhận `payUrl`.
- Redirect người dùng tới MoMo.
- Hiển thị trạng thái thanh toán.

Vue KHÔNG được chứa:

```text
Secret Key
Access Key
Private Key
Signature Secret
```

---

# 3. Spring Boot

Spring Boot chịu trách nhiệm:

- Tạo Payment.
- Tạo `requestId`.
- Tạo `orderId`.
- Lấy số tiền từ Database.
- Tạo signature.
- Gọi MoMo Test API.
- Nhận `payUrl`.
- Xử lý IPN/Callback.
- Verify signature.
- Kiểm tra `resultCode`.
- Kiểm tra amount.
- Cập nhật Payment.
- Cập nhật Order.

---

# 4. MySQL

Thiết kế hệ thống Database để lưu:

```text
Order
Payment
Transaction
```

Không lưu trạng thái thanh toán chỉ ở Vue.

Database phải là nguồn dữ liệu chính để xác định:

```text
PENDING
PAID
FAILED
CANCELLED
```

---

# 5. MoMo Credentials

Giải thích rõ cách sử dụng:

```text
Partner Code
Access Key
Secret Key
```

Phân biệt:

```text
Partner Code
    ↓
Định danh merchant/app

Access Key
    ↓
Thông tin truy cập API

Secret Key
    ↓
Dùng tạo chữ ký
```

Không đưa các thông tin này vào Vue.

Chúng phải nằm ở Backend:

```properties
momo.partner-code=${MOMO_PARTNER_CODE}
momo.access-key=${MOMO_ACCESS_KEY}
momo.secret-key=${MOMO_SECRET_KEY}
```

Nếu quy trình lấy credentials hoặc môi trường Test của MoMo đã thay đổi, hãy sử dụng tài liệu MoMo Developer chính thức hiện tại thay vì các tutorial cũ.

---

# 6. MoMo Test Endpoint

Xác định chính xác:

```text
MoMo Test API Base URL
```

Không tự đoán endpoint.

Hãy kiểm tra tài liệu MoMo hiện tại và cho biết:

```text
Create Payment API
Query Payment API
IPN/Callback
```

Endpoint nào được sử dụng trong môi trường Test.

---

# 7. Thiết kế Database

Tạo bảng:

```text
orders
```

Ví dụ:

```text
id
user_id
order_code
total_amount
status
created_at
updated_at
```

Tạo bảng:

```text
payments
```

Ví dụ:

```text
id
order_id
provider
transaction_id
amount
status
request_id
response_code
response_message
created_at
updated_at
```

Provider:

```text
MOMO
```

Status:

```text
PENDING
PAID
FAILED
CANCELLED
```

Giải thích tại sao phải tách:

```text
Order
```

và:

```text
Payment
```

---

# 8. API tạo thanh toán

Tạo API:

```http
POST /api/payments/momo/create
```

Request:

```json
{
    "orderId": 123
}
```

Backend phải:

1. Kiểm tra Order tồn tại.
2. Kiểm tra user sở hữu Order.
3. Kiểm tra Order chưa thanh toán.
4. Lấy amount từ Database.
5. Tạo `requestId`.
6. Tạo `orderId` của MoMo.
7. Tạo `orderInfo`.
8. Tạo `redirectUrl`.
9. Tạo `ipnUrl`.
10. Tạo request body.
11. Tạo signature bằng Secret Key.
12. Gửi request tới MoMo Test.
13. Nhận response.
14. Kiểm tra `resultCode`.
15. Lưu Payment.
16. Trả `payUrl` cho Vue.

---

# 9. Signature

Giải thích thật kỹ cơ chế signature của MoMo.

Cho biết:

```text
rawSignature
```

được tạo như thế nào.

Ví dụ mô hình:

```text
Request parameters
       ↓
Ghép thành rawSignature
       ↓
Secret Key
       ↓
HMAC-SHA256
       ↓
signature
       ↓
Gửi MoMo
```

Phải sử dụng chính xác:

- Tên parameter.
- Thứ tự parameter.
- Format raw signature.
- HMAC algorithm.
- Encoding.

Không được lấy cách tạo signature từ tutorial cũ nếu khác với API MoMo hiện tại.

---

# 10. redirectUrl

Tạo route Vue:

```text
/payment-result
```

Ví dụ:

```text
http://localhost:5173/payment-result
```

Backend gửi URL này cho MoMo:

```text
redirectUrl
```

Sau khi người dùng thanh toán:

```text
MoMo
   ↓
redirectUrl
   ↓
Vue
```

Nhưng phải giải thích rõ:

> Không được coi việc người dùng quay về `redirectUrl` là bằng chứng thanh toán thành công.

Frontend phải gọi Backend để lấy trạng thái thực tế.

---

# 11. IPN / Callback

Đây là phần quan trọng nhất.

Tạo Backend API:

```http
POST /api/payments/momo/ipn
```

MoMo sẽ gọi:

```text
MoMo
   ↓
Internet
   ↓
Backend
   ↓
/api/payments/momo/ipn
```

Backend phải:

1. Nhận request.
2. Lấy các trường callback.
3. Tạo lại raw signature.
4. Dùng Secret Key.
5. Verify signature.
6. Nếu signature sai → reject.
7. Tìm Payment.
8. Kiểm tra orderId.
9. Kiểm tra amount.
10. Kiểm tra transactionId.
11. Kiểm tra resultCode.
12. Nếu thành công → PAID.
13. Nếu thất bại → FAILED.
14. Không xử lý callback trùng nhiều lần.

---

# 12. Idempotency

Phải thiết kế callback có khả năng xử lý nhiều lần.

Ví dụ:

```text
MoMo callback #1
    ↓
PAID

MoMo callback #2
    ↓
Không tạo Payment mới
    ↓
Không cộng tiền lần 2
    ↓
Không cập nhật Order sai
```

Giải thích cách kiểm tra:

```text
transactionId
```

hoặc:

```text
orderId
```

để chống duplicate callback.

---

# 13. Query Payment

Tạo API:

```http
GET /api/payments/momo/{orderId}/status
```

Backend kiểm tra Database.

Nếu cần thiết, sử dụng MoMo Query API để kiểm tra lại trạng thái giao dịch.

Luồng:

```text
Vue
 ↓
GET /api/payments/momo/123/status
 ↓
Spring Boot
 ↓
MySQL
```

Nếu Payment vẫn:

```text
PENDING
```

thì Backend có thể query MoMo.

---

# 14. Vue Service

Tạo:

```text
src/services/paymentService.js
```

Ví dụ:

```javascript
import axios from "axios";

const API_URL = "http://localhost:8080/api";

export const createMoMoPayment = async (orderId) => {
    const response = await axios.post(
        `${API_URL}/payments/momo/create`,
        {
            orderId
        }
    );

    return response.data;
};

export const getMoMoPaymentStatus = async (orderId) => {
    const response = await axios.get(
        `${API_URL}/payments/momo/${orderId}/status`
    );

    return response.data;
};
```

---

# 15. Vue Checkout

Trong:

```text
CheckoutView.vue
```

hiển thị:

```text
Phương thức thanh toán

○ COD

○ ZaloPay

● MoMo
```

Khi người dùng chọn:

```text
MoMo
```

và nhấn:

```text
Thanh toán
```

thì:

```text
Vue
 ↓
POST /api/payments/momo/create
 ↓
Backend
 ↓
MoMo
 ↓
payUrl
 ↓
Vue
 ↓
window.location.href = payUrl
```

---

# 16. PaymentResultView.vue

Tạo:

```text
src/views/PaymentResultView.vue
```

Route:

```text
/payment-result
```

Khi người dùng quay về:

```text
MoMo
 ↓
Vue PaymentResult
```

Vue không được tự kết luận:

```text
resultCode = 0
```

từ URL một cách mù quáng.

Thay vào đó:

```text
Vue
 ↓
GET /api/payments/momo/{orderId}/status
 ↓
Backend
 ↓
MySQL
 ↓
PAID / FAILED / PENDING
```

Sau đó hiển thị:

### Thành công

```text
✓ Thanh toán thành công

Mã đơn hàng: ORD001
Phương thức: MoMo
Số tiền: 500.000 VNĐ
```

### Thất bại

```text
✕ Thanh toán thất bại

Vui lòng thử lại.
```

### Đang xử lý

```text
⏳ Đang xử lý thanh toán...
```

---

# 17. Callback khi chạy localhost

Backend:

```text
http://localhost:8080
```

không thể trực tiếp nhận callback từ MoMo.

Hãy hướng dẫn sử dụng:

```text
ngrok
```

hoặc:

```text
Cloudflare Tunnel
```

Ví dụ:

```text
Spring Boot
localhost:8080
       ↓
ngrok
       ↓
https://abc123.ngrok-free.app
       ↓
/api/payments/momo/ipn
```

IPN URL:

```text
https://abc123.ngrok-free.app/api/payments/momo/ipn
```

---

# 18. Bảo mật

Đảm bảo:

```text
Secret Key
Access Key
```

chỉ nằm ở Backend.

Không được:

```text
Vue
 ├── Secret Key ❌
 └── Access Key ❌
```

Mà phải:

```text
Vue
 ↓
Spring Boot
 ↓
MoMo
```

Backend phải:

- Không tin amount từ Vue.
- Lấy amount từ Database.
- Verify signature.
- Kiểm tra orderId.
- Kiểm tra amount.
- Kiểm tra transactionId.
- Chống callback giả.
- Chống callback duplicate.
- Không commit Secret Key lên Git.
- Sử dụng environment variables.
- Không log Secret Key.
- Không log toàn bộ thông tin nhạy cảm.

---

# 19. Các trường hợp phải test

## Test 1 — Thanh toán thành công

```text
Checkout
 ↓
MoMo
 ↓
Test Wallet
 ↓
Thanh toán
 ↓
IPN
 ↓
Verify Signature
 ↓
PAID
```

## Test 2 — Người dùng hủy

```text
Checkout
 ↓
MoMo
 ↓
Cancel
 ↓
Vue
```

## Test 3 — Signature sai

```text
IPN
 ↓
Verify Signature
 ↓
Invalid
 ↓
Reject
```

## Test 4 — Callback trùng

```text
Callback #1
 ↓
PAID

Callback #2
 ↓
Không xử lý lần 2
```

## Test 5 — Amount bị sửa

Vue gửi:

```json
{
    "orderId": 123,
    "amount": 1000
}
```

Backend phải bỏ qua amount từ Vue và lấy:

```text
orders.total_amount
```

từ Database.

## Test 6 — Callback giả

Một request giả gửi:

```text
resultCode = 0
```

nhưng signature không hợp lệ.

Backend phải:

```text
REJECT
```

## Test 7 — MoMo timeout

Nếu MoMo không phản hồi:

```text
Payment = PENDING
```

Không được tự động:

```text
PAID
```

---

# 20. Cấu trúc project đề xuất

```text
backend/
└── src/main/java/
    └── com.example.project/
        │
        ├── controller/
        │   └── MoMoController.java
        │
        ├── service/
        │   └── MoMoService.java
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
        │   ├── MoMoCreateRequest.java
        │   ├── MoMoCreateResponse.java
        │   └── MoMoIPNRequest.java
        │
        └── util/
            └── MoMoSignatureUtil.java
```

Frontend:

```text
frontend/
└── src/
    │
    ├── components/
    │   └── payment/
    │       └── MoMoButton.vue
    │
    ├── views/
    │   ├── CheckoutView.vue
    │   └── PaymentResultView.vue
    │
    ├── services/
    │   └── paymentService.js
    │
    └── router/
        └── index.js
```

---

# 21. Yêu cầu cách hướng dẫn

Không đưa toàn bộ code một lần.

Hãy hướng dẫn tôi từng bước:

```text
STEP 1
Kiểm tra MoMo Test Credentials
        ↓
STEP 2
Cấu hình Spring Boot
        ↓
STEP 3
Thiết kế Database
        ↓
STEP 4
Tạo Entity Order + Payment
        ↓
STEP 5
Tạo MoMoConfig
        ↓
STEP 6
Tạo MoMoSignatureUtil
        ↓
STEP 7
Tạo MoMoService
        ↓
STEP 8
Tạo API Create Payment
        ↓
STEP 9
Test API bằng Postman
        ↓
STEP 10
Tạo Vue paymentService
        ↓
STEP 11
Tích hợp nút MoMo vào Checkout
        ↓
STEP 12
Redirect sang MoMo Test
        ↓
STEP 13
Tạo IPN Callback
        ↓
STEP 14
Cấu hình ngrok
        ↓
STEP 15
Verify Callback
        ↓
STEP 16
Update Payment
        ↓
STEP 17
PaymentResultView
        ↓
STEP 18
Query Payment Status
        ↓
STEP 19
Test thành công
        ↓
STEP 20
Test thất bại / hủy / callback giả / duplicate
```

Ở mỗi STEP:

1. Giải thích mục đích.
2. Cho biết file cần tạo/sửa.
3. Đưa code hoàn chỉnh.
4. Giải thích code.
5. Cho biết cách chạy.
6. Cho biết kết quả mong đợi.
7. Cho biết lỗi thường gặp.
8. Chỉ chuyển sang STEP tiếp theo khi STEP hiện tại đã hoàn thành.

Ưu tiên **tài liệu MoMo Developer chính thức hiện tại**. Không sử dụng endpoint, parameter, signature format hoặc code mẫu từ các tutorial cũ nếu chúng không còn phù hợp với API hiện tại.

Mục tiêu cuối cùng là tôi có một hệ thống:

```text
Vue 3
   ↓
Spring Boot
   ↓
MoMo Test
   ↓
MoMo Test Wallet
   ↓
IPN Callback
   ↓
Spring Boot
   ↓
MySQL
   ↓
Payment = PAID
```

và có thể chạy song song với hệ thống:

```text
Vue 3
   ↓
Spring Boot
   ↓
ZaloPay Sandbox
```

để website hỗ trợ **nhiều cổng thanh toán**.