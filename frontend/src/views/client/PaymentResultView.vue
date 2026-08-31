<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import * as zalopayApi from '@/api/zalopayApi'
import * as momoApi from '@/api/momoApi'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'

const route = useRoute()
const router = useRouter()

const orderId = ref(route.query.orderId || '')
const gatewayParam = ref(route.query.gateway || '')

const paymentInfo = ref(null)
const isLoading = ref(true)
const errorMessage = ref('')
const countdown = ref(4)
let timerId = null

const verifyPaymentResult = async () => {
  if (!orderId.value) {
    errorMessage.value = 'Không tìm thấy thông tin mã đơn hàng để đối soát.'
    isLoading.value = false
    return
  }

  isLoading.value = true
  errorMessage.value = ''
  try {
    let res
    if (gatewayParam.value === 'MOMO') {
      res = await momoApi.getMoMoStatus(orderId.value)
    } else {
      res = await zalopayApi.getZaloPayStatus(orderId.value)
    }
    paymentInfo.value = res.data?.data

    if (paymentInfo.value && paymentInfo.value.paymentStatus === 'PAID') {
      // Đếm ngược 4s tự động chuyển đến trang Lịch Sử Đơn Hàng (/orders)
      timerId = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0) {
          clearInterval(timerId)
          goToOrderHistory()
        }
      }, 1000)
    }
  } catch (err) {
    errorMessage.value = err.response?.data?.message || 'Không thể kiểm tra kết quả thanh toán.'
  } finally {
    isLoading.value = false
  }
}

const goToOrderHistory = () => {
  if (timerId) clearInterval(timerId)
  router.push('/orders')
}

const formatPrice = (val) => {
  if (!val) return '0 đ'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)
}

onMounted(() => {
  verifyPaymentResult()
})

onUnmounted(() => {
  if (timerId) clearInterval(timerId)
})
</script>

<template>
  <div class="payment-result-page py-5">
    <div class="container">
      <div class="row justify-content-center">
        <div class="col-lg-6">
          <div class="card border-0 shadow-lg rounded-lg overflow-hidden bg-white text-center p-4">
            <div v-if="isLoading" class="py-5">
              <LoadingSpinner text="Đang đối soát kết quả giao dịch từ ngân hàng..." />
            </div>

            <div v-else-if="errorMessage" class="py-4">
              <i class="fa fa-exclamation-triangle fa-3x text-danger mb-3 d-block"></i>
              <h4 class="font-weight-bold text-dark mb-2">Lỗi Kiểm Tra Thanh Toán</h4>
              <p class="text-muted small mb-4">{{ errorMessage }}</p>
              <RouterLink to="/orders" class="btn btn-dark font-weight-bold px-4 py-2">Xem Lịch Sử Đơn Hàng</RouterLink>
            </div>

            <!-- Trường Hợp 1: Thanh Toán Thành Công (PAID) -->
            <div v-else-if="paymentInfo && paymentInfo.paymentStatus === 'PAID'" class="py-3">
              <div class="d-inline-flex align-items-center justify-content-center bg-success text-white rounded-circle mb-3 shadow" style="width: 80px; height: 80px;">
                <i class="fa fa-check fa-3x"></i>
              </div>

              <h3 class="font-weight-bold text-success mb-2">THANH TOÁN THÀNH CÔNG!</h3>
              <p class="text-muted small mb-3">Đơn hàng của bạn đã được xác nhận thanh toán thành công và gửi tới hệ thống Admin xử lý.</p>

              <div class="alert alert-success py-2 small mb-4 font-weight-bold">
                <i class="fa fa-refresh fa-spin mr-1"></i> Tự động chuyển đến trang <u>Lịch Sử Đơn Hàng</u> sau <strong>{{ countdown }}s</strong>...
              </div>

              <!-- Bảng Chi Tiết Giao Dịch -->
              <div class="bg-light p-3 border rounded text-start small mb-4">
                <div class="d-flex justify-content-between mb-2">
                  <span class="text-muted">Mã Đơn Hàng:</span>
                  <strong class="text-danger">#{{ paymentInfo.orderCode }}</strong>
                </div>
                <div class="d-flex justify-content-between mb-2">
                  <span class="text-muted">Khách Hàng:</span>
                  <strong class="text-dark">{{ paymentInfo.customerName }}</strong>
                </div>
                <div class="d-flex justify-content-between mb-2">
                  <span class="text-muted">Phương Thức Thanh Toán:</span>
                  <span
                    class="badge font-weight-bold"
                    :class="paymentInfo.paymentGateway === 'MOMO' ? 'text-white' : 'bg-primary text-white'"
                    :style="paymentInfo.paymentGateway === 'MOMO' ? 'background-color: #a50064;' : ''"
                  >
                    VÍ {{ paymentInfo.paymentGateway || 'ONLINE' }}
                  </span>
                </div>
                <div class="d-flex justify-content-between mb-2">
                  <span class="text-muted">Mã Giao Dịch Cổng (TransID):</span>
                  <code class="text-dark font-weight-bold">{{ paymentInfo.gatewayTransactionNo || paymentInfo.appTransId || paymentInfo.requestId || 'MOCK-SUCCESS' }}</code>
                </div>
                <div class="d-flex justify-content-between pt-2 border-top">
                  <span class="font-weight-bold text-dark">Tổng Tiền Đã Thanh Toán:</span>
                  <strong class="font-weight-bold text-danger fs-5">{{ formatPrice(paymentInfo.totalAmount) }}</strong>
                </div>
              </div>

              <div class="d-flex justify-content-center gap-2">
                <button class="btn btn-dark font-weight-bold px-4 py-2" @click="goToOrderHistory">
                  <i class="fa fa-list mr-1"></i> Xem Đơn Hàng Của Tôi Ngay
                </button>
                <RouterLink to="/products" class="btn btn-outline-danger font-weight-bold px-4 py-2">
                  Tiếp Tục Mua Sắm
                </RouterLink>
              </div>
            </div>

            <!-- Trường Hợp 2: Thanh Toán Thất Bại / Bị Hủy -->
            <div v-else class="py-3">
              <div class="d-inline-flex align-items-center justify-content-center bg-danger text-white rounded-circle mb-3 shadow" style="width: 80px; height: 80px;">
                <i class="fa fa-times fa-3x"></i>
              </div>

              <h3 class="font-weight-bold text-danger mb-2">THANH TOÁN THẤT BẠI</h3>
              <p class="text-muted small mb-4">Giao dịch đã bị hủy hoặc không thể hoàn tất qua cổng thanh toán.</p>

              <div v-if="paymentInfo" class="bg-light p-3 border rounded text-start small mb-4">
                <div class="d-flex justify-content-between mb-2">
                  <span class="text-muted">Mã Đơn Hàng:</span>
                  <strong class="text-dark">#{{ paymentInfo.orderCode }}</strong>
                </div>
                <div class="d-flex justify-content-between">
                  <span class="text-muted">Trạng Thái Đơn:</span>
                  <span class="badge bg-warning text-dark font-weight-bold">Chưa thanh toán ({{ paymentInfo.paymentStatus }})</span>
                </div>
              </div>

              <div class="d-flex justify-content-center gap-2">
                <RouterLink to="/cart" class="btn btn-secondary font-weight-bold px-4 py-2">Về Giỏ Hàng</RouterLink>
                <RouterLink to="/orders" class="btn btn-danger font-weight-bold px-4 py-2">Xem Lịch Sử Đơn Hàng</RouterLink>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
