<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

const router = useRouter()
const authStore = useAuthStore()

const paymentMethod = ref('CASH_ON_DELIVERY')

const form = reactive({
  receiverName: authStore.user?.fullName || '',
  phone: authStore.user?.phone || '',
  province: '',
  district: '',
  ward: '',
  addressLine: '',
  note: '',
})

const orderSummary = ref({
  items: [
    { name: 'Áo Khoác Biker Piqué (x1)', total: 1550000 },
    { name: 'Áo Polo Nam Thêu Logo (x2)', total: 980000 },
  ],
  subtotal: 2530000,
  shippingFee: 0,
  total: 2530000,
})

const formatPrice = (val) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)

const handlePlaceOrder = () => {
  alert('Đặt hàng thành công! Đơn hàng của bạn đang được xử lý.')
  router.push('/orders')
}
</script>

<template>
  <div class="checkout-page">
    <!-- Breadcrumb Begin -->
    <section class="breadcrumb-option">
      <div class="container">
        <div class="row">
          <div class="col-lg-12">
            <div class="breadcrumb__text">
              <h4>Thanh toán</h4>
              <div class="breadcrumb__links">
                <RouterLink to="/">Trang chủ</RouterLink>
                <RouterLink to="/cart">Giỏ hàng</RouterLink>
                <span>Thanh toán</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
    <!-- Breadcrumb End -->

    <!-- Checkout Section Begin -->
    <section class="checkout spad">
      <div class="container">
        <div class="checkout__form">
          <form @submit.prevent="handlePlaceOrder">
            <div class="row">
              <!-- Form Địa chỉ nhận hàng -->
              <div class="col-lg-8 col-md-6">
                <h6 class="checkout__title">Thông tin giao hàng</h6>
                <div class="row">
                  <div class="col-lg-6">
                    <div class="checkout__input">
                      <p>Họ và tên người nhận<span>*</span></p>
                      <input v-model="form.receiverName" type="text" required placeholder="Nguyễn Văn A">
                    </div>
                  </div>
                  <div class="col-lg-6">
                    <div class="checkout__input">
                      <p>Số điện thoại<span>*</span></p>
                      <input v-model="form.phone" type="text" required placeholder="0901234567">
                    </div>
                  </div>
                </div>

                <div class="row">
                  <div class="col-lg-4">
                    <div class="checkout__input">
                      <p>Tỉnh / Thành phố<span>*</span></p>
                      <input v-model="form.province" type="text" required placeholder="TP. Hồ Chí Minh">
                    </div>
                  </div>
                  <div class="col-lg-4">
                    <div class="checkout__input">
                      <p>Quận / Huyện<span>*</span></p>
                      <input v-model="form.district" type="text" required placeholder="Quận 1">
                    </div>
                  </div>
                  <div class="col-lg-4">
                    <div class="checkout__input">
                      <p>Phường / Xã<span>*</span></p>
                      <input v-model="form.ward" type="text" required placeholder="Phường Bến Nghé">
                    </div>
                  </div>
                </div>

                <div class="checkout__input">
                  <p>Địa chỉ cụ thể (Số nhà, tên đường)<span>*</span></p>
                  <input v-model="form.addressLine" type="text" required placeholder="123 Đường Nguyễn Huệ">
                </div>

                <div class="checkout__input">
                  <p>Ghi chú cho đơn hàng</p>
                  <input v-model="form.note" type="text" placeholder="Ghi chú về đơn hàng, ví dụ: thời gian hay địa điểm giao hàng chi tiết.">
                </div>
              </div>

              <!-- Tóm tắt đơn hàng & Phương thức thanh toán -->
              <div class="col-lg-4 col-md-6">
                <div class="checkout__order">
                  <h4 class="order__title">Đơn hàng của bạn</h4>
                  <div class="checkout__order__products">Sản phẩm <span>Tổng</span></div>
                  <ul class="checkout__total__products">
                    <li v-for="(item, idx) in orderSummary.items" :key="idx">
                      {{ item.name }} <span>{{ formatPrice(item.total) }}</span>
                    </li>
                  </ul>
                  <ul class="checkout__total__all">
                    <li>Tạm tính <span>{{ formatPrice(orderSummary.subtotal) }}</span></li>
                    <li>Phí vận chuyển <span>Miễn phí</span></li>
                    <li>Tổng thanh toán <span>{{ formatPrice(orderSummary.total) }}</span></li>
                  </ul>

                  <div class="checkout__input__checkbox">
                    <label for="cod">
                      Thanh toán khi nhận hàng (COD)
                      <input type="radio" id="cod" value="CASH_ON_DELIVERY" v-model="paymentMethod">
                      <span class="checkmark"></span>
                    </label>
                  </div>
                  <div class="checkout__input__checkbox">
                    <label for="bank">
                      Chuyển khoản ngân hàng (VNPAY / QR)
                      <input type="radio" id="bank" value="BANK_TRANSFER" v-model="paymentMethod">
                      <span class="checkmark"></span>
                    </label>
                  </div>

                  <button type="submit" class="site-btn btn-block mt-4">XÁC NHẬN ĐẶT HÀNG</button>
                </div>
              </div>
            </div>
          </form>
        </div>
      </div>
    </section>
    <!-- Checkout Section End -->
  </div>
</template>

<style scoped>
.breadcrumb-option {
  background: #f3f2ee;
  padding: 40px 0;
}

.checkout__order {
  background: #f3f2ee;
  padding: 30px;
}

.site-btn {
  font-size: 14px;
  color: #ffffff;
  background: #111111;
  font-weight: 700;
  border: none;
  letter-spacing: 2px;
  text-transform: uppercase;
  padding: 14px 30px;
  display: inline-block;
  width: 100%;
}

.site-btn:hover {
  background: #e53637;
}
</style>
