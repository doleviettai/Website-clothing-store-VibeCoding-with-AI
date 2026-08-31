<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import * as zalopayApi from '@/api/zalopayApi'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'

const route = useRoute()
const router = useRouter()

const orderId = ref(route.query.order_id || '')
const appTransId = ref(route.query.app_trans_id || '')
const amount = ref(route.query.amount || '0')

// State Tabs
const activeTab = ref('ATM') // Mặc định mở tab Thẻ ATM / Napas ZaloPay

// Danh Sách Ngân Hàng Sandbox ZaloPay
const banks = ref([
  { code: 'CTG', name: 'VietinBank', color: '#004a98' },
  { code: 'VCB', name: 'Vietcombank', color: '#005a36' },
  { code: 'MBB', name: 'MBBank', color: '#1335ad' },
  { code: 'TCB', name: 'Techcombank', color: '#e30613' },
  { code: 'VPB', name: 'VPBank', color: '#00aa56' },
  { code: 'BIDV', name: 'BIDV', color: '#0066b3' },
  { code: 'ACB', name: 'ACB', color: '#0054a6' },
  { code: 'STB', name: 'Sacombank', color: '#0058a9' }
])
const selectedBank = ref('CTG') // VietinBank / ZaloPay Napas

// Form Thẻ ATM (Người dùng nhập số thẻ)
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

// Kiểm tra thẻ và xử lý thanh toán ZaloPay Sandbox
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

  // 1. Kịch bản Thẻ bị mất / đánh cắp
  const stolenCards = ['9704540000000013', '9704540000000021', '9704541000000029', '9704541000000052', '9704541000000060', '9704541000000086']
  if (stolenCards.includes(cleanNum)) {
    errorMessage.value = '❌ Giao dịch thất bại: Thẻ đã bị khai báo MẤT hoặc BỊ ĐÁNH CẮP. Vui lòng liên hệ ngân hàng phát hành.'
    handleConfirmPayment('FAILED', 'STOLEN_CARD')
    return
  }

  // 2. Kịch bản Timeout / Quá thời gian
  const timeoutCards = ['9704540000000039', '9704541000000037', '9704540000000054']
  if (timeoutCards.includes(cleanNum)) {
    errorMessage.value = '⏱️ Giao dịch thất bại: Quá thời gian xử lý phản hồi từ ngân hàng (Timeout).'
    handleConfirmPayment('FAILED', 'TIMEOUT')
    return
  }

  // 3. Kịch bản Hết tiền / Không đủ số dư
  const lowBalanceCards = ['9704540000000047', '9704541000000011', '9704541000000045']
  if (lowBalanceCards.includes(cleanNum)) {
    errorMessage.value = '💸 Giao dịch thất bại: Tài khoản không đủ số dư để thực hiện thanh toán.'
    handleConfirmPayment('FAILED', 'LOW_BALANCE')
    return
  }

  // Thẻ hợp lệ (VD: 9704540000000062) -> Mở Popup OTP xác thực
  inputOtp.value = ''
  showOtpModal.value = true
}

const handleConfirmOtp = () => {
  if (inputOtp.value !== '111111' && inputOtp.value !== '123456' && inputOtp.value !== '100000') {
    alert('Mã OTP không đúng! (Mã OTP thử nghiệm ZaloPay Sandbox: 111111)')
    return
  }
  showOtpModal.value = false
  handleConfirmPayment('SUCCESS', `ATM_${selectedBank.value}`)
}

const handleConfirmPayment = async (status, detailCode = '') => {
  isProcessing.value = true
  statusMessage.value = status === 'SUCCESS' ? 'Đang xác thực thẻ ngân hàng và xử lý thanh toán ZaloPay...' : 'Đang xử lý giao dịch...'
  try {
    const finalTransId = detailCode ? `${appTransId.value}_${detailCode}` : appTransId.value
    await zalopayApi.confirmMockZaloPay(orderId.value, finalTransId, status)

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
    alert(err.response?.data?.message || 'Có lỗi xảy ra khi xử lý giao dịch ZaloPay.')
    isProcessing.value = false
  }
}

const redirectToStore = (status) => {
  router.push({
    path: '/payment-result',
    query: {
      orderId: orderId.value,
      appTransId: appTransId.value,
      gateway: 'ZALOPAY',
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
  <div class="zalopay-gateway-mock min-vh-100 bg-light d-flex align-items-center justify-content-center py-4">
    <div class="card border-0 shadow-lg rounded-lg overflow-hidden" style="max-width: 540px; width: 100%;">
      <!-- Header ZaloPay Banner -->
      <div class="card-header text-center py-4 text-white" style="background: linear-gradient(135deg, #0068ff 0%, #0046b8 100%);">
        <div class="d-inline-flex align-items-center justify-content-center bg-white rounded-circle p-2 mb-2 shadow-sm" style="width: 65px; height: 65px;">
          <span class="font-weight-bold h4 text-primary m-0">Zalo<span class="text-success">Pay</span></span>
        </div>
        <h5 class="font-weight-bold m-0 text-white">CỔNG THANH TOÁN ZALOPAY GATEWAY</h5>
        <small class="text-white-50">Cổng Thanh Toán Trực Tuyến ZaloPay Sandbox</small>
      </div>

      <!-- Navigation Tabs -->
      <div class="bg-white border-bottom d-flex text-center font-weight-bold">
        <div
          class="tab-item flex-fill py-3 cursor-pointer border-end"
          :class="{ 'active-tab': activeTab === 'ATM' }"
          @click="activeTab = 'ATM'"
        >
          <i class="fa fa-credit-card mr-1 text-success"></i> Thẻ ATM Ngân Hàng Nội Địa
        </div>
        <div
          class="tab-item flex-fill py-3 cursor-pointer"
          :class="{ 'active-tab': activeTab === 'QR' }"
          @click="activeTab = 'QR'"
        >
          <i class="fa fa-qrcode mr-1 text-primary"></i> Quét Mã ZaloPay QR
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
          <h4 class="font-weight-bold text-success mb-2">THANH TOÁN THÀNH CÔNG!</h4>
          <p class="text-muted small mb-3">Giao dịch ZaloPay qua thẻ ATM ngân hàng đã hoàn tất.</p>
          <div class="alert alert-info py-2 small mb-4">
            <i class="fa fa-spinner fa-spin mr-1"></i> Tự động quay về website bán hàng xem đơn hàng sau <strong>{{ countdown }}s</strong>...
          </div>
          <button class="btn btn-primary font-weight-bold w-100 py-2" @click="redirectToStore('SUCCESS')">
            <i class="fa fa-arrow-left mr-1"></i> Quay Về Web Bán Hàng Ngay
          </button>
        </div>

        <!-- TAB 1: THANH TOÁN QUA THẺ ATM NGÂN HÀNG NỘI ĐỊA -->
        <div v-else-if="activeTab === 'ATM'">
          <div class="p-3 rounded mb-3 text-center" style="background: #f0f7ff; border: 1px dashed #0068ff;">
            <small class="text-muted d-block text-uppercase font-weight-bold">Số Tiền Thanh Toán</small>
            <h3 class="font-weight-bold text-primary m-0">{{ formatPrice(amount) }}</h3>
          </div>

          <!-- 1. Chọn Ngân Hàng -->
          <label class="form-label font-weight-bold text-dark mb-2">1. Ngân Hàng Phát Hành Thẻ ATM <span class="text-danger">*</span></label>
          <div class="row g-2 mb-3">
            <div v-for="bank in banks" :key="bank.code" class="col-3">
              <div
                class="bank-card p-2 border rounded text-center cursor-pointer h-100 d-flex flex-column align-items-center justify-content-center"
                :class="{ 'selected-bank': selectedBank === bank.code }"
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
                  placeholder="10/18"
                />
              </div>
            </div>
          </div>

          <button
            class="btn btn-success btn-lg w-100 font-weight-bold text-uppercase py-3 shadow-sm"
            @click="handleAtmSubmit"
          >
            <i class="fa fa-credit-card mr-2"></i> THANH TOÁN THẺ ATM {{ selectedBank }}
          </button>
        </div>

        <!-- TAB 2: QUÉT MÃ QR CODE ZALOPAY -->
        <div v-else-if="activeTab === 'QR'" class="text-center">
          <div class="p-3 rounded mb-3" style="background: #f0f7ff; border: 1px dashed #0068ff;">
            <small class="text-muted d-block text-uppercase font-weight-bold mb-1">Số Tiền Thanh Toán</small>
            <h2 class="font-weight-bold text-primary m-0">{{ formatPrice(amount) }}</h2>
          </div>

          <div class="text-start small border rounded p-3 mb-3 bg-light">
            <div class="d-flex justify-content-between mb-1">
              <span class="text-muted">Mã Đơn Hàng:</span>
              <strong class="text-dark">#{{ orderId }}</strong>
            </div>
            <div class="d-flex justify-content-between mb-1">
              <span class="text-muted">Mã TransID:</span>
              <code class="text-primary font-weight-bold">{{ appTransId }}</code>
            </div>
          </div>

          <!-- QR Code Demo -->
          <div class="qr-box text-center mb-3">
            <div class="d-inline-block border p-2 rounded bg-white shadow-sm">
              <img
                src="https://api.qrserver.com/v1/create-qr-code/?size=170x170&data=ZaloPaySandboxDemo"
                alt="QR Code ZaloPay Sandbox"
                style="width: 150px; height: 150px;"
              />
            </div>
            <small class="text-muted d-block mt-2">Bấm nút bên dưới để xác nhận thanh toán qua Ví ZaloPay QR</small>
          </div>

          <div class="d-flex flex-column gap-2">
            <button
              class="btn btn-success btn-lg font-weight-bold text-uppercase shadow-sm py-3"
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
        <small class="text-muted">Cổng thanh toán trực tuyến ZaloPay Gateway Sandbox.</small>
      </div>
    </div>

    <!-- POPUP MODAL NHẬP MÃ OTP -->
    <div v-if="showOtpModal" class="modal fade show d-block" style="background: rgba(0,0,0,0.6);" tabindex="-1">
      <div class="modal-dialog modal-dialog-centered" style="max-width: 400px;">
        <div class="modal-content border-0 shadow-lg">
          <div class="modal-header bg-dark text-white">
            <h5 class="modal-title font-weight-bold">
              <i class="fa fa-shield text-warning mr-2"></i> XÁC THỰC MÃ OTP ZALOPAY
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

            <button class="btn btn-success font-weight-bold w-100 py-2 text-uppercase" @click="handleConfirmOtp">
              XÁC NHẬN THANH TOÁN NGAY
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.zalopay-gateway-mock {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
}
.cursor-pointer {
  cursor: pointer;
}
.tab-item {
  color: #6c757d;
  transition: all 0.2s ease;
}
.active-tab {
  color: #0068ff !important;
  border-bottom: 3px solid #0068ff;
  background-color: #f0f7ff;
}
.bank-card {
  transition: all 0.2s ease;
  background: #fff;
}
.bank-card:hover {
  border-color: #0068ff !important;
}
.selected-bank {
  border: 2px solid #0068ff !important;
  background-color: #e6f0ff !important;
  box-shadow: 0 2px 5px rgba(0, 104, 255, 0.2);
}
.letter-spacing-2 {
  letter-spacing: 4px;
}
</style>
