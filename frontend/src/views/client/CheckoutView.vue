<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import * as cartApi from '@/api/cartApi'
import * as orderApi from '@/api/orderApi'
import * as zalopayApi from '@/api/zalopayApi'
import * as momoApi from '@/api/momoApi'
import * as vnpayApi from '@/api/vnpayApi'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'

const router = useRouter()
const authStore = useAuthStore()

const cart = ref(null)
const isLoadingCart = ref(true)
const isSubmitting = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

// Form Thông Tin Nhận Hàng
const form = ref({
  customerName: authStore.user?.fullName || '',
  phone: authStore.user?.phone || '',
  email: authStore.user?.email || '',
  province: 'Hà Nội',
  district: 'Cầu Giấy',
  ward: 'Dịch Vọng',
  streetAddress: '',
  note: '',
  paymentMethod: 'COD',
})

// Tải thông tin Giỏ hàng
const fetchCart = async () => {
  isLoadingCart.value = true
  errorMessage.value = ''
  try {
    const res = await cartApi.getCart()
    cart.value = res.data?.data
    if (!cart.value || !cart.value.items || cart.value.items.length === 0) {
      errorMessage.value = 'Giỏ hàng của bạn đang rỗng. Vui lòng thêm sản phẩm trước khi thanh toán.'
    }
  } catch (err) {
    errorMessage.value = err.response?.data?.message || 'Không thể nạp thông tin giỏ hàng.'
  } finally {
    isLoadingCart.value = false
  }
}

const items = computed(() => cart.value?.items || [])
const subtotal = computed(() => cart.value?.subtotal || 0)
const totalAmount = computed(() => cart.value?.subtotal || 0)

// Xử lý Lên Đơn Hàng Thanh Toán
const handlePlaceOrder = async () => {
  if (items.value.length === 0) {
    errorMessage.value = 'Giỏ hàng của bạn rỗng, không thể lên đơn hàng!'
    return
  }

  if (!form.value.customerName.trim()) {
    errorMessage.value = 'Vui lòng nhập Họ và tên người nhận.'
    return
  }
  if (!form.value.phone.trim()) {
    errorMessage.value = 'Vui lòng nhập Số điện thoại nhận hàng.'
    return
  }
  if (!form.value.province.trim()) {
    errorMessage.value = 'Vui lòng chọn / nhập Tỉnh / Thành phố.'
    return
  }
  if (!form.value.ward.trim()) {
    errorMessage.value = 'Vui lòng nhập Phường / Xã.'
    return
  }
  if (!form.value.streetAddress.trim()) {
    errorMessage.value = 'Vui lòng nhập Số nhà / Thôn / Tên đường chi tiết.'
    return
  }

  isSubmitting.value = true
  errorMessage.value = ''

  try {
    // 1. Tạo Đơn Hàng trong Database
    const res = await orderApi.createOrder({
      customerName: form.value.customerName.trim(),
      phone: form.value.phone.trim(),
      email: form.value.email.trim(),
      province: form.value.province.trim(),
      district: form.value.district.trim(),
      ward: form.value.ward.trim(),
      streetAddress: form.value.streetAddress.trim(),
      note: form.value.note.trim(),
      paymentMethod: form.value.paymentMethod,
    })

    const createdOrder = res.data?.data

    // 2. Phân nhánh Thanh Toán VNPAY vs MoMo vs ZaloPay vs COD
    if (form.value.paymentMethod === 'VNPAY') {
      const vnpayRes = await vnpayApi.createVNPayPayment(createdOrder.id)
      const vnpayData = vnpayRes.data?.data

      if (vnpayData && vnpayData.paymentUrl) {
        successMessage.value = 'Đang chuyển sang Cổng Thanh Toán VNPAY Sandbox...'
        setTimeout(() => {
          window.location.href = vnpayData.paymentUrl
        }, 800)
      } else {
        errorMessage.value = 'Không thể khởi tạo cổng VNPAY. Vui lòng thử lại.'
        isSubmitting.value = false
      }
    } else if (form.value.paymentMethod === 'MOMO') {
      const momoRes = await momoApi.createMoMoPayment(createdOrder.id)
      const momoData = momoRes.data?.data

      if (momoData && momoData.payUrl) {
        successMessage.value = 'Đang chuyển sang Cổng Thanh Toán Ví MoMo Sandbox...'
        setTimeout(() => {
          window.location.href = momoData.payUrl
        }, 800)
      } else {
        errorMessage.value = 'Không thể khởi tạo cổng MoMo. Vui lòng thử lại.'
        isSubmitting.value = false
      }
    } else if (form.value.paymentMethod === 'ZALOPAY') {
      const zaloRes = await zalopayApi.createZaloPayPayment(createdOrder.id)
      const zaloData = zaloRes.data?.data

      if (zaloData && zaloData.orderUrl) {
        successMessage.value = 'Đang chuyển sang Cổng Thanh Toán Ví ZaloPay Sandbox...'
        setTimeout(() => {
          window.location.href = zaloData.orderUrl
        }, 800)
      } else {
        errorMessage.value = 'Không thể khởi tạo cổng ZaloPay. Vui lòng thử lại.'
        isSubmitting.value = false
      }
    } else {
      successMessage.value = `🎉 Đặt hàng thành công! Mã đơn: #${createdOrder?.orderCode}`
      setTimeout(() => {
        router.push('/orders')
      }, 1500)
    }
  } catch (err) {
    errorMessage.value = err.response?.data?.message || 'Có lỗi xảy ra khi tạo đơn hàng. Vui lòng thử lại.'
    isSubmitting.value = false
  }
}

const formatPrice = (val) => {
  if (!val) return '0 đ'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)
}

onMounted(() => {
  fetchCart()
})
</script>

<template>
  <div class="checkout-page py-4">
    <!-- Breadcrumb -->
    <div class="bg-light py-3 mb-4 border-bottom">
      <div class="container">
        <div class="d-flex align-items-center justify-content-between">
          <h4 class="font-weight-bold text-dark m-0">
            <i class="fa fa-check-square-o text-danger mr-2"></i> THANH TOÁN ĐƠN HÀNG (CHECKOUT)
          </h4>
          <nav aria-label="breadcrumb">
            <ol class="breadcrumb m-0 bg-transparent p-0">
              <li class="breadcrumb-item"><RouterLink to="/" class="text-muted">Trang chủ</RouterLink></li>
              <li class="breadcrumb-item"><RouterLink to="/cart" class="text-muted">Giỏ hàng</RouterLink></li>
              <li class="breadcrumb-item active text-dark font-weight-bold" aria-current="page">Checkout</li>
            </ol>
          </nav>
        </div>
      </div>
    </div>

    <!-- Container Chính -->
    <div class="container">
      <div v-if="successMessage" class="alert alert-success shadow-sm p-4 text-center mb-4">
        <i class="fa fa-check-circle fa-2x mb-2 d-block"></i>
        <h5 class="font-weight-bold m-0">{{ successMessage }}</h5>
        <small class="text-muted">Đang chuyển tới trang Lịch sử đơn hàng của bạn...</small>
      </div>

      <div v-if="errorMessage" class="alert alert-danger shadow-sm p-3 mb-4">
        <i class="fa fa-exclamation-triangle mr-2"></i> {{ errorMessage }}
      </div>

      <div v-if="isLoadingCart" class="py-5 text-center">
        <LoadingSpinner text="Đang nạp thông tin đơn hàng..." />
      </div>

      <div v-else-if="items.length === 0" class="py-5 text-center my-4 bg-white border rounded shadow-sm">
        <i class="fa fa-shopping-cart fa-4x text-muted mb-3 d-block"></i>
        <h5 class="font-weight-bold text-dark mb-2">Giỏ hàng của bạn đang rỗng</h5>
        <p class="text-muted small mb-4">Hãy chọn những mẫu quần áo ưng ý trước khi thực hiện thanh toán nhé!</p>
        <RouterLink to="/products" class="btn btn-danger font-weight-bold px-5 py-2">Khám Phá Cửa Hàng Ngay</RouterLink>
      </div>

      <div v-else class="row">
        <!-- Cột Bên Trái: Form Thông Tin Giao Nhận -->
        <div class="col-lg-7 mb-4">
          <div class="card border-0 shadow-sm rounded-lg bg-white p-4">
            <h5 class="font-weight-bold text-dark mb-4 border-bottom pb-2">
              <i class="fa fa-address-card text-danger mr-2"></i> THÔNG TIN NGƯỜI NHẬN & ĐỊA CHỈ GIAO HÀNG
            </h5>

            <form @submit.prevent="handlePlaceOrder">
              <div class="row">
                <!-- Họ và tên người nhận -->
                <div class="col-md-12 mb-3">
                  <label class="form-label font-weight-bold text-dark">Họ và tên người nhận <span class="text-danger">*</span></label>
                  <input
                    type="text"
                    v-model="form.customerName"
                    class="form-control"
                    placeholder="Ví dụ: Nguyễn Văn A"
                    required
                  />
                </div>

                <!-- Số điện thoại -->
                <div class="col-md-6 mb-3">
                  <label class="form-label font-weight-bold text-dark">Số điện thoại <span class="text-danger">*</span></label>
                  <input
                    type="tel"
                    v-model="form.phone"
                    class="form-control"
                    placeholder="Ví dụ: 0912345678"
                    required
                  />
                </div>

                <!-- Email -->
                <div class="col-md-6 mb-3">
                  <label class="form-label font-weight-bold text-dark">Địa chỉ Email</label>
                  <input
                    type="email"
                    v-model="form.email"
                    class="form-control"
                    placeholder="Ví dụ: email@gmail.com"
                  />
                </div>

                <!-- Tỉnh / Thành phố -->
                <div class="col-md-4 mb-3">
                  <label class="form-label font-weight-bold text-dark">Tỉnh / Thành phố <span class="text-danger">*</span></label>
                  <input
                    type="text"
                    v-model="form.province"
                    class="form-control"
                    placeholder="Ví dụ: Hà Nội / TP.HCM"
                    required
                  />
                </div>

                <!-- Quận / Huyện -->
                <div class="col-md-4 mb-3">
                  <label class="form-label font-weight-bold text-dark">Quận / Huyện</label>
                  <input
                    type="text"
                    v-model="form.district"
                    class="form-control"
                    placeholder="Ví dụ: Cầu Giấy / Quận 1"
                  />
                </div>

                <!-- Phường / Xã -->
                <div class="col-md-4 mb-3">
                  <label class="form-label font-weight-bold text-dark">Phường / Xã <span class="text-danger">*</span></label>
                  <input
                    type="text"
                    v-model="form.ward"
                    class="form-control"
                    placeholder="Ví dụ: Dịch Vọng / Bến Nghé"
                    required
                  />
                </div>

                <!-- Số nhà / Thôn / Tên đường -->
                <div class="col-md-12 mb-3">
                  <label class="form-label font-weight-bold text-dark">Số nhà / Thôn / Tên đường chi tiết <span class="text-danger">*</span></label>
                  <input
                    type="text"
                    v-model="form.streetAddress"
                    class="form-control"
                    placeholder="Ví dụ: Số 123 đường Xuân Thủy..."
                    required
                  />
                </div>

                <!-- Ghi chú đơn hàng -->
                <div class="col-md-12 mb-3">
                  <label class="form-label font-weight-bold text-dark">Ghi chú đơn hàng (Không bắt buộc)</label>
                  <textarea
                    v-model="form.note"
                    class="form-control"
                    rows="3"
                    placeholder="Ghi chú về đơn hàng, thời gian giao nhận mong muốn..."
                  ></textarea>
                </div>
              </div>
            </form>
          </div>
        </div>

        <!-- Cột Bên Phải: Tóm Tắt Đơn Hàng & Chọn Phương Thức Thanh Toán -->
        <div class="col-lg-5 mb-4">
          <div class="card border-0 shadow-sm rounded-lg bg-white p-4">
            <h5 class="font-weight-bold text-dark mb-4 border-bottom pb-2">
              <i class="fa fa-shopping-bag text-danger mr-2"></i> TÓM TẮT ĐƠN HÀNG (YOUR ORDER)
            </h5>

            <!-- Danh sách sản phẩm -->
            <div class="order-items-list border-bottom mb-3 pb-2" style="max-height: 280px; overflow-y: auto;">
              <div v-for="item in items" :key="item.id" class="d-flex align-items-center justify-content-between mb-3 pr-2">
                <div class="d-flex align-items-center gap-2">
                  <img
                    :src="item.thumbnailUrl || '/img/product/product-1.jpg'"
                    class="rounded border"
                    style="width: 48px; height: 48px; object-fit: cover;"
                  />
                  <div>
                    <h6 class="font-weight-bold text-dark m-0 small">{{ item.productName }}</h6>
                    <small class="text-muted">
                      Size: {{ item.size || 'N/A' }} | Color: {{ item.color || 'N/A' }} | SL: {{ item.quantity }}
                    </small>
                  </div>
                </div>
                <span class="font-weight-bold text-danger small">{{ formatPrice(item.itemTotal) }}</span>
              </div>
            </div>

            <!-- Tổng Tiền Subtotal -->
            <div class="d-flex justify-content-between align-items-center mb-2">
              <span class="text-muted">Tạm tính (Subtotal):</span>
              <span class="font-weight-bold text-dark">{{ formatPrice(subtotal) }}</span>
            </div>

            <div class="d-flex justify-content-between align-items-center mb-3">
              <span class="text-muted">Phí vận chuyển:</span>
              <span class="font-weight-bold text-success">Miễn phí giao hàng</span>
            </div>

            <div class="d-flex justify-content-between align-items-center mb-4 pt-3 border-top">
              <span class="font-weight-bold text-dark h6 m-0">TỔNG THÀNH TIỀN:</span>
              <span class="font-weight-bold text-danger h4 m-0">{{ formatPrice(totalAmount) }}</span>
            </div>

            <!-- PHƯƠNG THỨC THANH TOÁN -->
            <h6 class="font-weight-bold text-dark mb-3 border-top pt-3">Chọn Phương Thức Thanh Toán:</h6>

            <!-- Option 1: Thanh Toán Khi Nhận Hàng (COD) -->
            <div class="form-check p-3 border rounded bg-light mb-2 cursor-pointer">
              <input
                class="form-check-input ms-0 mt-1 mr-2"
                type="radio"
                name="paymentMethod"
                id="pmCod"
                value="COD"
                v-model="form.paymentMethod"
              />
              <label class="form-check-label font-weight-bold text-dark cursor-pointer" for="pmCod">
                <i class="fa fa-truck text-danger mr-1"></i> Thanh toán khi nhận hàng (COD)
              </label>
              <small class="d-block text-muted mt-1">Bạn sẽ thanh toán tiền mặt trực tiếp cho nhân viên giao hàng khi nhận sản phẩm.</small>
            </div>

            <!-- Option 2: Online Payment ZaloPay -->
            <div class="form-check p-3 border rounded bg-light mb-2 cursor-pointer">
              <input
                class="form-check-input ms-0 mt-1 mr-2"
                type="radio"
                name="paymentMethod"
                id="pmZalopay"
                value="ZALOPAY"
                v-model="form.paymentMethod"
              />
              <label class="form-check-label font-weight-bold text-dark cursor-pointer" for="pmZalopay">
                <i class="fa fa-credit-card text-primary mr-1"></i> Thanh toán Ví Điện Tử ZaloPay (Sandbox)
              </label>
              <small class="d-block text-muted mt-1">Thanh toán qua ZaloPay QR / Ví ZaloPay thử nghiệm.</small>
            </div>

            <!-- Option 3: Online Payment MoMo -->
            <div class="form-check p-3 border rounded bg-light mb-2 cursor-pointer">
              <input
                class="form-check-input ms-0 mt-1 mr-2"
                type="radio"
                name="paymentMethod"
                id="pmMomo"
                value="MOMO"
                v-model="form.paymentMethod"
              />
              <label class="form-check-label font-weight-bold text-dark cursor-pointer" for="pmMomo">
                <i class="fa fa-mobile text-danger mr-1" style="color: #a50064 !important;"></i> Thanh toán Ví Điện Tử MoMo (Sandbox)
              </label>
              <small class="d-block text-muted mt-1">Thanh toán qua MoMo QR / Ví MoMo thử nghiệm.</small>
            </div>

            <!-- Option 4: Online Payment VNPAY -->
            <div class="form-check p-3 border rounded bg-light mb-4 cursor-pointer">
              <input
                class="form-check-input ms-0 mt-1 mr-2"
                type="radio"
                name="paymentMethod"
                id="pmVnpay"
                value="VNPAY"
                v-model="form.paymentMethod"
              />
              <label class="form-check-label font-weight-bold text-dark cursor-pointer" for="pmVnpay">
                <i class="fa fa-credit-card mr-1" style="color: #005baa !important;"></i> Thanh toán Cổng VNPAY (Sandbox)
              </label>
              <small class="d-block text-muted mt-1">Thanh toán qua VNPAY-QR / Thẻ ATM Ngân Hàng NCB thử nghiệm.</small>
            </div>

            <!-- Nút Bấm Đặt Hàng -->
            <button
              class="btn btn-danger btn-lg w-100 font-weight-bold text-uppercase py-3 shadow"
              :disabled="isSubmitting || items.length === 0"
              @click="handlePlaceOrder"
            >
              <i class="fa fa-paper-plane mr-2"></i> {{ isSubmitting ? 'ĐANG TẠO ĐƠN HÀNG...' : 'XÁC NHẬN ĐẶT HÀNG (PLACE ORDER)' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.cursor-pointer {
  cursor: pointer;
}
</style>
