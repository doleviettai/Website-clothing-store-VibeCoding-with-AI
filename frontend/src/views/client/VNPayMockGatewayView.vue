<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import * as vnpayApi from '@/api/vnpayApi'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'

const route = useRoute()
const router = useRouter()

const orderId = ref(route.query.order_id || '')
const vnpTxnRef = ref(route.query.vnp_TxnRef || '')
const amount = ref(route.query.amount || '0')
const bankCodeParam = ref(route.query.bank_code || '')

// State Tabs
const activeTab = ref('ATM') // Mặc định mở tab Thẻ ATM / Napas VNPAY

// Danh Sách Ngân Hàng Sandbox VNPAY (Mặc định chọn NCB)
const banks = ref([
  { code: 'NCB', name: 'Ngan hang NCB', color: '#005baa' },
  { code: 'VCB', name: 'Vietcombank', color: '#005a36' },
  { code: 'MBB', name: 'MBBank', color: '#1335ad' },
  { code: 'TCB', name: 'Techcombank', color: '#e30613' },
  { code: 'VPB', name: 'VPBank', color: '#00aa56' },
  { code: 'BIDV', name: 'BIDV', color: '#0066b3' },
  { code: 'ACB', name: 'ACB', color: '#0054a6' },
  { code: 'VAB', name: 'Agribank', color: '#a61d24' }
])
const selectedBank = ref(bankCodeParam.value || 'NCB')

// Form Thẻ ATM VNPAY Sandbox (NCB)
const cardForm = ref({
  cardNumber: '9704 1985 2619 1432',
  cardHolder: 'NGUYEN VAN A',
  issueDate: '07/15'
})

const showOtpModal = ref(false)
const inputOtp = ref('')
const isProcessing = ref(false)
const isSuccessScreen = ref(false)
const statusMessage = ref('')
const errorMessage = ref('')
const countdown = ref(3)

// Kiểm tra thẻ và xử lý thanh toán VNPAY Sandbox
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

  // 1. Kịch bản Thẻ bị khóa
  if (cleanNum === '9704198526191433') {
    errorMessage.value = '🔒 Giao dịch thất bại: Thẻ của quý khách đã bị KHÓA. Vui lòng liên hệ ngân hàng NCB.'
    handleConfirmPayment('FAILED', 'LOCKED')
    return
  }

  // 2. Kịch bản Không đủ số dư
  if (cleanNum === '9704198526191434') {
    errorMessage.value = '💸 Giao dịch thất bại: Tài khoản không đủ số dư để thực hiện thanh toán.'
    handleConfirmPayment('FAILED', 'LOW_BALANCE')
    return
  }

  // 3. Kịch bản Vượt hạn mức
  if (cleanNum === '9704198526191435') {
    errorMessage.value = '⚠️ Giao dịch thất bại: Số tiền thanh toán vượt quá hạn mức giao dịch trong ngày của thẻ.'
    handleConfirmPayment('FAILED', 'OVER_LIMIT')
    return
  }

  // Thẻ hợp lệ (VD: 9704198526191432) -> Mở Popup OTP xác thực
  inputOtp.value = '123456'
  showOtpModal.value = true
}

const handleConfirmOtp = () => {
  if (inputOtp.value !== '123456' && inputOtp.value !== '100000' && inputOtp.value !== '111111') {
    alert('Mã OTP không đúng! (Mã OTP thử nghiệm VNPAY Sandbox NCB: 123456)')
    return
  }
  showOtpModal.value = false
  handleConfirmPayment('SUCCESS', `ATM_${selectedBank.value}`)
}

const handleConfirmPayment = async (status, detailCode = '') => {
  isProcessing.value = true
  statusMessage.value = status === 'SUCCESS' ? 'Đang xác thực thẻ ngân hàng và xử lý thanh toán VNPAY...' : 'Đang xử lý giao dịch...'
  try {
    const finalTxnRef = detailCode ? `${vnpTxnRef.value}_${detailCode}` : vnpTxnRef.value
    await vnpayApi.confirmMockVNPay(orderId.value, finalTxnRef, status)

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
    alert(err.response?.data?.message || 'Có lỗi xảy ra khi xử lý giao dịch VNPAY.')
    isProcessing.value = false
  }
}

const redirectToStore = (status) => {
  router.push({
    path: '/payment-result',
    query: {
      orderId: orderId.value,
      vnpTxnRef: vnpTxnRef.value,
      gateway: 'VNPAY',
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
  <div class="vnpay-gateway-mock min-vh-100 bg-light d-flex align-items-center justify-content-center py-4">
    <div class="card border-0 shadow-lg rounded-lg overflow-hidden" style="max-width: 540px; width: 100%;">
      <!-- Header VNPAY Banner -->
      <div class="card-header text-center py-4 text-white" style="background: linear-gradient(135deg, #005baa 0%, #003b73 100%);">
        <div class="d-inline-flex align-items-center justify-content-center bg-white rounded-circle p-2 mb-2 shadow-sm" style="width: 65px; height: 65px;">
          <span class="font-weight-bold h4 m-0" style="color: #005baa;">VN<span style="color: #ed1c24;">PAY</span></span>
        </div>
        <h5 class="font-weight-bold m-0 text-white">CỔNG THANH TOÁN VNPAY GATEWAY</h5>
        <small class="text-white-50">Cổng Thanh Toán Trực Tuyến VNPAY Sandbox</small>
      </div>

      <!-- Navigation Tabs -->
      <div class="bg-white border-bottom d-flex text-center font-weight-bold">
        <div
          class="tab-item flex-fill py-3 cursor-pointer border-end"
          :class="{ 'active-tab-vnpay': activeTab === 'ATM' }"
          @click="activeTab = 'ATM'"
        >
          <i class="fa fa-credit-card mr-1" style="color: #005baa;"></i> Thẻ ATM / Ngân Hàng Nội Địa
        </div>
        <div
          class="tab-item flex-fill py-3 cursor-pointer"
          :class="{ 'active-tab-vnpay': activeTab === 'QR' }"
          @click="activeTab = 'QR'"
        >
          <i class="fa fa-qrcode mr-1 text-danger"></i> Quét Mã VNPAY-QR
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
          <h4 class="font-weight-bold text-success mb-2">THANH TOÁN VNPAY THÀNH CÔNG!</h4>
          <p class="text-muted small mb-3">Giao dịch VNPAY qua thẻ ATM ngân hàng đã hoàn tất.</p>
          <div class="alert alert-info py-2 small mb-4">
            <i class="fa fa-spinner fa-spin mr-1"></i> Tự động quay về website bán hàng xem đơn hàng sau <strong>{{ countdown }}s</strong>...
          </div>
          <button class="btn font-weight-bold text-white w-100 py-2" style="background-color: #005baa;" @click="redirectToStore('SUCCESS')">
            <i class="fa fa-arrow-left mr-1"></i> Quay Về Web Bán Hàng Ngay
          </button>
        </div>

        <!-- TAB 1: THANH TOÁN QUA THẺ ATM NGÂN HÀNG NỘI ĐỊA -->
        <div v-else-if="activeTab === 'ATM'">
          <div class="p-3 rounded mb-3 text-center" style="background: #eef6ff; border: 1px dashed #005baa;">
            <small class="text-muted d-block text-uppercase font-weight-bold">Số Tiền Thanh Toán</small>
            <h3 class="font-weight-bold m-0" style="color: #005baa;">{{ formatPrice(amount) }}</h3>
          </div>

          <!-- 1. Chọn Ngân Hàng -->
          <label class="form-label font-weight-bold text-dark mb-2">1. Ngân Hàng Phát Hành Thẻ ATM <span class="text-danger">*</span></label>
          <div class="row g-2 mb-3">
            <div v-for="bank in banks" :key="bank.code" class="col-3">
              <div
                class="bank-card p-2 border rounded text-center cursor-pointer h-100 d-flex flex-column align-items-center justify-content-center"
                :class="{ 'selected-bank-vnpay': selectedBank === bank.code }"
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
                placeholder="9704 1985 2619 1432"
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
                  placeholder="07/15"
                />
              </div>
            </div>
          </div>

          <button
            class="btn text-white btn-lg w-100 font-weight-bold text-uppercase py-3 shadow-sm"
            style="background-color: #005baa;"
            @click="handleAtmSubmit"
          >
            <i class="fa fa-credit-card mr-2"></i> THANH TOÁN THẺ ATM {{ selectedBank }}
          </button>
        </div>

        <!-- TAB 2: QUÉT MÃ VNPAY-QR -->
        <div v-else-if="activeTab === 'QR'" class="text-center">
          <div class="p-3 rounded mb-3" style="background: #eef6ff; border: 1px dashed #005baa;">
            <small class="text-muted d-block text-uppercase font-weight-bold mb-1">Số Tiền Thanh Toán</small>
            <h2 class="font-weight-bold m-0" style="color: #005baa;">{{ formatPrice(amount) }}</h2>
          </div>

          <div class="text-start small border rounded p-3 mb-3 bg-light">
            <div class="d-flex justify-content-between mb-1">
              <span class="text-muted">Mã Đơn Hàng:</span>
              <strong class="text-dark">#{{ orderId }}</strong>
            </div>
            <div class="d-flex justify-content-between mb-1">
              <span class="text-muted">Mã vnp_TxnRef:</span>
              <code class="font-weight-bold" style="color: #005baa;">{{ vnpTxnRef }}</code>
            </div>
          </div>

          <!-- QR Code Demo -->
          <div class="qr-box text-center mb-3">
            <div class="d-inline-block border p-2 rounded bg-white shadow-sm">
              <img
                src="https://api.qrserver.com/v1/create-qr-code/?size=170x170&data=VNPaySandboxTestDemo"
                alt="QR Code VNPAY Sandbox"
                style="width: 150px; height: 150px;"
              />
            </div>
            <small class="text-muted d-block mt-2">Bấm nút bên dưới để xác nhận thanh toán qua VNPAY-QR</small>
          </div>

          <div class="d-flex flex-column gap-2">
            <button
              class="btn text-white btn-lg font-weight-bold text-uppercase shadow-sm py-3"
              style="background-color: #005baa;"
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
        <small class="text-muted">Cổng thanh toán trực tuyến VNPAY Gateway Sandbox.</small>
      </div>
    </div>

    <!-- POPUP MODAL NHẬP MÃ OTP -->
    <div v-if="showOtpModal" class="modal fade show d-block" style="background: rgba(0,0,0,0.6);" tabindex="-1">
      <div class="modal-dialog modal-dialog-centered" style="max-width: 400px;">
        <div class="modal-content border-0 shadow-lg">
          <div class="modal-header text-white" style="background-color: #005baa;">
            <h5 class="modal-title font-weight-bold">
              <i class="fa fa-shield text-warning mr-2"></i> XÁC THỰC MÃ OTP VNPAY
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
                placeholder="123456"
                maxlength="6"
              />
            </div>

            <button
              class="btn text-white font-weight-bold w-100 py-2 text-uppercase"
              style="background-color: #005baa;"
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
.vnpay-gateway-mock {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
}
.cursor-pointer {
  cursor: pointer;
}
.tab-item {
  color: #6c757d;
  transition: all 0.2s ease;
}
.active-tab-vnpay {
  color: #005baa !important;
  border-bottom: 3px solid #005baa;
  background-color: #eef6ff;
}
.bank-card {
  transition: all 0.2s ease;
  background: #fff;
}
.bank-card:hover {
  border-color: #005baa !important;
}
.selected-bank-vnpay {
  border: 2px solid #005baa !important;
  background-color: #eef6ff !important;
  box-shadow: 0 2px 5px rgba(0, 91, 170, 0.2);
}
.letter-spacing-2 {
  letter-spacing: 4px;
}
</style>
