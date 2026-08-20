<script setup>
/**
 * Component ClientFooter chuẩn giao diện MaleFashion
 */
</script>

<template>
  <footer class="footer">
    <div class="container">
      <div class="row">
        <div class="col-lg-3 col-md-6 col-sm-6">
          <div class="footer__about">
            <div class="footer__logo">
              <RouterLink to="/"><img src="/img/footer-logo.png" alt="Male Fashion Logo"></RouterLink>
            </div>
            <p>Khách hàng là trung tâm trong mô hình kinh doanh độc đáo của chúng tôi, bao gồm cả thiết kế thời trang hiện đại.</p>
            <a href="#"><img src="/img/payment.png" alt="Phương thức thanh toán"></a>
          </div>
        </div>
        <div class="col-lg-2 offset-lg-1 col-md-3 col-sm-6">
          <div class="footer__widget">
            <h6>Cửa hàng</h6>
            <ul>
              <li><RouterLink to="/products">Thời trang Nam</RouterLink></li>
              <li><RouterLink to="/products">Giày Nổi Bật</RouterLink></li>
              <li><RouterLink to="/products">Phụ Kiện</RouterLink></li>
              <li><RouterLink to="/products">Giảm Giá</RouterLink></li>
            </ul>
          </div>
        </div>
        <div class="col-lg-2 col-md-3 col-sm-6">
          <div class="footer__widget">
            <h6>Hỗ trợ</h6>
            <ul>
              <li><a href="#">Liên hệ chúng tôi</a></li>
              <li><a href="#">Phương thức thanh toán</a></li>
              <li><a href="#">Giao hàng</a></li>
              <li><a href="#">Đổi trả & Hoàn tiền</a></li>
            </ul>
          </div>
        </div>
        <div class="col-lg-3 offset-lg-1 col-md-6 col-sm-6">
          <div class="footer__widget">
            <h6>Nhận tin mới</h6>
            <div class="footer__newslatter">
              <p>Hãy là người đầu tiên nhận thông tin về bộ sưu tập mới, khuyến mãi & ưu đãi!</p>
              <form @submit.prevent>
                <input type="text" placeholder="Nhập email của bạn">
                <button type="submit"><span class="icon_mail_alt"></span></button>
              </form>
            </div>
          </div>
        </div>
      </div>
      <div class="row">
        <div class="col-lg-12 text-center">
          <div class="footer__copyright__text">
            <p>
              Copyright © {{ new Date().getFullYear() }} Clothing Store — Male Fashion. All rights reserved.
            </p>
          </div>
        </div>
      </div>
    </div>
  </footer>
</template>

<style scoped>
.footer {
  background: #111111;
  padding-top: 70px;
  padding-bottom: 0;
}
</style>
