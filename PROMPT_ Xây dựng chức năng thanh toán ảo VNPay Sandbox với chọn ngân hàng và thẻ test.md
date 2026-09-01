# PROMPT: Xây dựng chức năng thanh toán ảo VNPay Sandbox với chọn ngân hàng và thẻ test

Tôi đang phát triển một website bán hàng với kiến trúc:

- Frontend: Vue 3
- Backend: Spring Boot Java
- Database: MySQL
- Payment Gateway: VNPay
- Môi trường: VNPay Sandbox/Test
- Giao tiếp Frontend và Backend: REST API
- Mục đích: học tập, đồ án và demo
- Không sử dụng giao dịch tiền thật.

Tôi muốn xây dựng hoàn chỉnh chức năng **thanh toán VNPay Sandbox**.

Hệ thống phải cho phép người dùng:

1. Chọn phương thức thanh toán VNPay.
2. Có thể chọn ngân hàng/phương thức thanh toán nếu API VNPay hiện tại hỗ trợ.
3. Nhấn nút thanh toán.
4. Website chuyển người dùng sang trang thanh toán VNPay Sandbox.
5. Trên trang VNPay Sandbox, người dùng chọn phương thức/ngân hàng phù hợp.
6. Nhập thông tin thẻ test chính thức do VNPay cung cấp.
7. Nhập OTP test nếu môi trường VNPay yêu cầu.
8. Thực hiện thanh toán.
9. Nếu thanh toán thành công, VNPay tự động redirect người dùng về website bán hàng.
10. Website xác minh kết quả với Backend.
11. Backend cập nhật Payment và Order trong MySQL.
12. Frontend hiển thị trạng thái thanh toán thành công và thông tin đơn hàng.

Không được sử dụng tiền thật.

Không sử dụng số thẻ thật.

Không hard-code thông tin bí mật trong Vue.

Nếu tài liệu, endpoint, tham số hoặc cơ chế ký của VNPay hiện tại khác với các tutorial cũ, phải ưu tiên tài liệu VNPay chính thức hiện tại.

---

# 1. Mục tiêu tổng thể

Hãy thiết kế và hướng dẫn triển khai quy trình:

```text
NGƯỜI DÙNG
     │
     ▼
VUE 3 - CHECKOUT
     │
     ▼
Chọn VNPay
     │
     ▼
Chọn ngân hàng/phương thức (nếu cần)
     │
     ▼
Nhấn "Thanh toán"
     │
     ▼
POST /api/payments/vnpay/create
     │
     ▼
SPRING BOOT
     │
     ├── Kiểm tra Order
     ├── Kiểm tra quyền sở hữu Order
     ├── Kiểm tra Order chưa thanh toán
     ├── Lấy amount từ MySQL
     ├── Không tin amount từ Vue
     │
     ▼
Tạo Payment = PENDING
     │
     ▼
Tạo vnp_TxnRef
     │
     ▼
Tạo các tham số VNPay
     │
     ▼
Tạo Secure Hash
     │
     ▼
Tạo URL thanh toán VNPay Sandbox
     │
     ▼
Trả paymentUrl cho Vue
     │
     ▼
VUE REDIRECT
window.location.href = paymentUrl
     │
     ▼
TRANG THANH TOÁN VNPAY SANDBOX
     │
     ▼
Chọn ngân hàng/phương thức
     │
     ▼
Nhập thẻ TEST
     │
     ▼
Nhập OTP TEST
     │
     ▼
Thanh toán
     │
     ├─────────────────────┐
     │                     │
     ▼                     ▼
THÀNH CÔNG             THẤT BẠI/HỦY
     │                     │
     ▼                     ▼
VNPay Redirect          VNPay Redirect
     │                     │
     ▼                     ▼
Vue PaymentResultView
     │
     ▼
Gọi Spring Boot
     │
     ▼
Verify dữ liệu trả về
     │
     ├── Verify Secure Hash
     ├── Kiểm tra vnp_TxnRef
     ├── Kiểm tra amount
     ├── Kiểm tra Payment
     └── Kiểm tra response code
     │
     ▼
Cập nhật MySQL
     │
     ├── Payment = PAID
     └── Order = PAID
     │
     ▼
HIỂN THỊ ĐƠN HÀNG
```

---

# 2. Vai trò của từng thành phần

## Vue 3

Vue chịu trách nhiệm:

- Hiển thị Checkout.
- Hiển thị phương thức VNPay.
- Hiển thị tùy chọn ngân hàng/phương thức nếu cần.
- Gửi `orderId` và lựa chọn hợp lệ tới Backend.
- Nhận `paymentUrl`.
- Redirect người dùng sang VNPay Sandbox.
- Nhận người dùng quay lại `PaymentResultView`.
- Gọi Backend để lấy trạng thái thanh toán thực tế.
- Hiển thị thông tin đơn hàng sau khi thanh toán.

Vue KHÔNG được chứa:

```text
vnp_HashSecret
Secret Key
API Secret
Secure Hash Key
```

Không được tạo chữ ký VNPay trong Vue.

---

# 3. Spring Boot

Spring Boot chịu trách nhiệm:

- Kiểm tra Order.
- Kiểm tra user hiện tại có quyền thanh toán Order.
- Lấy tổng tiền từ MySQL.
- Tạo Payment.
- Tạo `vnp_TxnRef`.
- Tạo request thanh toán VNPay.
- Tạo Secure Hash.
- Trả URL thanh toán cho Vue.
- Xử lý kết quả VNPay trả về.
- Verify chữ ký.
- Kiểm tra số tiền.
- Kiểm tra mã giao dịch.
- Cập nhật Payment.
- Cập nhật Order.
- Chống xử lý giao dịch nhiều lần.

---

# 4. VNPay Credentials

Hãy kiểm tra tài liệu chính thức hiện tại của VNPay và hướng dẫn sử dụng các thông tin Sandbox cần thiết, ví dụ:

```text
vnp_TmnCode
vnp_HashSecret
vnp_PayUrl
vnp_ReturnUrl
```

Giải thích rõ:

```text
vnp_TmnCode
    ↓
Mã định danh Terminal/Merchant Sandbox

vnp_HashSecret
    ↓
Dùng để tạo và xác minh Secure Hash

vnp_PayUrl
    ↓
Endpoint/trang tạo thanh toán Sandbox

vnp_ReturnUrl
    ↓
URL mà VNPay redirect người dùng quay lại website
```

Không tự đoán endpoint.

Không sử dụng thông tin Production để test.

Nếu cần đăng ký Sandbox để lấy thông tin test, hãy hướng dẫn theo quy trình VNPay chính thức hiện tại.

Cấu hình Spring Boot bằng Environment Variables:

```properties
vnpay.tmn-code=${VNPAY_TMN_CODE}
vnpay.hash-secret=${VNPAY_HASH_SECRET}
vnpay.pay-url=${VNPAY_PAY_URL}
vnpay.return-url=${VNPAY_RETURN_URL}
```

Không commit `vnp_HashSecret` lên GitHub.

---

# 5. Thiết kế Database

Tôi đã có hoặc cần thiết kế bảng:

```text
orders
```

Ví dụ:

```text
orders
--------------------------------
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
payments
--------------------------------
id
order_id
provider
payment_method
bank_code

transaction_id
txn_ref

amount

status

response_code
response_message

created_at
updated_at
```

Provider:

```text
VNPAY
MOMO
ZALOPAY
COD
```

Payment method:

```text
VNPAY
MOMO_WALLET
MOMO_ATM
ZALOPAY_WALLET
ZALOPAY_DOMESTIC_CARD
COD
```

Payment status:

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

để sau này một Order có thể hỗ trợ nhiều phương thức thanh toán.

---

# 6. Thiết kế giao diện Vue Checkout

Trong:

```text
CheckoutView.vue
```

hiển thị:

```text
PHƯƠNG THỨC THANH TOÁN

○ COD

○ MoMo

○ ZaloPay

● VNPay
```

Khi chọn VNPay, có thể hiển thị:

```text
┌─────────────────────────────────────┐
│             THANH TOÁN VNPAY        │
├─────────────────────────────────────┤
│                                     │
│ Phương thức:                        │
│                                     │
│ ● Thanh toán qua VNPay              │
│                                     │
│ Ngân hàng:                          │
│                                     │
│ [ Chọn ngân hàng               ▼ ]  │
│                                     │
│ Ví dụ các lựa chọn được hỗ trợ:     │
│ - Tự chọn tại VNPay                 │
│ - Ngân hàng nội địa                 │
│ - Thẻ quốc tế                       │
│ - QR nếu Sandbox/API hỗ trợ         │
│                                     │
│ [       THANH TOÁN VỚI VNPAY      ] │
└─────────────────────────────────────┘
```

Không được hard-code danh sách ngân hàng nếu VNPay không hỗ trợ truyền `bankCode` theo cách đó.

Nếu `vnp_BankCode` được API hiện tại hỗ trợ:

```json
{
    "orderId": 123,
    "provider": "VNPAY",
    "bankCode": "NCB"
}
```

Nếu người dùng không chọn ngân hàng:

```json
{
    "orderId": 123,
    "provider": "VNPAY",
    "bankCode": ""
}
```

Khi đó để VNPay hiển thị trang lựa chọn phương thức thanh toán.

---

# 7. API tạo thanh toán

Tạo API:

```http
POST /api/payments/vnpay/create
```

Request:

```json
{
    "orderId": 123,
    "bankCode": ""
}
```

Backend phải thực hiện:

```text
1. Kiểm tra Order tồn tại.

2. Kiểm tra Order thuộc về user hiện tại.

3. Kiểm tra Order chưa thanh toán.

4. Lấy amount từ Database.

5. Không sử dụng amount do Vue gửi.

6. Tạo Payment nếu chưa có.

7. Payment status = PENDING.

8. Tạo vnp_TxnRef duy nhất.

9. Tạo vnp_OrderInfo.

10. Tạo vnp_Amount theo đúng format VNPay yêu cầu.

11. Tạo vnp_CreateDate.

12. Tạo vnp_ExpireDate nếu API hiện tại yêu cầu/hỗ trợ.

13. Thêm vnp_BankCode nếu người dùng đã chọn.

14. Thêm vnp_ReturnUrl.

15. Sắp xếp tham số đúng quy tắc.

16. Tạo HashData.

17. Tạo SecureHash bằng thuật toán VNPay hiện tại.

18. Tạo paymentUrl.

19. Trả paymentUrl về Vue.
```

Response:

```json
{
    "success": true,
    "paymentUrl": "VNPAY_SANDBOX_PAYMENT_URL",
    "txnRef": "ORDER_123_..."
}
```

---

# 8. Tạo URL thanh toán VNPay

Giải thích chi tiết:

```text
Order
   ↓
vnp_TxnRef

Amount
   ↓
vnp_Amount

Order Information
   ↓
vnp_OrderInfo

Bank
   ↓
vnp_BankCode

Return URL
   ↓
vnp_ReturnUrl

Các tham số VNPay
   ↓
Sort
   ↓
Hash Data
   ↓
HMAC/Secure Hash theo tài liệu hiện tại
   ↓
vnp_SecureHash
   ↓
Payment URL
```

Phải sử dụng chính xác thuật toán chữ ký được VNPay yêu cầu hiện tại.

Không lấy code MD5/SHA256/HMAC từ tutorial cũ nếu tài liệu VNPay Sandbox hiện tại đã thay đổi.

---

# 9. Chuyển sang trang VNPay Sandbox

Sau khi Vue nhận:

```json
{
    "paymentUrl": "..."
}
```

Vue thực hiện:

```javascript
window.location.href = response.paymentUrl;
```

Luồng:

```text
Vue Website
      │
      │ Nhấn thanh toán
      ▼
Spring Boot
      │
      ▼
Tạo VNPay Payment URL
      │
      ▼
Vue nhận URL
      │
      ▼
window.location.href
      │
      ▼
VNPay Sandbox
```

Không mở trang thanh toán bằng iframe nếu VNPay không cho phép.

---

# 10. Chọn ngân hàng và nhập thẻ test

Trên trang VNPay Sandbox:

```text
VNPay Sandbox
      │
      ▼
Chọn phương thức thanh toán
      │
      ▼
Chọn ngân hàng
      │
      ▼
Hiển thị form thanh toán
      │
      ├── Số thẻ TEST
      ├── Tên chủ thẻ
      ├── Ngày phát hành
      └── OTP TEST
      │
      ▼
Xác nhận thanh toán
```

Hãy tìm trong **tài liệu VNPay chính thức hiện tại**:

1. Những ngân hàng nào hỗ trợ Sandbox/Test.
2. Mã `vnp_BankCode` của từng ngân hàng nếu có.
3. Bộ thông tin thẻ test chính thức.
4. Thẻ nào cho kết quả thanh toán thành công.
5. Thẻ nào cho kết quả:
   - Không đủ số dư.
   - Thẻ bị khóa.
   - Thẻ chưa kích hoạt.
   - Thẻ hết hạn.
   - Sai OTP.
   - Người dùng hủy.
6. OTP test tương ứng nếu được công bố.

Không tự tạo số thẻ và gọi chúng là thẻ test chính thức của VNPay.

Nếu VNPay không công bố test card cho một trạng thái cụ thể, phải ghi rõ:

```text
VNPay không công bố dữ liệu test chính thức cho trạng thái này.
```

---

# 11. VNPay Return URL

Tạo route Vue:

```text
/payment/vnpay/result
```

Ví dụ:

```text
http://localhost:5173/payment/vnpay/result
```

Hoặc URL phù hợp với môi trường hiện tại.

Sau khi người dùng thanh toán:

```text
VNPay Sandbox
      │
      │ Thanh toán thành công/thất bại
      ▼
vnp_ReturnUrl
      │
      ▼
Vue PaymentResultView
```

VNPay có thể truyền các tham số kết quả trên URL.

Tuy nhiên:

```text
KHÔNG ĐƯỢC
```

coi việc:

```text
vnp_ResponseCode = 00
```

trên URL là bằng chứng duy nhất để cập nhật:

```text
Payment = PAID
```

Phải xác minh ở Backend.

---

# 12. PaymentResultView.vue

Tạo:

```text
src/views/PaymentResultView.vue
```

Khi người dùng quay về website:

```text
VNPay
   ↓
PaymentResultView.vue
   ↓
Hiển thị:
"Đang xác minh thanh toán..."
   ↓
Vue gọi Backend
   ↓
GET /api/payments/{txnRef}/status
   ↓
Spring Boot
   ↓
Verify kết quả
   ↓
MySQL
```

Sau đó:

## Nếu thành công

Hiển thị:

```text
✓ THANH TOÁN THÀNH CÔNG

Mã đơn hàng: ORD00123

Phương thức:
VNPay

Số tiền:
500.000 VNĐ

Trạng thái:
Đã thanh toán

[ XEM ĐƠN HÀNG ]

[ TIẾP TỤC MUA SẮM ]
```

Khi bấm:

```text
XEM ĐƠN HÀNG
```

chuyển tới:

```text
/orders/123
```

Hoặc tự động chuyển sau vài giây nếu tôi muốn.

Ví dụ:

```text
Thanh toán thành công
      ↓
Hiển thị kết quả 3 giây
      ↓
Tự động chuyển
      ↓
/orders/123
```

## Nếu thất bại

```text
✕ THANH TOÁN THẤT BẠI

Đơn hàng chưa được thanh toán.

Bạn có thể thử lại.

[ THANH TOÁN LẠI ]

[ QUAY VỀ ĐƠN HÀNG ]
```

---

# 13. Backend xử lý kết quả VNPay

Tạo API phù hợp, ví dụ:

```http
GET /api/payments/vnpay/return
```

hoặc thiết kế kiến trúc tốt hơn nếu cần.

Backend nhận các tham số VNPay trả về.

Thực hiện:

```text
1. Lấy vnp_TxnRef.

2. Tìm Payment trong Database.

3. Kiểm tra Payment tồn tại.

4. Lấy các tham số cần verify.

5. Loại trừ vnp_SecureHash và các trường theo quy tắc tài liệu.

6. Sắp xếp lại tham số đúng quy tắc.

7. Tạo lại Secure Hash.

8. So sánh Secure Hash.

9. Nếu signature sai:
   → Không cập nhật Payment.

10. Kiểm tra amount.

11. Kiểm tra vnp_TxnRef.

12. Kiểm tra response code.

13. Nếu thành công:
    Payment = PAID
    Order = PAID

14. Nếu thất bại:
    Payment = FAILED hoặc CANCELLED.

15. Không xử lý cùng một giao dịch nhiều lần.
```

---

# 14. Idempotency

Phải xử lý trường hợp:

```text
VNPay gửi/redirect kết quả nhiều lần
```

hoặc:

```text
Người dùng refresh PaymentResultView
```

Ví dụ:

```text
Lần 1
vnp_TxnRef = ORDER123
       ↓
Payment = PAID


Lần 2
vnp_TxnRef = ORDER123
       ↓
Không tạo Payment mới

Không cập nhật Order lần thứ hai

Không cộng tiền lần thứ hai
```

Sử dụng:

```text
vnp_TxnRef
```

và transaction ID phù hợp làm dữ liệu đối chiếu.

---

# 15. Cấu trúc Spring Boot

Đề xuất:

```text
backend/
└── src/main/java/
    └── com.example.project/
        │
        ├── controller/
        │   └── VNPayController.java
        │
        ├── service/
        │   ├── PaymentService.java
        │   └── VNPayService.java
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
        │   ├── VNPayCreateRequest.java
        │   ├── VNPayCreateResponse.java
        │   └── PaymentStatusResponse.java
        │
        ├── config/
        │   └── VNPayConfig.java
        │
        └── util/
            └── VNPayUtil.java
```

---

# 16. Cấu trúc Vue

```text
frontend/
└── src/
    │
    ├── views/
    │   ├── CheckoutView.vue
    │   ├── PaymentResultView.vue
    │   └── OrderDetailView.vue
    │
    ├── services/
    │   └── paymentService.js
    │
    ├── components/
    │   └── payment/
    │       ├── PaymentMethodSelector.vue
    │       └── VNPayBankSelector.vue
    │
    └── router/
        └── index.js
```

---

# 17. API Frontend

Tạo:

```text
src/services/paymentService.js
```

Ví dụ:

```javascript
import axios from "axios";

const API_URL = "http://localhost:8080/api";

export const createVNPayPayment = async (
    orderId,
    bankCode = ""
) => {

    const response = await axios.post(
        `${API_URL}/payments/vnpay/create`,
        {
            orderId,
            bankCode
        }
    );

    return response.data;
};


export const getPaymentStatus = async (
    txnRef
) => {

    const response = await axios.get(
        `${API_URL}/payments/${txnRef}/status`
    );

    return response.data;
};
```

Sau đó giải thích cách tích hợp vào `CheckoutView.vue`.

---

# 18. Luồng thành công hoàn chỉnh

Mô phỏng:

```text
Người dùng đặt hàng

Order:
ORD00123

Tổng tiền:
500.000 VNĐ

        │
        ▼

Chọn:

VNPay

        │
        ▼

Chọn ngân hàng:

NCB hoặc ngân hàng Sandbox được VNPay hỗ trợ

        │
        ▼

Nhấn:

THANH TOÁN

        │
        ▼

Vue
POST /api/payments/vnpay/create

        │
        ▼

Spring Boot

Tạo:

vnp_TxnRef = ORD00123_...

Payment = PENDING

        │
        ▼

Tạo VNPay Sandbox URL

        │
        ▼

Vue redirect

        │
        ▼

VNPay Sandbox

        │
        ▼

Chọn ngân hàng

        │
        ▼

Nhập thông tin thẻ TEST

        │
        ▼

Nhập OTP TEST

        │
        ▼

THANH TOÁN

        │
        ▼

VNPay xử lý

        │
        ▼

SUCCESS

        │
        ▼

Redirect về:

/payment/vnpay/result

        │
        ▼

PaymentResultView

        │
        ▼

GET /api/payments/{txnRef}/status

        │
        ▼

Spring Boot Verify

        │
        ▼

MySQL

Payment = PAID

Order = PAID

        │
        ▼

Hiển thị:

THANH TOÁN THÀNH CÔNG

        │
        ▼

Tự động chuyển hoặc bấm:

XEM ĐƠN HÀNG

        │
        ▼

/ orders / 123
```

---

# 19. Xử lý các trường hợp lỗi

Phải hướng dẫn và code xử lý:

## Case 1 — Thanh toán thành công

```text
Payment = PAID
Order = PAID
```

## Case 2 — Người dùng hủy

```text
Payment = CANCELLED
```

hoặc trạng thái phù hợp theo response code VNPay.

## Case 3 — Không đủ tiền

```text
Payment = FAILED
```

## Case 4 — Thẻ không hợp lệ

```text
Payment = FAILED
```

## Case 5 — Sai OTP

```text
Payment = FAILED
```

hoặc giữ trạng thái theo đúng hành vi Sandbox.

## Case 6 — Secure Hash sai

```text
Không cập nhật Payment.
Ghi log lỗi.
Trả lỗi xác thực.
```

## Case 7 — Amount bị thay đổi

Ví dụ Vue gửi:

```json
{
    "orderId": 123,
    "amount": 1000
}
```

Backend phải:

```text
Bỏ qua amount từ Vue.
```

và lấy:

```text
orders.total_amount
```

từ MySQL.

## Case 8 — Người dùng refresh trang kết quả

Không được tạo giao dịch mới.

## Case 9 — Payment đã PAID

Không được thanh toán lại cùng Payment nếu nghiệp vụ không cho phép.

---

# 20. Bảo mật

Đảm bảo:

```text
Vue
│
├── Không chứa vnp_HashSecret
├── Không tạo Secure Hash
├── Không lưu Secret
└── Không quyết định Payment = PAID
```

Chỉ:

```text
Spring Boot
```

được:

```text
✓ Tạo Secure Hash
✓ Verify Secure Hash
✓ Cập nhật Payment
✓ Cập nhật Order
✓ Gọi các API cần thiết
```

Không log:

```text
vnp_HashSecret
Thông tin thẻ
OTP
```

Không tự tạo form trên website để thu thập thẻ ngân hàng thật.

Việc nhập:

```text
Số thẻ
Tên chủ thẻ
Ngày phát hành
OTP
```

phải diễn ra trên **trang VNPay Sandbox/Gateway** nếu đó là flow chính thức.

---

# 21. Hướng dẫn theo từng bước

Không đưa toàn bộ code của hệ thống một lần.

Hãy hướng dẫn tôi theo từng bước:

```text
STEP 1
Kiểm tra và cấu hình VNPay Sandbox Credentials
        ↓
STEP 2
Cấu hình application.properties / Environment Variables
        ↓
STEP 3
Thiết kế Payment Entity
        ↓
STEP 4
Tạo Payment Repository
        ↓
STEP 5
Tạo VNPayConfig
        ↓
STEP 6
Tạo VNPayUtil để tạo Secure Hash
        ↓
STEP 7
Tạo VNPayService
        ↓
STEP 8
Tạo API Create Payment
        ↓
STEP 9
Test API bằng Postman
        ↓
STEP 10
Tạo Vue paymentService.js
        ↓
STEP 11
Tạo giao diện chọn VNPay
        ↓
STEP 12
Thêm chọn ngân hàng nếu VNPay API hỗ trợ
        ↓
STEP 13
Redirect sang VNPay Sandbox
        ↓
STEP 14
Test bằng thẻ test chính thức
        ↓
STEP 15
Tạo Return URL
        ↓
STEP 16
Tạo PaymentResultView.vue
        ↓
STEP 17
Verify Secure Hash ở Backend
        ↓
STEP 18
Cập nhật Payment = PAID
        ↓
STEP 19
Cập nhật Order = PAID
        ↓
STEP 20
Tự động chuyển đến trang chi tiết đơn hàng
        ↓
STEP 21
Test các trường hợp thất bại
```

Ở mỗi STEP phải:

1. Giải thích mục đích.
2. Nói rõ file nào cần tạo hoặc sửa.
3. Cung cấp code hoàn chỉnh của STEP đó.
4. Giải thích code.
5. Hướng dẫn cách chạy.
6. Cho biết kết quả mong đợi.
7. Liệt kê lỗi thường gặp.
8. Không chuyển sang STEP tiếp theo cho đến khi tôi xác nhận STEP hiện tại đã chạy thành công.

---

# 22. Yêu cầu đặc biệt

Website của tôi đã có hoặc đang tích hợp nhiều phương thức:

```text
COD
MoMo Test
ZaloPay Sandbox
VNPay Sandbox
```

Vì vậy hãy thiết kế theo kiến trúc chung:

```text
                    PAYMENT SERVICE
                           │
           ┌───────────────┼───────────────┐
           │               │               │
           ▼               ▼               ▼
        MoMoService   ZaloPayService   VNPayService
           │               │               │
           └───────────────┼───────────────┘
                           │
                           ▼
                        Payment
                           │
                           ▼
                         MySQL
```

Không tạo ba hệ thống `Order` khác nhau.

Tất cả phải sử dụng chung:

```text
Order
Payment
PaymentStatus
PaymentProvider
```

Nhưng mỗi cổng thanh toán có:

```text
MoMoService
ZaloPayService
VNPayService
```

riêng để xử lý:

```text
Credentials
Signature
Create Payment
Callback/Return
Query Status
```

---

# MỤC TIÊU CUỐI CÙNG

Tôi muốn có một website bán hàng hoạt động theo luồng:

```text
VUE 3
   │
   ▼
CHECKOUT
   │
   ├───────────── COD
   │
   ├───────────── MOMO TEST
   │                    │
   │                    ▼
   │              MoMo Test Wallet
   │
   ├───────────── ZALOPAY SANDBOX
   │                    │
   │                    ▼
   │              ZaloPay Test
   │
   └───────────── VNPAY SANDBOX
                        │
                        ▼
                  Chọn ngân hàng
                        │
                        ▼
                  Nhập thẻ TEST
                        │
                        ▼
                    VNPay Sandbox
                        │
                        ▼
                 Thanh toán thành công
                        │
                        ▼
                  Tự động Redirect
                        │
                        ▼
                 PaymentResultView
                        │
                        ▼
                  Backend Verify
                        │
                        ▼
                   Payment = PAID
                   Order = PAID
                        │
                        ▼
                   XEM ĐƠN HÀNG
```

Hãy ưu tiên tài liệu **VNPay chính thức và mới nhất**. Không sử dụng thông tin thẻ test, endpoint, thuật toán Secure Hash hoặc mã ngân hàng từ tutorial cũ nếu không còn phù hợp.