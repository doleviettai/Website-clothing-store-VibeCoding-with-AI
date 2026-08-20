# Clothing Store

Clothing Store là website thương mại điện tử chuyên bán quần áo, được xây dựng theo mô hình frontend và backend tách biệt.

Dự án hướng đến hai nhóm người dùng:

- `CLIENT`: khách hàng sử dụng website để xem sản phẩm, yêu thích, đánh giá, mua hàng và quản lý tài khoản.
- `ADMIN`: quản trị viên quản lý toàn bộ dữ liệu và hoạt động của cửa hàng.

Dự án được phát triển theo từng phần nhỏ. Mỗi chức năng sẽ được giải thích, viết code, kiểm tra và hoàn thiện trước khi chuyển sang chức năng tiếp theo.

---

# 1. Mục tiêu dự án

Clothing Store được xây dựng nhằm:

- Tạo một website bán quần áo hoàn chỉnh.
- Tách biệt frontend và backend.
- Xây dựng hệ thống đăng ký, đăng nhập và phân quyền.
- Quản lý danh mục, banner, thương hiệu và sản phẩm.
- Hỗ trợ sản phẩm có nhiều màu sắc và kích thước.
- Hỗ trợ tìm kiếm và lọc sản phẩm bằng API.
- Hỗ trợ danh sách yêu thích riêng cho từng khách hàng.
- Hỗ trợ đánh giá sản phẩm.
- Hỗ trợ giỏ hàng.
- Hỗ trợ mua ngay một sản phẩm.
- Hỗ trợ thanh toán nhiều sản phẩm trong giỏ hàng.
- Quản lý đơn hàng và thanh toán.
- Thiết kế database có khả năng mở rộng lâu dài.
- Giúp người học hiểu rõ từng phần code thay vì sao chép toàn bộ dự án.

---

# 2. Công nghệ sử dụng

## 2.1. Frontend

Frontend sử dụng:

- Vue.js
- Vue Router
- Pinia
- Axios
- HTML
- CSS
- JavaScript

### Vue.js

Vue.js được sử dụng để xây dựng giao diện website.

### Vue Router

Vue Router được sử dụng để:

- Chuyển trang mà không tải lại toàn bộ website.
- Phân chia route dành cho ADMIN và CLIENT.
- Bảo vệ các route yêu cầu đăng nhập.
- Kiểm tra quyền trước khi truy cập trang quản trị.

### Pinia

Pinia được sử dụng để lưu trạng thái dùng chung như:

- Người dùng đang đăng nhập.
- Vai trò của người dùng.
- Access token.
- Thông tin hồ sơ.
- Giỏ hàng.
- Danh sách yêu thích.
- Bộ lọc sản phẩm.

### Axios

Axios được sử dụng để gọi REST API từ Spring Boot.

Ví dụ:

```text
Vue.js
   ↓
Axios gửi HTTP request
   ↓
Spring Boot xử lý request
   ↓
MySQL trả dữ liệu
   ↓
Spring Boot trả JSON
   ↓
Vue.js cập nhật giao diện
```

Cách tải dữ liệu này hoạt động tương tự AJAX. Website không cần tải lại toàn bộ trang khi tìm kiếm, lọc hoặc chuyển trang sản phẩm.

---

## 2.2. Backend

Backend sử dụng:

- Java
- Spring Boot
- Spring Web
- Spring Security
- Spring Data JPA
- Spring Validation
- MySQL Driver
- Flyway
- Maven

### Spring Web

Spring Web được sử dụng để xây dựng REST API.

### Spring Security

Spring Security chịu trách nhiệm:

- Xác thực người dùng.
- Kiểm tra access token.
- Phân quyền ADMIN và CLIENT.
- Bảo vệ API.
- Mã hóa mật khẩu.
- Chặn người dùng không đủ quyền.

### Spring Data JPA

Spring Data JPA được sử dụng để:

- Ánh xạ bảng database thành Java Entity.
- Thực hiện truy vấn database.
- Quản lý quan hệ giữa các bảng.
- Hỗ trợ phân trang và sắp xếp.

### Spring Validation

Spring Validation được sử dụng để kiểm tra dữ liệu đầu vào.

Ví dụ:

- Email không được để trống.
- Email phải đúng định dạng.
- Mật khẩu phải đủ độ dài.
- Giá sản phẩm không được âm.
- Số lượng sản phẩm phải lớn hơn 0.
- Rating phải từ 1 đến 5.

### Flyway

Flyway được sử dụng để quản lý lịch sử thay đổi database.

Mỗi thay đổi database sẽ được viết trong một file migration.

Ví dụ:

```text
V1__create_users_and_roles.sql
V2__create_refresh_tokens.sql
V3__create_categories.sql
```

Không chỉnh sửa trực tiếp database bằng tay sau khi dự án đã bắt đầu sử dụng Flyway.

---

## 2.3. Database

Database sử dụng:

- MySQL

MySQL lưu trữ:

- Người dùng.
- Vai trò.
- Phiên đăng nhập.
- Địa chỉ giao hàng.
- Chuyên mục.
- Thương hiệu.
- Banner.
- Sản phẩm.
- Hình ảnh sản phẩm.
- Màu sắc.
- Kích thước.
- Biến thể sản phẩm.
- Tồn kho.
- Sản phẩm yêu thích.
- Đánh giá.
- Giỏ hàng.
- Đơn hàng.
- Lịch sử đơn hàng.
- Thanh toán.
- Sự kiện thanh toán.

---

# 3. Kiến trúc tổng quát

```text
┌──────────────────────────┐
│      Vue.js Frontend     │
│                          │
│  Client UI               │
│  Admin UI                │
│  Vue Router              │
│  Pinia                    │
│  Axios                    │
└─────────────┬────────────┘
              │
              │ HTTPS / JSON
              ▼
┌──────────────────────────┐
│   Spring Boot Backend    │
│                          │
│  REST API                │
│  Spring Security         │
│  Business Logic          │
│  Validation              │
│  JPA                     │
└─────────────┬────────────┘
              │
              │ SQL
              ▼
┌──────────────────────────┐
│      MySQL Database      │
└──────────────────────────┘
```

Frontend không được kết nối trực tiếp đến MySQL.

Mọi dữ liệu đều phải đi qua backend Spring Boot.

---

# 4. Phân quyền hệ thống

Hệ thống ban đầu có hai vai trò:

| Vai trò | Mô tả |
|---|---|
| `ADMIN` | Quản trị viên |
| `CLIENT` | Khách hàng |

Sau khi đăng nhập thành công:

```text
ADMIN  → chuyển đến /admin
CLIENT → chuyển đến /
```

Frontend sử dụng vai trò để hiển thị giao diện phù hợp.

Backend vẫn phải kiểm tra quyền của từng API.

Việc ẩn nút hoặc ẩn trang trên frontend không đủ để bảo vệ hệ thống.

Ví dụ:

- CLIENT không được gọi API tạo sản phẩm.
- CLIENT không được chỉnh sửa đơn hàng của người khác.
- CLIENT không được xem danh sách toàn bộ người dùng.
- CLIENT không được tự gán quyền ADMIN.
- ADMIN không được xem mật khẩu gốc của người dùng.

---

# 5. Chức năng ADMIN

## 5.1. Dashboard tổng quan

Dashboard hiển thị tình trạng chung của cửa hàng.

Các dữ liệu dự kiến:

- Tổng số người dùng.
- Tổng số khách hàng đang hoạt động.
- Tổng số sản phẩm.
- Tổng số chuyên mục.
- Tổng số thương hiệu.
- Tổng số đơn hàng.
- Tổng doanh thu.
- Doanh thu theo ngày.
- Doanh thu theo tháng.
- Số đơn hàng mới.
- Số đơn hàng đang xử lý.
- Số đơn hàng đang giao.
- Số đơn hàng đã hoàn thành.
- Số đơn hàng đã hủy.
- Số sản phẩm sắp hết hàng.
- Sản phẩm bán chạy.
- Sản phẩm được yêu thích nhiều nhất.
- Sản phẩm được đánh giá cao nhất.
- Đơn hàng mới nhất.
- Khách hàng mới nhất.

Dashboard không cần bảng riêng trong database.

Dữ liệu dashboard được tổng hợp từ các bảng hiện có.

---

## 5.2. Quản lý chuyên mục sản phẩm

ADMIN có thể:

- Thêm chuyên mục.
- Xem danh sách chuyên mục.
- Xem chi tiết chuyên mục.
- Sửa chuyên mục.
- Xóa mềm chuyên mục.
- Tìm kiếm chuyên mục theo tên.
- Lọc theo trạng thái.
- Sắp xếp thứ tự hiển thị.
- Tạo chuyên mục cha.
- Tạo chuyên mục con.
- Bật hoặc tắt chuyên mục.

Ví dụ:

```text
Thời trang nam
├── Áo nam
│   ├── Áo thun
│   ├── Áo sơ mi
│   └── Áo khoác
└── Quần nam
    ├── Quần jean
    ├── Quần short
    └── Quần tây
```

Chuyên mục có sản phẩm không nên bị xóa vật lý.

Khi ADMIN xóa chuyên mục, hệ thống sẽ:

- Xóa mềm.
- Hoặc chuyển trạng thái thành `INACTIVE`.

---

## 5.3. Quản lý banner

ADMIN có thể:

- Thêm banner.
- Xem danh sách banner.
- Xem chi tiết banner.
- Sửa banner.
- Xóa mềm banner.
- Tìm kiếm banner theo tiêu đề.
- Lọc banner theo trạng thái.
- Bật hoặc tắt banner.
- Sắp xếp thứ tự hiển thị.
- Chọn vị trí hiển thị.
- Thiết lập ngày bắt đầu.
- Thiết lập ngày kết thúc.
- Thêm đường dẫn khi nhấn vào banner.
- Sử dụng ảnh riêng cho desktop và mobile.

Các vị trí banner dự kiến:

```text
HOME_TOP
HOME_MIDDLE
CATEGORY_TOP
```

Banner chỉ được hiển thị phía CLIENT khi:

- Banner có trạng thái `ACTIVE`.
- Chưa bị xóa mềm.
- Đã đến thời gian bắt đầu.
- Chưa hết thời gian hiển thị.

---

## 5.4. Quản lý thương hiệu

Vì CLIENT có thể lọc sản phẩm theo hãng, hệ thống cần chức năng quản lý thương hiệu.

ADMIN có thể:

- Thêm thương hiệu.
- Xem danh sách thương hiệu.
- Xem chi tiết thương hiệu.
- Sửa thương hiệu.
- Xóa mềm thương hiệu.
- Tìm kiếm theo tên.
- Lọc theo trạng thái.
- Thêm logo thương hiệu.
- Bật hoặc tắt thương hiệu.

Ví dụ:

```text
Nike
Adidas
Puma
Uniqlo
Local Brand
```

---

## 5.5. Quản lý sản phẩm

ADMIN có thể:

- Thêm sản phẩm.
- Xem danh sách sản phẩm.
- Xem chi tiết sản phẩm.
- Sửa sản phẩm.
- Xóa mềm sản phẩm.
- Tìm kiếm theo tên.
- Tìm kiếm theo mã SKU.
- Lọc theo chuyên mục.
- Lọc theo thương hiệu.
- Lọc theo trạng thái.
- Lọc theo khoảng giá.
- Lọc theo tồn kho.
- Sắp xếp theo ngày tạo.
- Quản lý hình ảnh.
- Quản lý màu sắc.
- Quản lý kích thước.
- Quản lý biến thể.
- Quản lý giá bán.
- Quản lý giá khuyến mãi.
- Quản lý tồn kho.
- Đánh dấu sản phẩm nổi bật.
- Xem số lượt yêu thích.
- Xem điểm đánh giá trung bình.
- Xem danh sách đánh giá.

Một sản phẩm có thể có nhiều biến thể.

Ví dụ:

```text
Áo thun Basic
├── Màu đen - Size S
├── Màu đen - Size M
├── Màu đen - Size L
├── Màu trắng - Size S
├── Màu trắng - Size M
└── Màu trắng - Size L
```

Mỗi biến thể có:

- SKU riêng.
- Giá riêng.
- Giá khuyến mãi riêng.
- Tồn kho riêng.
- Trạng thái riêng.

---

## 5.6. Quản lý đánh giá

ADMIN có thể:

- Xem danh sách đánh giá.
- Tìm kiếm đánh giá theo sản phẩm.
- Tìm kiếm theo người dùng.
- Lọc theo số sao.
- Lọc theo trạng thái.
- Xem nội dung đánh giá.
- Ẩn đánh giá vi phạm.
- Hiển thị lại đánh giá.
- Từ chối đánh giá không phù hợp.

ADMIN không được tự thay đổi nội dung đánh giá của khách hàng.

---

## 5.7. Quản lý người dùng

Người dùng tự tạo tài khoản thông qua chức năng đăng ký.

ADMIN không cần chức năng thêm người dùng trong phiên bản đầu tiên.

ADMIN có thể:

- Xem danh sách người dùng.
- Xem chi tiết người dùng.
- Sửa một số thông tin.
- Tìm kiếm theo tên.
- Tìm kiếm theo email.
- Tìm kiếm theo số điện thoại.
- Lọc theo trạng thái.
- Khóa tài khoản.
- Mở khóa tài khoản.
- Xóa mềm tài khoản.
- Xem lịch sử đơn hàng.
- Xem danh sách đánh giá.
- Xem danh sách yêu thích.

ADMIN không được:

- Xem mật khẩu gốc.
- Lấy trường `password_hash`.
- Đổi mật khẩu của người dùng thành một giá trị nhìn thấy được.
- Xóa vật lý người dùng đã có đơn hàng.

---

## 5.8. Quản lý đơn hàng

ADMIN có thể:

- Xem danh sách đơn hàng.
- Xem chi tiết đơn hàng.
- Tìm kiếm theo mã đơn hàng.
- Tìm kiếm theo tên khách hàng.
- Tìm kiếm theo số điện thoại.
- Lọc theo trạng thái đơn hàng.
- Lọc theo trạng thái thanh toán.
- Lọc theo phương thức thanh toán.
- Lọc theo khoảng ngày.
- Cập nhật trạng thái đơn hàng.
- Hủy đơn hàng.
- Ghi chú cho đơn hàng.
- Xem lịch sử trạng thái.
- Xem thông tin thanh toán.
- Xem địa chỉ giao hàng.

Đơn hàng không được xóa vật lý.

Lý do:

- Đơn hàng liên quan đến thanh toán.
- Đơn hàng liên quan đến doanh thu.
- Đơn hàng liên quan đến tồn kho.
- Đơn hàng là lịch sử mua hàng.
- Đơn hàng có thể được dùng để giải quyết khiếu nại.

Chức năng xóa trên giao diện quản trị nên được hiểu là:

- Hủy đơn hàng.
- Lưu trữ đơn hàng.
- Ẩn khỏi danh sách mặc định.
- Xóa mềm khi thật sự cần thiết.

---

## 5.9. Quản lý thanh toán

ADMIN có thể:

- Xem danh sách thanh toán.
- Xem chi tiết giao dịch.
- Tìm kiếm theo mã đơn hàng.
- Tìm kiếm theo mã giao dịch.
- Lọc theo phương thức thanh toán.
- Lọc theo nhà cung cấp.
- Lọc theo trạng thái.
- Lọc theo khoảng ngày.
- Kiểm tra giao dịch thành công.
- Kiểm tra giao dịch thất bại.
- Xem lý do thất bại.
- Đối chiếu số tiền thanh toán với đơn hàng.
- Theo dõi webhook từ cổng thanh toán.

---

# 6. Chức năng CLIENT

## 6.1. Đăng ký

Khách hàng có thể đăng ký tài khoản bằng:

- Họ tên.
- Email.
- Số điện thoại.
- Mật khẩu.
- Xác nhận mật khẩu.

Quy tắc:

- Email không được trùng.
- Số điện thoại không được trùng nếu đã được sử dụng.
- Mật khẩu phải được mã hóa.
- Người dùng đăng ký từ giao diện CLIENT chỉ nhận vai trò `CLIENT`.
- Người dùng không được tự gửi vai trò `ADMIN`.

---

## 6.2. Đăng nhập

Người dùng đăng nhập bằng:

- Email.
- Mật khẩu.

Sau khi đăng nhập:

- CLIENT được chuyển đến trang chủ.
- ADMIN được chuyển đến dashboard.
- Người dùng bị khóa không được đăng nhập.
- Người dùng đã xóa mềm không được đăng nhập.

---

## 6.3. Trang chủ

Trang chủ CLIENT cần có giao diện đẹp, dễ nhìn và phù hợp với website bán quần áo.

Các khu vực dự kiến:

- Header.
- Logo.
- Thanh điều hướng.
- Thanh tìm kiếm.
- Chuyên mục.
- Banner chính.
- Banner phụ.
- Sản phẩm mới.
- Sản phẩm nổi bật.
- Sản phẩm gợi ý.
- Sản phẩm được yêu thích nhiều nhất.
- Sản phẩm được đánh giá cao.
- Sản phẩm được yêu thích nhiều nhất theo từng chuyên mục.
- Footer.

Ví dụ:

```text
Trang chủ
├── Header
├── Banner chính
├── Chuyên mục nổi bật
├── Sản phẩm mới
├── Sản phẩm gợi ý
├── Sản phẩm được yêu thích nhiều nhất
├── Sản phẩm nam được yêu thích
├── Sản phẩm nữ được yêu thích
├── Sản phẩm được đánh giá cao
└── Footer
```

Trong phiên bản đầu, sản phẩm gợi ý được xác định bằng các tiêu chí đơn giản:

- Sản phẩm mới.
- Sản phẩm nổi bật.
- Sản phẩm có nhiều lượt yêu thích.
- Sản phẩm có điểm đánh giá cao.
- Sản phẩm cùng chuyên mục.
- Sản phẩm cùng thương hiệu.

Chưa cần sử dụng trí tuệ nhân tạo.

---

## 6.4. Danh sách sản phẩm

CLIENT có thể:

- Xem danh sách sản phẩm.
- Xem theo chuyên mục.
- Tìm kiếm bằng từ khóa.
- Lọc theo kích thước.
- Lọc theo màu sắc.
- Lọc theo thương hiệu.
- Lọc theo khoảng giá.
- Lọc theo trạng thái còn hàng.
- Lọc theo điểm đánh giá.
- Sắp xếp theo giá tăng dần.
- Sắp xếp theo giá giảm dần.
- Sắp xếp theo sản phẩm mới.
- Sắp xếp theo lượt yêu thích.
- Sắp xếp theo điểm đánh giá.
- Chuyển trang dữ liệu.

Dữ liệu được tải bằng Axios.

Khi khách hàng thay đổi bộ lọc, Vue.js chỉ tải lại danh sách sản phẩm thay vì tải lại toàn bộ website.

Luồng hoạt động:

```text
Khách hàng thay đổi bộ lọc
        ↓
Vue cập nhật điều kiện
        ↓
Axios gọi API
        ↓
Spring Boot truy vấn MySQL
        ↓
Spring Boot trả JSON
        ↓
Vue cập nhật danh sách sản phẩm
```

---

## 6.5. Tìm kiếm nhập tay

Khách hàng có thể nhập tên sản phẩm vào ô tìm kiếm.

Frontend sử dụng debounce để tránh gọi API sau mỗi ký tự.

Luồng dự kiến:

```text
Người dùng nhập từ khóa
        ↓
Chờ khoảng 300–500 ms
        ↓
Nếu người dùng không nhập tiếp
        ↓
Gọi API tìm kiếm
```

---

## 6.6. Chi tiết sản phẩm

Trang chi tiết sản phẩm hiển thị:

- Tên sản phẩm.
- Hình ảnh.
- Chuyên mục.
- Thương hiệu.
- Mô tả ngắn.
- Mô tả chi tiết.
- Giá bán.
- Giá khuyến mãi.
- Màu sắc.
- Kích thước.
- Số lượng còn lại.
- Trạng thái còn hàng.
- Điểm đánh giá trung bình.
- Số lượt đánh giá.
- Số lượt yêu thích.
- Danh sách đánh giá.
- Sản phẩm liên quan.

CLIENT có thể:

- Chọn màu.
- Chọn kích thước.
- Chọn số lượng.
- Thêm vào giỏ hàng.
- Mua ngay.
- Thêm vào danh sách yêu thích.
- Xóa khỏi danh sách yêu thích.
- Đánh giá sản phẩm khi đủ điều kiện.

---

## 6.7. Danh sách yêu thích

Mỗi khách hàng có danh sách yêu thích riêng.

CLIENT có thể:

- Thêm sản phẩm vào danh sách yêu thích.
- Xóa sản phẩm khỏi danh sách yêu thích.
- Xem toàn bộ sản phẩm đã yêu thích.
- Chuyển sản phẩm từ yêu thích sang giỏ hàng.
- Xem sản phẩm còn hoạt động hay đã ngừng bán.

Quy tắc:

- Người dùng phải đăng nhập.
- Một người dùng chỉ được yêu thích một sản phẩm một lần.
- Danh sách yêu thích của người dùng này không được hiển thị cho người dùng khác.

---

## 6.8. Đánh giá sản phẩm

CLIENT có thể:

- Đánh giá từ 1 đến 5 sao.
- Viết nội dung nhận xét.
- Sửa đánh giá của mình.
- Xóa mềm đánh giá của mình.
- Xem các đánh giá đã viết.

Quy tắc:

- Người dùng phải đăng nhập.
- Người dùng phải từng mua sản phẩm.
- Đơn hàng chứa sản phẩm phải hoàn thành.
- Một người dùng chỉ có một đánh giá cho mỗi sản phẩm.
- Người dùng chỉ được sửa đánh giá của mình.
- ADMIN có thể ẩn đánh giá vi phạm.

Ảnh đánh giá có thể được bổ sung sau khi chức năng đánh giá văn bản hoạt động ổn định.

---

## 6.9. Giỏ hàng

CLIENT có thể:

- Thêm biến thể sản phẩm vào giỏ hàng.
- Thay đổi số lượng.
- Xóa sản phẩm khỏi giỏ hàng.
- Chọn các sản phẩm cần thanh toán.
- Xem giá từng sản phẩm.
- Xem tổng tiền tạm tính.
- Kiểm tra tồn kho.

Giỏ hàng phải lưu biến thể sản phẩm.

Ví dụ:

```text
Áo thun Basic
Màu: Đen
Size: M
Số lượng: 2
```

Không chỉ lưu `productId`, vì cùng một sản phẩm có thể có nhiều kích thước và màu sắc.

---

## 6.10. Mua ngay một sản phẩm

Tại trang chi tiết sản phẩm, CLIENT có thể nhấn nút:

```text
Mua ngay
```

Luồng mua ngay:

```text
1. Chọn màu sắc.
2. Chọn kích thước.
3. Chọn số lượng.
4. Nhấn Mua ngay.
5. Chuyển đến trang thanh toán.
6. Chọn địa chỉ giao hàng.
7. Chọn phương thức thanh toán.
8. Xác nhận đơn hàng.
```

Sản phẩm mua ngay không bắt buộc phải được thêm vào giỏ hàng chính.

Backend vẫn phải kiểm tra lại:

- Sản phẩm còn hoạt động.
- Biến thể tồn tại.
- Giá hiện tại.
- Tồn kho.
- Số lượng.
- Tổng tiền.

---

## 6.11. Thanh toán giỏ hàng

CLIENT có thể chọn một hoặc nhiều sản phẩm trong giỏ hàng và thanh toán cùng lúc.

Luồng:

```text
1. Mở giỏ hàng.
2. Chọn sản phẩm cần mua.
3. Nhấn Thanh toán.
4. Chọn địa chỉ giao hàng.
5. Chọn phương thức thanh toán.
6. Backend kiểm tra giá.
7. Backend kiểm tra tồn kho.
8. Backend tính lại tổng tiền.
9. Backend tạo đơn hàng.
10. Backend tạo các order item.
11. Backend xử lý thanh toán.
```

Một đơn hàng có thể chứa nhiều loại sản phẩm.

---

## 6.12. Hồ sơ cá nhân

CLIENT có thể:

- Xem thông tin cá nhân.
- Sửa họ tên.
- Sửa số điện thoại.
- Thay đổi ảnh đại diện.
- Đổi mật khẩu.
- Quản lý địa chỉ giao hàng.
- Xem lịch sử đơn hàng.
- Xem chi tiết đơn hàng.
- Xem danh sách yêu thích.
- Xem danh sách đánh giá.

Email đăng nhập không thay đổi trực tiếp trong phiên bản đầu tiên.

---

# 7. Trạng thái dữ liệu

## 7.1. Trạng thái người dùng

```text
PENDING
ACTIVE
LOCKED
INACTIVE
```

## 7.2. Trạng thái sản phẩm

```text
DRAFT
ACTIVE
INACTIVE
```

## 7.3. Trạng thái đơn hàng

```text
PENDING
CONFIRMED
PROCESSING
SHIPPING
COMPLETED
CANCELLED
```

## 7.4. Trạng thái thanh toán

```text
UNPAID
PENDING
PAID
FAILED
REFUNDED
PARTIALLY_REFUNDED
```

## 7.5. Trạng thái đánh giá

```text
PENDING
VISIBLE
HIDDEN
REJECTED
```

---

# 8. Cấu trúc thư mục dự kiến

```text
clothing-store/
├── backend/
│   ├── pom.xml
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   └── resources/
│   │   └── test/
│   └── README.md
│
├── frontend/
│   ├── package.json
│   ├── public/
│   ├── src/
│   │   ├── api/
│   │   ├── assets/
│   │   ├── components/
│   │   ├── layouts/
│   │   ├── router/
│   │   ├── stores/
│   │   ├── views/
│   │   ├── App.vue
│   │   └── main.js
│   └── README.md
│
├── docs/
│   └── DESIGN.md
│
├── README.md
└── .gitignore
```

---

# 9. Cấu trúc backend dự kiến

Backend được chia theo từng chức năng.

```text
com.clothingstore
├── auth/
│   ├── controller/
│   ├── dto/
│   ├── security/
│   └── service/
│
├── user/
│   ├── controller/
│   ├── dto/
│   ├── entity/
│   ├── repository/
│   └── service/
│
├── category/
├── brand/
├── banner/
├── product/
├── favorite/
├── review/
├── cart/
├── order/
├── payment/
├── dashboard/
│
├── common/
│   ├── exception/
│   ├── response/
│   ├── validation/
│   └── util/
│
└── config/
```

Mỗi module có thể chứa:

```text
controller
dto
entity
repository
service
mapper
```

Không đặt toàn bộ entity của dự án trong một module không rõ chức năng.

---

# 10. Cấu trúc frontend dự kiến

```text
src/
├── api/
│   ├── authApi.js
│   ├── userApi.js
│   ├── categoryApi.js
│   ├── brandApi.js
│   ├── bannerApi.js
│   ├── productApi.js
│   ├── favoriteApi.js
│   ├── reviewApi.js
│   ├── cartApi.js
│   ├── orderApi.js
│   └── paymentApi.js
│
├── assets/
├── components/
│   ├── common/
│   ├── admin/
│   └── client/
│
├── layouts/
│   ├── AdminLayout.vue
│   ├── ClientLayout.vue
│   └── AuthLayout.vue
│
├── router/
│   └── index.js
│
├── stores/
│   ├── authStore.js
│   ├── cartStore.js
│   ├── favoriteStore.js
│   └── productFilterStore.js
│
├── views/
│   ├── auth/
│   ├── admin/
│   └── client/
│
├── App.vue
└── main.js
```

---

# 11. Quy ước REST API

API sử dụng tiền tố:

```text
/api/v1
```

Ví dụ:

```text
POST   /api/v1/auth/register
POST   /api/v1/auth/login
POST   /api/v1/auth/refresh
POST   /api/v1/auth/logout
GET    /api/v1/auth/me
```

API quản trị:

```text
/api/v1/admin
```

Ví dụ:

```text
GET    /api/v1/admin/products
POST   /api/v1/admin/products
PUT    /api/v1/admin/products/{id}
DELETE /api/v1/admin/products/{id}
```

API tài khoản cá nhân:

```text
/api/v1/account
```

Ví dụ:

```text
GET /api/v1/account/profile
GET /api/v1/account/favorites
GET /api/v1/account/orders
```

---

# 12. Quy ước response

Response thành công:

```json
{
  "success": true,
  "message": "Đăng nhập thành công",
  "data": {
    "id": 1,
    "email": "client@example.com",
    "roles": [
      "CLIENT"
    ]
  }
}
```

Response thất bại:

```json
{
  "success": false,
  "message": "Email hoặc mật khẩu không chính xác",
  "data": null
}
```

Response validation:

```json
{
  "success": false,
  "message": "Dữ liệu không hợp lệ",
  "errors": {
    "email": "Email không đúng định dạng",
    "password": "Mật khẩu phải có ít nhất 8 ký tự"
  }
}
```

Response phân trang:

```json
{
  "success": true,
  "message": "Lấy danh sách thành công",
  "data": {
    "content": [],
    "page": 0,
    "size": 20,
    "totalElements": 100,
    "totalPages": 5,
    "first": true,
    "last": false
  }
}
```

---

# 13. Nguyên tắc phát triển

- Không viết nhiều chức năng cùng lúc.
- Mỗi phần phải chạy được trước khi chuyển bước.
- Không đặt business logic trong controller.
- Không trả Entity trực tiếp cho frontend.
- Sử dụng DTO cho request và response.
- Sử dụng Service để xử lý nghiệp vụ.
- Mật khẩu phải được mã hóa.
- Không lưu token nhạy cảm dạng nguyên bản.
- Backend luôn kiểm tra quyền.
- Frontend chỉ hỗ trợ điều hướng và giao diện.
- Sử dụng Flyway cho mọi thay đổi database.
- Không sửa migration đã chạy trên database dùng chung.
- Không sử dụng `DOUBLE` hoặc `FLOAT` để lưu tiền.
- Không xóa vật lý đơn hàng và thanh toán.
- Mỗi danh sách lớn phải có phân trang.
- Mọi API nhận dữ liệu phải có validation.
- Mỗi lỗi phải có thông báo rõ ràng.

---

# 14. Cách làm việc trong quá trình học

Mỗi chức năng được thực hiện theo thứ tự:

```text
1. Giải thích chức năng.
2. Thiết kế database.
3. Viết migration.
4. Viết Entity.
5. Viết Repository.
6. Viết DTO.
7. Viết Service.
8. Viết Controller.
9. Kiểm tra API.
10. Viết frontend API.
11. Viết Pinia store nếu cần.
12. Viết giao diện Vue.
13. Kiểm tra toàn bộ luồng.
```

Không viết toàn bộ code trong một lần.

Mỗi class và mỗi hàm cần được giải thích để người học có thể:

- Đọc code.
- Hiểu code.
- Tự paste code.
- Tự chạy thử.
- Tự rà soát lỗi.

---

# 15. Thứ tự triển khai dự án

```text
Phase 1: Khởi tạo
1. Tạo backend Spring Boot.
2. Tạo frontend Vue.js.
3. Tạo MySQL database.
4. Kiểm tra frontend và backend chạy độc lập.

Phase 2: Xác thực
5. Cấu hình Flyway.
6. Tạo users, roles và user_roles.
7. Viết chức năng đăng ký.
8. Viết chức năng đăng nhập.
9. Viết refresh token.
10. Viết đăng xuất.
11. Phân quyền ADMIN và CLIENT.
12. Tạo giao diện đăng ký và đăng nhập.

Phase 3: Giao diện và layout
13. Tạo AdminLayout.
14. Tạo ClientLayout.
15. Tạo router guard.
16. Tạo menu và điều hướng.

Phase 4: Dữ liệu nền
17. Quản lý chuyên mục.
18. Quản lý thương hiệu.
19. Quản lý banner.

Phase 5: Sản phẩm
20. Quản lý sản phẩm.
21. Quản lý hình ảnh.
22. Quản lý màu sắc.
23. Quản lý kích thước.
24. Quản lý biến thể.
25. Quản lý tồn kho.

Phase 6: Hiển thị phía CLIENT
26. Trang chủ.
27. Danh sách sản phẩm.
28. Tìm kiếm.
29. Bộ lọc.
30. Chi tiết sản phẩm.

Phase 7: Tương tác
31. Danh sách yêu thích.
32. Đánh giá sản phẩm.
33. Quản lý profile.

Phase 8: Mua hàng
34. Giỏ hàng.
35. Mua ngay.
36. Thanh toán giỏ hàng.
37. Tạo đơn hàng.
38. Lịch sử đơn hàng.

Phase 9: Quản trị
39. Quản lý người dùng.
40. Quản lý đơn hàng.
41. Quản lý thanh toán.
42. Dashboard tổng quan.

Phase 10: Hoàn thiện
43. Kiểm thử.
44. Tối ưu truy vấn.
45. Bảo mật.
46. Đóng gói và triển khai.
```

---

# 16. Trạng thái dự án

Dự án đang ở giai đoạn thiết kế.

Phần code đầu tiên sẽ là:

```text
1. Khởi tạo Spring Boot.
2. Kết nối MySQL.
3. Cấu hình Flyway.
4. Tạo bảng users.
5. Tạo bảng roles.
6. Tạo bảng user_roles.
```

Sau khi ba bảng này hoạt động đúng, dự án mới bắt đầu viết API đăng ký.