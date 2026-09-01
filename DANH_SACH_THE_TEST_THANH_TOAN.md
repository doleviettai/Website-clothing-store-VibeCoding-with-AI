# 💳 TÀI LIỆU DANH SÁCH THẺ TEST ATM SANDBOX (MOMO & ZALOPAY)

Document tra cứu số thẻ test thử nghiệm dành cho **Tester / Developer** khi thực hiện kiểm thử chức năng Đặt hàng & Thanh toán trên Website Bán Hàng Clothing Store (`Vue 3 + Spring Boot + MySQL`).

---

> [!NOTE]
> Tất cả các thông tin thẻ dưới đây là **thông tin thử nghiệm ảo Sandbox**. Giao dịch sẽ **KHÔNG trừ tiền thật** và không ảnh hưởng tới tài khoản ngân hàng thực tế.

---

## 💙 1. DANH SÁCH THẺ TEST ATM CỔNG ZALOPAY SANDBOX (VNG)

**Ngân Hàng Khuyên Dùng**: VietinBank (`CTG`) / Vietcombank (`VCB`) / Napas  
**Mã OTP Test mặc định**: `111111` (hoặc `123456` / `100000`)

| Kịch Bản Kiểm Thử | Số Thẻ Test (16 Số) | Tên Chủ Thẻ | Ngày Phát Hành | Mã OTP Test | Phản Hồi Từ Hệ Thống ZaloPay |
| :--- | :--- | :--- | :--- | :--- | :--- |
| ✅ **Thanh toán thành công** | `9704540000000062` | `NGUYEN VAN A` | `10/18` | **`111111`** | Mở OTP ➔ Nhập `111111` ➔ Chuyển đơn `PAID` |
| ❌ **Thẻ bị mất / đánh cắp** | `9704540000000013` | `NGUYEN VAN A` | `10/18` | N/A | Báo lỗi: *Thẻ đã bị khai báo MẤT hoặc BỊ ĐÁNH CẮP.* |
| ❌ **Thẻ bị mất / đánh cắp** | `9704540000000021` | `NGUYEN VAN A` | `10/18` | N/A | Báo lỗi: *Thẻ đã bị khai báo MẤT hoặc BỊ ĐÁNH CẮP.* |
| ❌ **Thẻ bị mất / đánh cắp** | `9704541000000029` | `NGUYEN VAN A` | `10/18` | N/A | Báo lỗi: *Thẻ đã bị khai báo MẤT hoặc BỊ ĐÁNH CẮP.* |
| ❌ **Thẻ bị mất / đánh cắp** | `9704541000000052` | `NGUYEN VAN A` | `10/18` | N/A | Báo lỗi: *Thẻ đã bị khai báo MẤT hoặc BỊ ĐÁNH CẮP.* |
| ❌ **Thẻ bị mất / đánh cắp** | `9704541000000060` | `NGUYEN VAN A` | `10/18` | N/A | Báo lỗi: *Thẻ đã bị khai báo MẤT hoặc BỊ ĐÁNH CẮP.* |
| ❌ **Thẻ bị mất / đánh cắp** | `9704541000000086` | `NGUYEN VAN A` | `10/18` | N/A | Báo lỗi: *Thẻ đã bị khai báo MẤT hoặc BỊ ĐÁNH CẮP.* |
| ⏱️ **Giao dịch Timeout** | `9704540000000039` | `NGUYEN VAN A` | `10/18` | N/A | Báo lỗi: *Quá thời gian xử lý phản hồi (Timeout).* |
| ⏱️ **Giao dịch Timeout** | `9704541000000037` | `NGUYEN VAN A` | `10/18` | N/A | Báo lỗi: *Quá thời gian xử lý phản hồi (Timeout).* |
| ⏱️ **Giao dịch Timeout** | `9704540000000054` | `NGUYEN VAN A` | `10/18` | N/A | Báo lỗi: *Quá thời gian xử lý phản hồi (Timeout).* |
| 💸 **Hết tiền / Thiếu số dư** | `9704540000000047` | `NGUYEN VAN A` | `10/18` | N/A | Báo lỗi: *Tài khoản không đủ số dư để thanh toán.* |
| 💸 **Hết tiền / Thiếu số dư** | `9704541000000011` | `NGUYEN VAN A` | `10/18` | N/A | Báo lỗi: *Tài khoản không đủ số dư để thanh toán.* |
| 💸 **Hết tiền / Thiếu số dư** | `9704541000000045` | `NGUYEN VAN A` | `10/18` | N/A | Báo lỗi: *Tài khoản không đủ số dư để thanh toán.* |

---

## 💖 2. DANH SÁCH THẺ TEST ATM CỔNG MOMO SANDBOX

**Ngân Hàng Khuyên Dùng**: Agribank (`VAB`) / MBBank (`MBB`) / Napas  
**Mã OTP Test mặc định**: `100000` (hoặc `123456`)

| Kịch Bản Kiểm Thử | Số Thẻ Test (16 Số) | Tên Chủ Thẻ | Ngày Phát Hành | Mã OTP Test | Phản Hồi Từ Hệ Thống MoMo |
| :--- | :--- | :--- | :--- | :--- | :--- |
| ✅ **Thanh toán thành công** | `9704000000000018` | `NGUYEN VAN A` | `03/07` | **`100000`** | Mở OTP ➔ Nhập `100000` ➔ Chuyển đơn `PAID` |
| 🔒 **Thẻ bị khóa** | `9704000000000026` | `NGUYEN VAN A` | `03/07` | N/A | Báo lỗi: *Thẻ của quý khách đã bị KHÓA.* |
| 💸 **Tài khoản không đủ số dư** | `9704000000000034` | `NGUYEN VAN A` | `03/07` | N/A | Báo lỗi: *Tài khoản không đủ số dư để thanh toán.* |
| ⚠️ **Vượt hạn mức trong ngày** | `9704000000000042` | `NGUYEN VAN A` | `03/07` | N/A | Báo lỗi: *Vượt quá hạn mức giao dịch trong ngày.* |

---

## 💙 3. DANH SÁCH THẺ TEST CỔNG VNPAY SANDBOX (NCB / NAPAS)

**Ngân Hàng Khuyên Dùng**: Ngan hang NCB (`NCB`) / Vietcombank (`VCB`) / Napas  
**Mã OTP Test mặc định**: **`123456`**

| Kịch Bản Kiểm Thử | Số Thẻ Test (16 Số) | Tên Chủ Thẻ | Hạn Thẻ | Mã OTP Test | Phản Hồi Từ Hệ Thống VNPAY |
| :--- | :--- | :--- | :--- | :--- | :--- |
| ✅ **Thanh toán thành công** | `9704198526191432` | `NGUYEN VAN A` | `07/15` | **`123456`** | Mở OTP ➔ Nhập `123456` ➔ Chuyển đơn `PAID` |
| 🔒 **Thẻ bị khóa** | `9704198526191433` | `NGUYEN VAN A` | `07/15` | N/A | Báo lỗi: *Thẻ của quý khách đã bị KHÓA.* |
| 💸 **Tài khoản không đủ số dư** | `9704198526191434` | `NGUYEN VAN A` | `07/15` | N/A | Báo lỗi: *Tài khoản không đủ số dư để thanh toán.* |
| ⚠️ **Vượt hạn mức trong ngày** | `9704198526191435` | `NGUYEN VAN A` | `07/15` | N/A | Báo lỗi: *Vượt quá hạn mức giao dịch trong ngày.* |

---

## 📋 4. HƯỚNG DẪN CÁC BƯỚC TEST DÀNH CHO TESTER

```text
1. Khách hàng vào website -> Thêm sản phẩm vào giỏ hàng -> Chọn "Checkout" (/checkout).
2. Nhập thông tin người nhận -> Chọn 1 trong 2 cổng:
   - "Thanh toán Ví Điện Tử ZaloPay (Sandbox)"
   - "Thanh toán Ví Điện Tử MoMo (Sandbox)"
3. Nhấn "XÁC NHẬN ĐẶT HÀNG".
4. Trên màn hình Cổng Thanh Toán Sandbox:
   - Chọn Tab "Thẻ ATM Ngân Hàng Nội Địa".
   - Tra cứu bảng thẻ ở trên và nhập Số thẻ, Tên chủ thẻ, Hạn thẻ.
   - Nhấn "THANH TOÁN THẺ ATM".
5. Nếu là thẻ hợp lệ -> Nhập mã OTP tương ứng -> Nhấn "XÁC NHẬN THANH TOÁN".
6. Kiểm tra phản hồi:
   - Đơn hàng chuyển sang trạng thái "PAID" và tự động điều hướng về /payment-result -> /orders.
   - Admin kiểm tra nhật ký giao dịch xuất hiện tại /admin/orders và /admin/payments.
```
