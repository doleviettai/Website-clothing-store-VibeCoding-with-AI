<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import * as momoApi from '@/api/momoApi'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'

const route = useRoute()
const router = useRouter()

const orderId = ref(route.query.order_id || '')
const requestId = ref(route.query.request_id || '')
const amount = ref(route.query.amount || '0')
const isProcessing = ref(false)
const isSuccessScreen = ref(false)
const statusMessage = ref('')
const countdown = ref(3)

const handleConfirmPayment = async (status) => {
  isProcessing.value = true
  statusMessage.value = status === 'SUCCESS' ? 'Đang xử lý thanh toán qua ví MoMo...' : 'Đang hủy giao dịch MoMo...'
  try {
    await momoApi.confirmMockMoMo(orderId.value, requestId.value, status)
    if (status === 'SUCCESS') {
      isProcessing.value = false
      isSuccessScreen.value = true

      // Đếm ngược 3s tự động quay lại Web Bán Hàng xem đơn hàng
      const timer = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0) {
          clearInterval(timer)
          redirectToStore('SUCCESS')
        }
      }, 1000)
    } else {
      redirectToStore('FAILED')
    }
  } catch (err) {
    alert(err.response?.data?.message || 'Có lỗi xảy ra khi xử lý giao dịch MoMo.')
    isProcessing.value = false
  }
}

const redirectToStore = (status) => {
  router.push({
    path: '/payment-result',
    query: {
      orderId: orderId.value,
      requestId: requestId.value,
      gateway: 'MOMO',
      status: status
    }
  })
}

const formatPrice = (val) => {
  if (!val) return '0 đ'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)
}
</script>

<template>
  <div class="momo-gateway-mock min-vh-100 bg-light d-flex align-items-center justify-content-center py-5">
    <div class="card border-0 shadow-lg rounded-lg overflow-hidden" style="max-width: 480px; width: 100%;">
      <!-- Header MoMo Pink Banner -->
      <div class="card-header text-center py-4" style="background: linear-gradient(135deg, #a50064 0%, #d82d8b 100%);">
        <div class="d-inline-flex align-items-center justify-content-center bg-white rounded-circle p-2 mb-2 shadow-sm" style="width: 70px; height: 70px;">
          <span class="font-weight-bold h4 text-danger m-0" style="color: #a50064 !important;">Mo<span style="color: #d82d8b;">Mo</span></span>
        </div>
        <h5 class="text-white font-weight-bold m-0">VÍ ĐIỆN TỬ MOMO GATEWAY</h5>
        <small class="text-white-50">Cổng Thanh Toán Trực Tuyến Ví MoMo Sandbox</small>
      </div>

      <!-- Body -->
      <div class="card-body p-4 bg-white text-center">
        <!-- Màn hình Đang Xử Lý -->
        <div v-if="isProcessing" class="py-4">
          <LoadingSpinner :text="statusMessage" />
        </div>

        <!-- Màn hình Thanh Toán Thành Công & Tự Động Quay Về Website -->
        <div v-else-if="isSuccessScreen" class="py-3">
          <div class="d-inline-flex align-items-center justify-content-center bg-success text-white rounded-circle mb-3 shadow" style="width: 75px; height: 75px;">
            <i class="fa fa-check fa-3x"></i>
          </div>
          <h4 class="font-weight-bold text-success mb-2">THANH TOÁN MOMO THÀNH CÔNG!</h4>
          <p class="text-muted small mb-3">Giao dịch qua Ví Điện Tử MoMo đã hoàn tất.</p>
          <div class="alert alert-info py-2 small mb-4">
            <i class="fa fa-spinner fa-spin mr-1"></i> Tự động quay về website bán hàng xem đơn hàng sau <strong>{{ countdown }}s</strong>...
          </div>
          <button class="btn text-white font-weight-bold w-100 py-2" style="background-color: #a50064;" @click="redirectToStore('SUCCESS')">
            <i class="fa fa-arrow-left mr-1"></i> Quay Về Web Bán Hàng Ngay
          </button>
        </div>

        <!-- Màn hình Quét Mã / Chọn Thanh Toán -->
        <div v-else>
          <!-- Thông Tin Số Tiền -->
          <div class="p-3 rounded mb-4" style="background: #fff0f6; border: 1px dashed #d82d8b;">
            <small class="text-muted d-block text-uppercase font-weight-bold mb-1">Số Tiền Thanh Toán</small>
            <h2 class="font-weight-bold m-0" style="color: #a50064;">{{ formatPrice(amount) }}</h2>
          </div>

          <!-- Mã Giao Dịch & Đơn Hàng -->
          <div class="text-start small border rounded p-3 mb-4 bg-light">
            <div class="d-flex justify-content-between mb-2">
              <span class="text-muted">Mã Đơn Hàng:</span>
              <strong class="text-dark">#{{ orderId }}</strong>
            </div>
            <div class="d-flex justify-content-between mb-2">
              <span class="text-muted">Mã MoMo RequestId:</span>
              <code class="font-weight-bold" style="color: #a50064;">{{ requestId }}</code>
            </div>
            <div class="d-flex justify-content-between">
              <span class="text-muted">Trạng Thái:</span>
              <strong class="text-success"><i class="fa fa-lock mr-1"></i> Chờ Xác Nhận Thanh Toán</strong>
            </div>
          </div>

          <!-- QR Code Demo -->
          <div class="qr-box text-center mb-4">
            <div class="d-inline-block border p-2 rounded bg-white shadow-sm">
              <img
                src="https://api.qrserver.com/v1/create-qr-code/?size=180x180&data=MoMoSandboxTestDemo"
                alt="QR Code MoMo Sandbox"
                style="width: 160px; height: 160px;"
              />
            </div>
            <small class="text-muted d-block mt-2">Bấm nút bên dưới để xác nhận thanh toán qua Ví MoMo</small>
          </div>

          <!-- Nút Thao Tác Giả Lập -->
          <div class="d-flex flex-column gap-2">
            <button
              class="btn text-white btn-lg font-weight-bold text-uppercase shadow-sm py-3"
              style="background-color: #a50064; border-color: #a50064;"
              @click="handleConfirmPayment('SUCCESS')"
            >
              <i class="fa fa-check-circle mr-2"></i> XÁC NHẬN THANH TOÁN MOMO (THÀNH CÔNG)
            </button>
            <button
              class="btn btn-outline-danger btn-lg font-weight-bold text-uppercase py-2"
              @click="handleConfirmPayment('FAILED')"
            >
              <i class="fa fa-times-circle mr-2"></i> HỦY GIAO DỊCH (THẤT BẠI)
            </button>
          </div>
        </div>
      </div>

      <div class="card-footer bg-light text-center py-3 border-top">
        <small class="text-muted">Trang thanh toán ví điện tử MoMo Sandbox thử nghiệm.</small>
      </div>
    </div>
  </div>
</template>

<style scoped>
.momo-gateway-mock {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
}
</style>
