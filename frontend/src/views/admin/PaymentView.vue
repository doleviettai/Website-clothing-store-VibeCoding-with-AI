<script setup>
import { ref, onMounted, watch } from 'vue'
import * as paymentApi from '@/api/paymentApi'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'

const payments = ref([])
const totalPages = ref(0)
const totalElements = ref(0)
const currentPage = ref(0)
const pageSize = ref(10)

const keyword = ref('')
const selectedGateway = ref('')
const selectedStatus = ref('')
const isLoading = ref(false)
const errorMessage = ref('')

// Modal Chi Tiết Giao Dịch
const selectedPayment = ref(null)
const isModalOpen = ref(false)

const gatewayBadges = {
  VNPAY: 'bg-primary text-white',
  MOMO: 'bg-danger text-white',
  ZALOPAY: 'bg-info text-white',
  BANK_TRANSFER: 'bg-dark text-white',
  COD: 'bg-secondary text-white',
}

const statusBadges = {
  SUCCESS: { label: 'Thành công', class: 'bg-success text-white' },
  PENDING: { label: 'Đang xử lý', class: 'bg-warning text-dark' },
  FAILED: { label: 'Thất bại', class: 'bg-danger text-white' },
  REFUNDED: { label: 'Đã hoàn tiền', class: 'bg-secondary text-white' },
}

const fetchPayments = async () => {
  isLoading.value = true
  errorMessage.value = ''
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: keyword.value.trim() || undefined,
      paymentGateway: selectedGateway.value || undefined,
      status: selectedStatus.value || undefined,
    }
    const res = await paymentApi.getAdminPayments(params)
    const pageData = res.data?.data
    payments.value = pageData?.content || []
    totalPages.value = pageData?.totalPages || 0
    totalElements.value = pageData?.totalElements || 0
  } catch (err) {
    errorMessage.value = err.response?.data?.message || 'Không thể tải nhật ký giao dịch.'
  } finally {
    isLoading.value = false
  }
}

const openDetailModal = (payment) => {
  selectedPayment.value = payment
  isModalOpen.value = true
}

const closeModal = () => {
  isModalOpen.value = false
  selectedPayment.value = null
}

const changePage = (p) => {
  if (p >= 0 && p < totalPages.value) {
    currentPage.value = p
    fetchPayments()
  }
}

const formatPrice = (val) => {
  if (!val) return '0 đ'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)
}

watch([selectedGateway, selectedStatus], () => {
  currentPage.value = 0
  fetchPayments()
})

onMounted(() => {
  fetchPayments()
})
</script>

<template>
  <div class="admin-page py-3">
    <!-- Title Header -->
    <div class="d-flex align-items-center justify-content-between mb-4">
      <div>
        <h3 class="page-title font-weight-bold text-dark m-0">
          <i class="fa fa-credit-card text-success mr-2"></i> Quản Lý Giao Dịch (Payment Management)
        </h3>
        <p class="text-muted small m-0">Theo dõi nhật ký giao dịch thanh toán trực tuyến và đối soát mã giao dịch cổng thanh toán</p>
      </div>
      <div class="badge bg-success text-white px-3 py-2 fs-6 shadow-sm">
        Tổng số: <strong>{{ totalElements }}</strong> giao dịch
      </div>
    </div>

    <!-- Alert Notification -->
    <div v-if="errorMessage" class="alert alert-danger alert-dismissible fade show shadow-sm" role="alert">
      <i class="fa fa-exclamation-triangle mr-2"></i> {{ errorMessage }}
    </div>

    <!-- Search & Filter Controls Card -->
    <div class="card border-0 shadow-sm rounded-lg mb-4 bg-white p-3">
      <div class="row g-3 align-items-center">
        <!-- Search Keyword -->
        <div class="col-md-4">
          <div class="input-group">
            <span class="input-group-text bg-light border-end-0"><i class="fa fa-search text-muted"></i></span>
            <input
              type="text"
              v-model="keyword"
              class="form-control border-start-0"
              placeholder="Mã giao dịch, Mã đơn hàng, Tên khách..."
              @keyup.enter="fetchPayments"
            />
          </div>
        </div>

        <!-- Filter Cổng Thanh Toán -->
        <div class="col-md-3">
          <select v-model="selectedGateway" class="form-select">
            <option value="">-- Tất cả cổng thanh toán --</option>
            <option value="VNPAY">VNPAY</option>
            <option value="MOMO">MOMO</option>
            <option value="ZALOPAY">ZALOPAY</option>
            <option value="BANK_TRANSFER">Chuyển Khoản Ngân Hàng</option>
            <option value="COD">Thanh toán khi nhận hàng (COD)</option>
          </select>
        </div>

        <!-- Filter Trạng Thái Giao Dịch -->
        <div class="col-md-3">
          <select v-model="selectedStatus" class="form-select">
            <option value="">-- Tất cả trạng thái --</option>
            <option value="SUCCESS">Thành công (SUCCESS)</option>
            <option value="PENDING">Đang xử lý (PENDING)</option>
            <option value="FAILED">Thất bại (FAILED)</option>
            <option value="REFUNDED">Đã hoàn tiền (REFUNDED)</option>
          </select>
        </div>

        <!-- Button Lọc -->
        <div class="col-md-2">
          <button class="btn btn-dark w-100 font-weight-bold" @click="fetchPayments">
            <i class="fa fa-filter mr-1"></i> Lọc Giao Dịch
          </button>
        </div>
      </div>
    </div>

    <!-- Table Main Content -->
    <div class="card border-0 shadow-sm rounded-lg overflow-hidden bg-white">
      <div class="card-body p-0">
        <div v-if="isLoading" class="py-5 text-center">
          <LoadingSpinner text="Đang tải lịch sử giao dịch..." />
        </div>

        <div v-else-if="payments.length === 0" class="py-5 text-center text-muted">
          <i class="fa fa-credit-card fa-3x mb-3 d-block"></i>
          Không tìm thấy giao dịch nào phù hợp.
        </div>

        <div v-else class="table-responsive">
          <table class="table table-hover align-middle m-0 text-center">
            <thead class="table-dark text-uppercase small">
              <tr>
                <th>Mã Giao Dịch</th>
                <th>Mã Đơn Hàng</th>
                <th>Khách Hàng</th>
                <th>Cổng Thanh Toán</th>
                <th>Phương Thức</th>
                <th>Mã Giao Dịch Cổng</th>
                <th>Số Tiền</th>
                <th>Trạng Thái</th>
                <th>Ngày Giao Dịch</th>
                <th>Chi Tiết</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="p in payments" :key="p.id">
                <!-- Mã Giao Dịch Internal -->
                <td>
                  <code class="font-weight-bold text-dark">{{ p.transactionCode }}</code>
                </td>

                <!-- Mã Đơn Hàng Liên Kết -->
                <td>
                  <RouterLink to="/admin/orders" class="font-weight-bold text-danger text-decoration-none">
                    {{ p.orderCode || 'N/A' }}
                  </RouterLink>
                </td>

                <!-- Khách Hàng -->
                <td>
                  <span class="font-weight-bold text-dark">{{ p.customerName || 'Khách hàng' }}</span>
                </td>

                <!-- Cổng Thanh Toán -->
                <td>
                  <span class="badge px-3 py-2 font-weight-bold" :class="gatewayBadges[p.paymentGateway] || 'bg-secondary'">
                    {{ p.paymentGateway }}
                  </span>
                </td>

                <!-- Phương Thức -->
                <td>
                  <span class="badge bg-light text-dark border">{{ p.paymentMethod }}</span>
                </td>

                <!-- Mã Giao Dịch Cổng -->
                <td>
                  <code class="text-primary font-weight-bold">{{ p.gatewayTransactionNo || '-' }}</code>
                </td>

                <!-- Số Tiền -->
                <td>
                  <span class="font-weight-bold text-success h6 m-0">{{ formatPrice(p.amount) }}</span>
                </td>

                <!-- Trạng Thái -->
                <td>
                  <span class="badge px-3 py-2" :class="statusBadges[p.status]?.class || 'bg-secondary'">
                    {{ statusBadges[p.status]?.label || p.status }}
                  </span>
                </td>

                <!-- Ngày Giao Dịch -->
                <td>
                  <small class="text-dark font-weight-bold d-block">{{ new Date(p.createdAt).toLocaleDateString('vi-VN') }}</small>
                  <small class="text-muted">{{ new Date(p.createdAt).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }) }}</small>
                </td>

                <!-- Chi Tiết -->
                <td>
                  <button class="btn btn-sm btn-outline-dark" @click="openDetailModal(p)" title="Chi tiết giao dịch">
                    <i class="fa fa-eye"></i> Xem
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Pagination Bar -->
        <div v-if="totalPages > 1" class="d-flex align-items-center justify-content-between p-3 border-top bg-light">
          <span class="text-muted small">
            Trang {{ currentPage + 1 }} / {{ totalPages }} (Tổng {{ totalElements }} giao dịch)
          </span>
          <ul class="pagination pagination-sm m-0">
            <li class="page-item" :class="{ disabled: currentPage === 0 }">
              <button class="page-link" @click="changePage(currentPage - 1)">Trước</button>
            </li>
            <li v-for="p in totalPages" :key="p" class="page-item" :class="{ active: currentPage === (p - 1) }">
              <button class="page-link" @click="changePage(p - 1)">{{ p }}</button>
            </li>
            <li class="page-item" :class="{ disabled: currentPage === totalPages - 1 }">
              <button class="page-link" @click="changePage(currentPage + 1)">Sau</button>
            </li>
          </ul>
        </div>
      </div>
    </div>

    <!-- MODAL POPUP CHI TIẾT GIAO DỊCH -->
    <div v-if="isModalOpen && selectedPayment" class="modal fade show d-block" style="background: rgba(0,0,0,0.6);" tabindex="-1">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 shadow-lg">
          <div class="modal-header bg-dark text-white">
            <h5 class="modal-title font-weight-bold">
              <i class="fa fa-receipt mr-2 text-success"></i> GIAO DỊCH #{{ selectedPayment.transactionCode }}
            </h5>
            <button type="button" class="btn-close btn-close-white" @click="closeModal"></button>
          </div>
          <div class="modal-body p-4 bg-light">
            <div class="bg-white p-3 border rounded mb-3">
              <p class="m-0 small"><strong>Mã Đơn Hàng Liên Kết:</strong> <span class="text-danger font-weight-bold">{{ selectedPayment.orderCode }}</span></p>
              <p class="m-0 small"><strong>Tên Khách Hàng:</strong> {{ selectedPayment.customerName }}</p>
              <p class="m-0 small"><strong>Số Tiền Giao Dịch:</strong> <span class="text-success font-weight-bold fs-5">{{ formatPrice(selectedPayment.amount) }}</span></p>
            </div>

            <div class="bg-white p-3 border rounded mb-3">
              <p class="m-0 small"><strong>Cổng Thanh Toán:</strong> {{ selectedPayment.paymentGateway }}</p>
              <p class="m-0 small"><strong>Phương Thức:</strong> {{ selectedPayment.paymentMethod }}</p>
              <p class="m-0 small"><strong>Mã Giao Dịch Cổng (Gateway No):</strong> <code>{{ selectedPayment.gatewayTransactionNo || 'Chưa cập nhật' }}</code></p>
              <p class="m-0 small"><strong>Trạng Thái:</strong>
                <span class="badge ml-2" :class="statusBadges[selectedPayment.status]?.class || 'bg-secondary'">
                  {{ statusBadges[selectedPayment.status]?.label || selectedPayment.status }}
                </span>
              </p>
            </div>

            <div v-if="selectedPayment.paymentInfo" class="bg-white p-3 border rounded">
              <p class="m-0 small text-muted"><strong>Mô Tả Giao Dịch:</strong></p>
              <p class="m-0 small text-dark">{{ selectedPayment.paymentInfo }}</p>
            </div>
          </div>
          <div class="modal-footer bg-white">
            <button type="button" class="btn btn-secondary font-weight-bold" @click="closeModal">Đóng Window</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-title {
  font-size: 1.5rem;
}
</style>
