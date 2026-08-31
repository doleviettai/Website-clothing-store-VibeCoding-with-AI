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

// State Tabs
const activeTab = ref('ATM') // Mặc định mở tab Thẻ ATM / Agribank

// Danh Sách Ngân Hàng Sandbox (Mặc định chọn Agribank)
const banks = ref([
  { code: 'VAB', name: 'Agribank', color: '#a61d24' },
  { code: 'VCB', name: 'Vietcombank', color: '#005a36' },
  { code: 'MBB', name: 'MBBank', color: '#1335ad' },
  { code: 'TCB', name: 'Techcombank', color: '#e30613' },
  { code: 'VPB', name: 'VPBank', color: '#00aa56' },
  { code: 'BIDV', name: 'BIDV', color: '#0066b3' },
  { code: 'ACB', name: 'ACB', color: '#0054a6' },
  { code: 'STB', name: 'Sacombank', color: '#0058a9' }
])
const selectedBank = ref('VAB') // Agribank

// Form Thẻ ATM (Giao diện sạch chuẩn mực, người dùng nhập số thẻ)
const cardForm = ref({
  cardNumber: '',
  cardHolder: '',
  issueDate: ''
})

const showOtpModal = ref(false)
const inputOtp = ref('')
const isProcessing = ref(false)
const isSuccessScreen = ref(false)
const statusMessage = ref('')
const errorMessage = ref('')
const countdown = ref(3)

// Kiểm tra thẻ và xử lý thanh toán
const handleAtmSubmit = () => {
  errorMessage.value = ''
  const cleanNum = cardForm.value.cardNumber.replace(/\s+/g, '')

  if (!cleanNum) {
    alert('Vui lòng nhập số thẻ ATM của bạn.')
    return
  }
  if (!cardForm.value.cardHolder.trim()) {
    alert('Vui lòng nhập tên chủ thẻ.')
    return
  }

  // 1. Kịch bản Thẻ bị khóa (Test number: 9704000000000026)
  if (cleanNum === '9704000000000026') {
    errorMessage.value = '🔒 Giao dịch thất bại: Thẻ của quý khách đã bị KHÓA. Vui lòng liên hệ ngân hàng Agribank.'
    handleConfirmPayment('FAILED', 'LOCKED')
    return
  }

  // 2. Kịch bản Không đủ số dư (Test number: 9704000000000034)
  if (cleanNum === '9704000000000034') {
    errorMessage.value = '💸 Giao dịch thất bại: Tài khoản không đủ số dư để thực hiện thanh toán.'
    handleConfirmPayment('FAILED', 'LOW_BALANCE')
    return
  }

  // 3. Kịch bản Vượt hạn mức (Test number: 9704000000000042)
  if (cleanNum === '9704000000000042') {
    errorMessage.value = '⚠️ Giao dịch thất bại: Số tiền thanh toán vượt quá hạn mức giao dịch trong ngày của thẻ.'
    handleConfirmPayment('FAILED', 'OVER_LIMIT')
    return
  }

  // Mở Popup OTP xác thực
  inputOtp.value = ''
  showOtpModal.value = true
}

const handleConfirmOtp = () => {
  if (inputOtp.value !== '100000' && inputOtp.value !== '123456') {
    alert('Mã OTP không đúng! (Mã OTP thử nghiệm Sandbox: 100000)')
    return
  }
  showOtpModal.value = false
  handleConfirmPayment('SUCCESS', `ATM_${selectedBank.value}`)
}

const handleConfirmPayment = async (status, detailCode = '') => {
  isProcessing.value = true
  statusMessage.value = status === 'SUCCESS' ? 'Đang xác thực thẻ ngân hàng và xử lý thanh toán MoMo...' : 'Đang xử lý giao dịch...'
  try {
    const finalRequestId = detailCode ? `${requestId.value}_${detailCode}` : requestId.value
    await momoApi.confirmMockMoMo(orderId.value, finalRequestId, status)

    if (status === 'SUCCESS') {
      isProcessing.value = false
      isSuccessScreen.value = true

      const timer = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0) {
          clearInterval(timer)
          redirectToStore('SUCCESS')
        }
      }, 1000)
    } else {
      setTimeout(() => {
        isProcessing.value = false
      }, 800)
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
  <div class="momo-gateway-mock min-vh-100 bg-light d-flex align-items-center justify-content-center py-4">
    <div class="card border-0 shadow-lg rounded-lg overflow-hidden" style="max-width: 540px; width: 100%;">
      <!-- Header MoMo Pink Banner -->
      <div class="card-header text-center py-4 text-white" style="background: linear-gradient(135deg, #a50064 0%, #d82d8b 100%);">
        <div class="d-inline-flex align-items-center justify-content-center bg-white rounded-circle p-2 mb-2 shadow-sm" style="width: 65px; height: 65px;">
          <span class="font-weight-bold h4 text-danger m-0" style="color: #a50064 !important;">Mo<span style="color: #d82d8b;">Mo</span></span>
        </div>
        <h5 class="font-weight-bold m-0 text-white">CỔNG THANH TOÁN MOMO GATEWAY</h5>
        <small class="text-white-50">Cổng Thanh Toán Trực Tuyến Ví MoMo Sandbox</small>
      </div>

      <!-- Navigation Tabs -->
      <div class="bg-white border-bottom d-flex text-center font-weight-bold">
        <div
          class="tab-item flex-fill py-3 cursor-pointer border-end"
          :class="{ 'active-tab-momo': activeTab === 'ATM' }"
          @click="activeTab = 'ATM'"
        >
          <i class="fa fa-credit-card mr-1 text-danger" style="color: #a50064 !important;"></i> Thẻ ATM Agribank / Napas
        </div>
        <div
          class="tab-item flex-fill py-3 cursor-pointer"
          :class="{ 'active-tab-momo': activeTab === 'QR' }"
          @click="activeTab = 'QR'"
        >
          <i class="fa fa-qrcode mr-1"></i> Quét Mã Ví MoMo QR
        </div>
      </div>

      <!-- Body -->
      <div class="card-body p-4 bg-white">
        <!-- Error Alert -->
        <div v-if="errorMessage" class="alert alert-danger alert-dismissible fade show shadow-sm mb-3" role="alert">
          <i class="fa fa-exclamation-circle mr-1"></i> {{ errorMessage }}
        </div>

        <!-- Màn hình Đang Xử Lý -->
        <div v-if="isProcessing" class="py-5 text-center">
          <LoadingSpinner :text="statusMessage" />
        </div>

        <!-- Màn hình Thanh Toán Thành Công & Tự Động Quay Về Website -->
        <div v-else-if="isSuccessScreen" class="py-3 text-center">
          <div class="d-inline-flex align-items-center justify-content-center bg-success text-white rounded-circle mb-3 shadow" style="width: 75px; height: 75px;">
            <i class="fa fa-check fa-3x"></i>
          </div>
          <h4 class="font-weight-bold text-success mb-2">THANH TOÁN MOMO THÀNH CÔNG!</h4>
          <p class="text-muted small mb-3">Giao dịch MoMo qua thẻ ATM ngân hàng đã hoàn tất.</p>
          <div class="alert alert-info py-2 small mb-4">
            <i class="fa fa-spinner fa-spin mr-1"></i> Tự động quay về website bán hàng xem đơn hàng sau <strong>{{ countdown }}s</strong>...
          </div>
          <button class="btn text-white font-weight-bold w-100 py-2" style="background-color: #a50064;" @click="redirectToStore('SUCCESS')">
            <i class="fa fa-arrow-left mr-1"></i> Quay Về Web Bán Hàng Ngay
          </button>
        </div>

        <!-- TAB 1: THANH TOÁN QUA THẺ ATM AGRIBANK / NAPAS -->
        <div v-else-if="activeTab === 'ATM'">
          <div class="p-3 rounded mb-3 text-center" style="background: #fff0f6; border: 1px dashed #d82d8b;">
            <small class="text-muted d-block text-uppercase font-weight-bold">Số Tiền Thanh Toán</small>
            <h3 class="font-weight-bold m-0" style="color: #a50064;">{{ formatPrice(amount) }}</h3>
          </div>

          <!-- 1. Chọn Ngân Hàng -->
          <label class="form-label font-weight-bold text-dark mb-2">1. Ngân Hàng Phát Hành Thẻ ATM <span class="text-danger">*</span></label>
          <div class="row g-2 mb-3">
            <div v-for="bank in banks" :key="bank.code" class="col-3">
              <div
                class="bank-card p-2 border rounded text-center cursor-pointer h-100 d-flex flex-column align-items-center justify-content-center"
                :class="{ 'selected-bank-momo': selectedBank === bank.code }"
                @click="selectedBank = bank.code"
              >
                <span class="font-weight-bold small text-truncate" :style="{ color: bank.color }">{{ bank.code }}</span>
                <small class="text-muted" style="font-size: 10px;">{{ bank.name }}</small>
              </div>
            </div>
          </div>

          <!-- 2. Form Nhập Thẻ ATM -->
          <label class="form-label font-weight-bold text-dark mb-2">2. Nhập Thông Tin Thẻ ATM Ngân Hàng <span class="text-danger">*</span></label>
          <div class="bg-light p-3 border rounded mb-3">
            <div class="mb-2">
              <label class="form-label small font-weight-bold text-muted">Số Thẻ ATM <span class="text-danger">*</span></label>
              <input
                type="text"
                v-model="cardForm.cardNumber"
                class="form-control form-control-sm font-weight-bold text-dark"
                placeholder="9704 xxxx xxxx xxxx"
                required
              />
            </div>
            <div class="row">
              <div class="col-7 mb-2">
                <label class="form-label small font-weight-bold text-muted">Tên Chủ Thẻ <span class="text-danger">*</span></label>
                <input
                  type="text"
                  v-model="cardForm.cardHolder"
                  class="form-control form-control-sm text-uppercase font-weight-bold"
                  placeholder="NGUYEN VAN A"
                  required
                />
              </div>
              <div class="col-5 mb-2">
                <label class="form-label small font-weight-bold text-muted">Hạn Thẻ (MM/YY)</label>
                <input
                  type="text"
                  v-model="cardForm.issueDate"
                  class="form-control form-control-sm font-weight-bold"
                  placeholder="03/07"
                />
              </div>
            </div>
          </div>

          <button
            class="btn text-white btn-lg w-100 font-weight-bold text-uppercase py-3 shadow-sm"
            style="background-color: #a50064;"
            @click="handleAtmSubmit"
          >
            <i class="fa fa-credit-card mr-2"></i> THANH TOÁN THẺ ATM {{ selectedBank }}
          </button>
        </div>

        <!-- TAB 2: QUÉT MÃ QR CODE MOMO -->
        <div v-else-if="activeTab === 'QR'" class="text-center">
          <div class="p-3 rounded mb-3" style="background: #fff0f6; border: 1px dashed #d82d8b;">
            <small class="text-muted d-block text-uppercase font-weight-bold mb-1">Số Tiền Thanh Toán</small>
            <h2 class="font-weight-bold m-0" style="color: #a50064;">{{ formatPrice(amount) }}</h2>
          </div>

          <div class="text-start small border rounded p-3 mb-3 bg-light">
            <div class="d-flex justify-content-between mb-1">
              <span class="text-muted">Mã Đơn Hàng:</span>
              <strong class="text-dark">#{{ orderId }}</strong>
            </div>
            <div class="d-flex justify-content-between mb-1">
              <span class="text-muted">Mã RequestId:</span>
              <code class="font-weight-bold" style="color: #a50064;">{{ requestId }}</code>
            </div>
          </div>

          <!-- QR Code Demo -->
          <div class="qr-box text-center mb-3">
            <div class="d-inline-block border p-2 rounded bg-white shadow-sm">
              <img
                src="https://api.qrserver.com/v1/create-qr-code/?size=170x170&data=MoMoSandboxTestDemo"
                alt="QR Code MoMo Sandbox"
                style="width: 150px; height: 150px;"
              />
            </div>
            <small class="text-muted d-block mt-2">Bấm nút bên dưới để xác nhận thanh toán qua Ví MoMo QR</small>
          </div>

          <div class="d-flex flex-column gap-2">
            <button
              class="btn text-white btn-lg font-weight-bold text-uppercase shadow-sm py-3"
              style="background-color: #a50064; border-color: #a50064;"
              @click="handleConfirmPayment('SUCCESS')"
            >
              <i class="fa fa-check-circle mr-2"></i> XÁC NHẬN THANH TOÁN (THÀNH CÔNG)
            </button>
            <button
              class="btn btn-outline-danger btn-sm font-weight-bold text-uppercase py-2"
              @click="handleConfirmPayment('FAILED')"
            >
              <i class="fa fa-times-circle mr-1"></i> Hủy Giao Dịch
            </button>
          </div>
        </div>
      </div>

      <div class="card-footer bg-light text-center py-3 border-top">
        <small class="text-muted">Cổng thanh toán trực tuyến MoMo Gateway.</small>
      </div>
    </div>

    <!-- POPUP MODAL NHẬP MÃ OTP -->
    <div v-if="showOtpModal" class="modal fade show d-block" style="background: rgba(0,0,0,0.6);" tabindex="-1">
      <div class="modal-dialog modal-dialog-centered" style="max-width: 400px;">
        <div class="modal-content border-0 shadow-lg">
          <div class="modal-header text-white" style="background-color: #a50064;">
            <h5 class="modal-title font-weight-bold">
              <i class="fa fa-shield text-warning mr-2"></i> XÁC THỰC MÃ OTP MOMO
            </h5>
            <button type="button" class="btn-close btn-close-white" @click="showOtpModal = false"></button>
          </div>
          <div class="modal-body p-4 text-center">
            <p class="small text-muted mb-3">
              Mã OTP xác thực thanh toán đơn hàng <strong>#{{ orderId }}</strong> đã được gửi tới số điện thoại liên kết thẻ <strong>{{ selectedBank }}</strong>.
            </p>

            <div class="mb-3">
              <input
                type="text"
                v-model="inputOtp"
                class="form-control form-control-lg text-center font-weight-bold letter-spacing-2"
                placeholder="Nhập mã OTP..."
                maxlength="6"
              />
            </div>

            <button
              class="btn text-white font-weight-bold w-100 py-2 text-uppercase"
              style="background-color: #a50064;"
              @click="handleConfirmOtp"
            >
              XÁC NHẬN THANH TOÁN NGAY
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.momo-gateway-mock {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
}
.cursor-pointer {
  cursor: pointer;
}
.tab-item {
  color: #6c757d;
  transition: all 0.2s ease;
}
.active-tab-momo {
  color: #a50064 !important;
  border-bottom: 3px solid #a50064;
  background-color: #fff0f6;
}
.bank-card {
  transition: all 0.2s ease;
  background: #fff;
}
.bank-card:hover {
  border-color: #a50064 !important;
}
.selected-bank-momo {
  border: 2px solid #a50064 !important;
  background-color: #fff0f6 !important;
  box-shadow: 0 2px 5px rgba(165, 0, 100, 0.2);
}
.letter-spacing-2 {
  letter-spacing: 4px;
}
</style>
