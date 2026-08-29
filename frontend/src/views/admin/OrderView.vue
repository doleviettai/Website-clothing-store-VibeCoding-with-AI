<script setup>
import { ref, onMounted, watch } from 'vue'
import * as orderApi from '@/api/orderApi'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'

const orders = ref([])
const totalPages = ref(0)
const totalElements = ref(0)
const currentPage = ref(0)
const pageSize = ref(10)

const keyword = ref('')
const selectedStatus = ref('')
const selectedPaymentStatus = ref('')
const isLoading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

// Modal Chi Tiết Đơn Hàng
const selectedOrder = ref(null)
const isModalOpen = ref(false)

const statusBadges = {
  PENDING: { label: 'Chờ xác nhận', class: 'bg-warning text-dark' },
  CONFIRMED: { label: 'Đã xác nhận', class: 'bg-info text-white' },
  SHIPPING: { label: 'Đang giao hàng', class: 'bg-primary text-white' },
  DELIVERED: { label: 'Đã giao (Hoàn thành)', class: 'bg-success text-white' },
  CANCELLED: { label: 'Đã hủy đơn', class: 'bg-secondary text-white' },
}

const paymentStatusBadges = {
  UNPAID: { label: 'Chưa thanh toán', class: 'bg-warning text-dark' },
  PAID: { label: 'Đã thanh toán', class: 'bg-success text-white' },
  REFUNDED: { label: 'Đã hoàn tiền', class: 'bg-danger text-white' },
}

const fetchOrders = async () => {
  isLoading.value = true
  errorMessage.value = ''
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: keyword.value.trim() || undefined,
      status: selectedStatus.value || undefined,
      paymentStatus: selectedPaymentStatus.value || undefined,
    }
    const res = await orderApi.getAdminOrders(params)
    const pageData = res.data?.data
    orders.value = pageData?.content || []
    totalPages.value = pageData?.totalPages || 0
    totalElements.value = pageData?.totalElements || 0
  } catch (err) {
    errorMessage.value = err.response?.data?.message || 'Không thể tải danh sách đơn hàng.'
  } finally {
    isLoading.value = false
  }
}

// Cập nhật Trạng Thái Đơn Hàng (Realtime AJAX không reload)
const handleUpdateStatus = async (order, newStatus) => {
  if (order.status === newStatus) return
  try {
    await orderApi.updateOrderStatus(order.id, newStatus)
    order.status = newStatus
    if (newStatus === 'DELIVERED' && order.paymentMethod === 'COD') {
      order.paymentStatus = 'PAID'
    }
    successMessage.value = `Đã cập nhật đơn hàng ${order.orderCode} sang "${statusBadges[newStatus]?.label || newStatus}"`
    setTimeout(() => { successMessage.value = '' }, 3500)
  } catch (err) {
    errorMessage.value = err.response?.data?.message || 'Không thể cập nhật trạng thái đơn hàng.'
    setTimeout(() => { errorMessage.value = '' }, 3500)
    fetchOrders()
  }
}

// Cập nhật Trạng Thái Thanh Toán
const handleUpdatePaymentStatus = async (order, newPayStatus) => {
  if (order.paymentStatus === newPayStatus) return
  try {
    await orderApi.updateOrderPaymentStatus(order.id, newPayStatus)
    order.paymentStatus = newPayStatus
    successMessage.value = `Đã cập nhật thanh toán đơn ${order.orderCode} sang "${paymentStatusBadges[newPayStatus]?.label || newPayStatus}"`
    setTimeout(() => { successMessage.value = '' }, 3500)
  } catch (err) {
    errorMessage.value = err.response?.data?.message || 'Không thể cập nhật trạng thái thanh toán.'
    setTimeout(() => { errorMessage.value = '' }, 3500)
    fetchOrders()
  }
}

const openDetailModal = (order) => {
  selectedOrder.value = order
  isModalOpen.value = true
}

const closeModal = () => {
  isModalOpen.value = false
  selectedOrder.value = null
}

const changePage = (p) => {
  if (p >= 0 && p < totalPages.value) {
    currentPage.value = p
    fetchOrders()
  }
}

const formatPrice = (val) => {
  if (!val) return '0 đ'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)
}

watch([selectedStatus, selectedPaymentStatus], () => {
  currentPage.value = 0
  fetchOrders()
})

onMounted(() => {
  fetchOrders()
})
</script>

<template>
  <div class="admin-page py-3">
    <!-- Title Header -->
    <div class="d-flex align-items-center justify-content-between mb-4">
      <div>
        <h3 class="page-title font-weight-bold text-dark m-0">
          <i class="fa fa-shopping-bag text-danger mr-2"></i> Quản Lý Đơn Hàng (Order Management)
        </h3>
        <p class="text-muted small m-0">Theo dõi, xét duyệt đơn hàng, kiểm tra địa chỉ giao nhận và cập nhật trạng thái đơn hàng</p>
      </div>
      <div class="badge bg-danger text-white px-3 py-2 fs-6 shadow-sm">
        Tổng số: <strong>{{ totalElements }}</strong> đơn hàng
      </div>
    </div>

    <!-- Alert Notifications -->
    <div v-if="successMessage" class="alert alert-success alert-dismissible fade show shadow-sm" role="alert">
      <i class="fa fa-check-circle mr-2"></i> {{ successMessage }}
    </div>
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
              placeholder="Tìm theo Mã Đơn, Khách hàng, SĐT, Email..."
              @keyup.enter="fetchOrders"
            />
          </div>
        </div>

        <!-- Filter Trạng Thái Đơn -->
        <div class="col-md-3">
          <select v-model="selectedStatus" class="form-select">
            <option value="">-- Tất cả trạng thái đơn --</option>
            <option value="PENDING">Chờ xác nhận</option>
            <option value="CONFIRMED">Đã xác nhận</option>
            <option value="SHIPPING">Đang giao hàng</option>
            <option value="DELIVERED">Đã giao (Hoàn thành)</option>
            <option value="CANCELLED">Đã hủy đơn</option>
          </select>
        </div>

        <!-- Filter Trạng Thái Thanh Toán -->
        <div class="col-md-3">
          <select v-model="selectedPaymentStatus" class="form-select">
            <option value="">-- Tất cả trạng thái thanh toán --</option>
            <option value="UNPAID">Chưa thanh toán</option>
            <option value="PAID">Đã thanh toán</option>
            <option value="REFUNDED">Đã hoàn tiền</option>
          </select>
        </div>

        <!-- Search Button -->
        <div class="col-md-2">
          <button class="btn btn-dark w-100 font-weight-bold" @click="fetchOrders">
            <i class="fa fa-filter mr-1"></i> Lọc Đơn
          </button>
        </div>
      </div>
    </div>

    <!-- Table Main Content -->
    <div class="card border-0 shadow-sm rounded-lg overflow-hidden bg-white">
      <div class="card-body p-0">
        <div v-if="isLoading" class="py-5 text-center">
          <LoadingSpinner text="Đang tải danh sách đơn hàng..." />
        </div>

        <div v-else-if="orders.length === 0" class="py-5 text-center text-muted">
          <i class="fa fa-inbox fa-3x mb-3 d-block"></i>
          Không tìm thấy đơn hàng nào phù hợp với điều kiện tìm kiếm.
        </div>

        <div v-else class="table-responsive">
          <table class="table table-hover align-middle m-0 text-center">
            <thead class="table-dark text-uppercase small">
              <tr>
                <th>Mã Đơn</th>
                <th>Khách Hàng</th>
                <th>Số Điện Thoại</th>
                <th class="text-left">Địa Chỉ Giao Nhận</th>
                <th>Tỉnh / Phường / Thôn</th>
                <th>Tổng Tiền</th>
                <th>Thanh Toán</th>
                <th>Trạng Thái Đơn</th>
                <th style="width: 170px;">Cập Nhật Trạng Thái</th>
                <th>Chi Tiết</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="order in orders" :key="order.id">
                <!-- Mã Đơn -->
                <td>
                  <span class="font-weight-bold text-danger">{{ order.orderCode }}</span>
                  <small class="text-muted d-block">{{ new Date(order.createdAt).toLocaleDateString('vi-VN') }}</small>
                </td>

                <!-- Khách Hàng -->
                <td>
                  <span class="font-weight-bold text-dark d-block">{{ order.customerName }}</span>
                  <small class="text-muted">{{ order.userEmail || 'Khách vãng lai' }}</small>
                </td>

                <!-- Số Điện Thoại -->
                <td>
                  <span class="badge bg-light text-dark border"><i class="fa fa-phone mr-1"></i> {{ order.phone }}</span>
                </td>

                <!-- Địa Chỉ Đầy Đủ -->
                <td class="text-left" style="max-width: 220px;">
                  <small class="text-dark font-weight-bold d-block text-truncate" :title="order.fullAddress">
                    {{ order.fullAddress }}
                  </small>
                </td>

                <!-- Tỉnh / Phường / Thôn Số nhà -->
                <td>
                  <small class="d-block font-weight-bold text-primary">{{ order.province }}</small>
                  <small class="text-muted d-block">{{ order.ward }}</small>
                  <small class="text-muted text-truncate d-block" style="max-width: 120px;" :title="order.streetAddress">{{ order.streetAddress }}</small>
                </td>

                <!-- Tổng Tiền -->
                <td>
                  <span class="font-weight-bold text-danger h6 m-0">{{ formatPrice(order.totalAmount) }}</span>
                </td>

                <!-- Thanh Toán -->
                <td>
                  <span class="badge mb-1 d-block" :class="paymentStatusBadges[order.paymentStatus]?.class || 'bg-secondary'">
                    {{ paymentStatusBadges[order.paymentStatus]?.label || order.paymentStatus }}
                  </span>
                  <small class="badge bg-light text-dark border">{{ order.paymentMethod }}</small>
                </td>

                <!-- Trạng Thái Đơn -->
                <td>
                  <span class="badge px-3 py-2" :class="statusBadges[order.status]?.class || 'bg-secondary'">
                    {{ statusBadges[order.status]?.label || order.status }}
                  </span>
                </td>

                <!-- Cập Nhật Trạng Thái đơn hàng -->
                <td>
                  <select
                    :value="order.status"
                    class="form-select form-select-sm font-weight-bold"
                    @change="handleUpdateStatus(order, $event.target.value)"
                  >
                    <option value="PENDING">Chờ xác nhận</option>
                    <option value="CONFIRMED">Đã xác nhận</option>
                    <option value="SHIPPING">Đang giao hàng</option>
                    <option value="DELIVERED">Đã giao (Hoàn thành)</option>
                    <option value="CANCELLED">Đã hủy đơn</option>
                  </select>
                </td>

                <!-- Xem Chi Tiết -->
                <td>
                  <button class="btn btn-sm btn-outline-dark" @click="openDetailModal(order)" title="Xem chi tiết đơn">
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
            Trang {{ currentPage + 1 }} / {{ totalPages }} (Tổng {{ totalElements }} đơn hàng)
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

    <!-- MODAL POPUP CHI TIẾT ĐƠN HÀNG & SẢN PHẨM -->
    <div v-if="isModalOpen && selectedOrder" class="modal fade show d-block" style="background: rgba(0,0,0,0.6);" tabindex="-1">
      <div class="modal-dialog modal-lg modal-dialog-centered modal-dialog-scrollable">
        <div class="modal-content border-0 shadow-lg">
          <div class="modal-header bg-dark text-white">
            <h5 class="modal-title font-weight-bold">
              <i class="fa fa-file-text-o mr-2 text-danger"></i> CHI TIẾT ĐƠN HÀNG #{{ selectedOrder.orderCode }}
            </h5>
            <button type="button" class="btn-close btn-close-white" @click="closeModal"></button>
          </div>
          <div class="modal-body p-4 bg-light">
            <!-- Grid Thông Tin Giao Nhận -->
            <div class="row mb-4">
              <div class="col-md-6 mb-3">
                <div class="p-3 bg-white border rounded">
                  <h6 class="font-weight-bold text-dark border-bottom pb-2">Thông Tin Khách Hàng</h6>
                  <p class="m-0 small"><strong>Họ tên:</strong> {{ selectedOrder.customerName }}</p>
                  <p class="m-0 small"><strong>Số điện thoại:</strong> {{ selectedOrder.phone }}</p>
                  <p class="m-0 small"><strong>Email:</strong> {{ selectedOrder.email || selectedOrder.userEmail || 'Chưa cung cấp' }}</p>
                </div>
              </div>
              <div class="col-md-6 mb-3">
                <div class="p-3 bg-white border rounded">
                  <h6 class="font-weight-bold text-dark border-bottom pb-2">Địa Chỉ Giao Hàng</h6>
                  <p class="m-0 small"><strong>Tỉnh / Thành phố:</strong> {{ selectedOrder.province }}</p>
                  <p class="m-0 small"><strong>Phường / Xã:</strong> {{ selectedOrder.ward }}</p>
                  <p class="m-0 small"><strong>Thôn / Số nhà:</strong> {{ selectedOrder.streetAddress }}</p>
                  <p v-if="selectedOrder.note" class="m-0 small text-danger"><strong>Ghi chú:</strong> {{ selectedOrder.note }}</p>
                </div>
              </div>
            </div>

            <!-- Bảng Sản Phẩm Trong Đơn -->
            <h6 class="font-weight-bold text-dark mb-2">Danh Sách Sản Phẩm Đã Mua:</h6>
            <div class="table-responsive bg-white border rounded">
              <table class="table align-middle m-0 text-center">
                <thead class="table-light small text-uppercase">
                  <tr>
                    <th style="width: 60px;">Ảnh</th>
                    <th class="text-left">Sản Phẩm</th>
                    <th>Phân Loại</th>
                    <th>Đơn Giá</th>
                    <th>Số Lượng</th>
                    <th>Thành Tiền</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="item in selectedOrder.items" :key="item.id">
                    <td>
                      <img :src="item.productThumbnail || '/img/product/product-1.jpg'" class="rounded border" style="width: 45px; height: 45px; object-fit: cover;" />
                    </td>
                    <td class="text-left font-weight-bold text-dark">{{ item.productName }}</td>
                    <td>
                      <span v-if="item.size" class="badge bg-dark text-white mr-1">{{ item.size }}</span>
                      <span v-if="item.color" class="badge bg-secondary text-white">{{ item.color }}</span>
                    </td>
                    <td>{{ formatPrice(item.price) }}</td>
                    <td class="font-weight-bold">x{{ item.quantity }}</td>
                    <td class="font-weight-bold text-danger">{{ formatPrice(item.totalPrice) }}</td>
                  </tr>
                </tbody>
              </table>
            </div>

            <!-- Tổng Tiền Thanh Toán -->
            <div class="row pt-3">
              <div class="col-md-6 offset-md-6 text-end">
                <p class="m-0 small">Tạm tính: <strong>{{ formatPrice(selectedOrder.subtotal) }}</strong></p>
                <p class="m-0 small">Phí vận chuyển: <strong>{{ formatPrice(selectedOrder.shippingFee) }}</strong></p>
                <h5 class="font-weight-bold text-danger mt-2">
                  TỔNG CỘNG: {{ formatPrice(selectedOrder.totalAmount) }}
                </h5>
              </div>
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
