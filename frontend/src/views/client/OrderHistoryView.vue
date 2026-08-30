<script setup>
import { ref, onMounted } from 'vue'
import * as orderApi from '@/api/orderApi'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'

const orders = ref([])
const isLoading = ref(true)
const errorMessage = ref('')
const successMessage = ref('')

// Modal Chi Tiết Đơn Hàng
const selectedOrder = ref(null)
const isModalOpen = ref(false)

const statusBadges = {
  PENDING: { label: 'Chờ xác nhận', class: 'bg-warning text-dark' },
  CONFIRMED: { label: 'Đã xác nhận', class: 'bg-info text-white' },
  SHIPPING: { label: 'Đang giao hàng', class: 'bg-primary text-white' },
  DELIVERED: { label: 'Đã giao thành công', class: 'bg-success text-white' },
  CANCELLED: { label: 'Đã hủy đơn', class: 'bg-secondary text-white' },
}

const paymentStatusBadges = {
  UNPAID: { label: 'Chưa thanh toán (COD)', class: 'bg-warning text-dark' },
  PAID: { label: 'Đã thanh toán', class: 'bg-success text-white' },
  REFUNDED: { label: 'Đã hoàn tiền', class: 'bg-danger text-white' },
}

const fetchUserOrders = async () => {
  isLoading.value = true
  errorMessage.value = ''
  try {
    const res = await orderApi.getUserOrders()
    orders.value = res.data?.data || []
  } catch (err) {
    errorMessage.value = err.response?.data?.message || 'Không thể tải danh sách đơn hàng của bạn.'
  } finally {
    isLoading.value = false
  }
}

const handleCancelOrder = async (orderId) => {
  if (!confirm('Bạn có chắc chắn muốn hủy đơn hàng này?')) return
  try {
    await orderApi.cancelUserOrder(orderId)
    successMessage.value = 'Đã hủy đơn hàng thành công!'
    setTimeout(() => { successMessage.value = '' }, 3500)
    fetchUserOrders()
  } catch (err) {
    errorMessage.value = err.response?.data?.message || 'Không thể hủy đơn hàng.'
    setTimeout(() => { errorMessage.value = '' }, 3500)
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

const formatPrice = (val) => {
  if (!val) return '0 đ'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)
}

onMounted(() => {
  fetchUserOrders()
})
</script>

<template>
  <div class="order-history-page py-4">
    <!-- Breadcrumb -->
    <div class="bg-light py-3 mb-4 border-bottom">
      <div class="container">
        <div class="d-flex align-items-center justify-content-between">
          <h4 class="font-weight-bold text-dark m-0">
            <i class="fa fa-history text-danger mr-2"></i> LỊCH SỬ ĐƠN HÀNG (MY ORDERS)
          </h4>
          <nav aria-label="breadcrumb">
            <ol class="breadcrumb m-0 bg-transparent p-0">
              <li class="breadcrumb-item"><RouterLink to="/" class="text-muted">Trang chủ</RouterLink></li>
              <li class="breadcrumb-item active text-dark font-weight-bold" aria-current="page">Orders</li>
            </ol>
          </nav>
        </div>
      </div>
    </div>

    <!-- Container Chính -->
    <div class="container">
      <div v-if="successMessage" class="alert alert-success shadow-sm alert-dismissible fade show mb-4" role="alert">
        <i class="fa fa-check-circle mr-2"></i> {{ successMessage }}
      </div>
      <div v-if="errorMessage" class="alert alert-danger shadow-sm alert-dismissible fade show mb-4" role="alert">
        <i class="fa fa-exclamation-triangle mr-2"></i> {{ errorMessage }}
      </div>

      <div v-if="isLoading" class="py-5 text-center">
        <LoadingSpinner text="Đang nạp danh sách đơn hàng..." />
      </div>

      <div v-else-if="orders.length === 0" class="py-5 text-center my-4 bg-white border rounded shadow-sm">
        <i class="fa fa-shopping-bag fa-4x text-muted mb-3 d-block"></i>
        <h5 class="font-weight-bold text-dark mb-2">Bạn chưa có đơn hàng nào</h5>
        <p class="text-muted small mb-4">Hãy trải nghiệm mua sắm các mẫu thời trang mới nhất ngay hôm nay!</p>
        <RouterLink to="/products" class="btn btn-danger font-weight-bold px-5 py-2">Khám Phá Cửa Hàng Ngay</RouterLink>
      </div>

      <!-- Bảng Danh Sách Đơn Hàng -->
      <div v-else class="card border-0 shadow-sm rounded-lg overflow-hidden bg-white">
        <div class="card-body p-0">
          <div class="table-responsive">
            <table class="table table-hover align-middle m-0 text-center">
              <thead class="table-dark text-uppercase small">
                <tr>
                  <th>Mã Đơn Hàng</th>
                  <th>Ngày Đặt</th>
                  <th class="text-left">Người Nhận & Địa Chỉ</th>
                  <th>Tổng Tiền</th>
                  <th>Thanh Toán</th>
                  <th>Trạng Thái Vận Chuyển</th>
                  <th style="width: 180px;">Thao Tác</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="order in orders" :key="order.id">
                  <!-- Mã đơn hàng -->
                  <td>
                    <span class="font-weight-bold text-danger">{{ order.orderCode }}</span>
                  </td>

                  <!-- Ngày đặt -->
                  <td>
                    <small class="text-dark font-weight-bold d-block">{{ new Date(order.createdAt).toLocaleDateString('vi-VN') }}</small>
                    <small class="text-muted">{{ new Date(order.createdAt).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }) }}</small>
                  </td>

                  <!-- Người nhận & Địa chỉ -->
                  <td class="text-left" style="max-width: 220px;">
                    <span class="font-weight-bold text-dark d-block">{{ order.customerName }} ({{ order.phone }})</span>
                    <small class="text-muted text-truncate d-block" :title="order.fullAddress">{{ order.fullAddress }}</small>
                  </td>

                  <!-- Tổng tiền -->
                  <td>
                    <span class="font-weight-bold text-danger h6 m-0">{{ formatPrice(order.totalAmount) }}</span>
                  </td>

                  <!-- Thanh toán -->
                  <td>
                    <span class="badge mb-1 d-block" :class="paymentStatusBadges[order.paymentStatus]?.class || 'bg-secondary'">
                      {{ paymentStatusBadges[order.paymentStatus]?.label || order.paymentStatus }}
                    </span>
                    <small class="badge bg-light text-dark border">{{ order.paymentMethod }}</small>
                  </td>

                  <!-- Trạng thái vận chuyển -->
                  <td>
                    <span class="badge px-3 py-2" :class="statusBadges[order.status]?.class || 'bg-secondary'">
                      {{ statusBadges[order.status]?.label || order.status }}
                    </span>
                  </td>

                  <!-- Thao tác -->
                  <td>
                    <div class="d-flex justify-content-center gap-2">
                      <button class="btn btn-sm btn-outline-dark font-weight-bold" @click="openDetailModal(order)">
                        <i class="fa fa-eye mr-1"></i> Chi Tiết
                      </button>

                      <button
                        v-if="order.status === 'PENDING'"
                        class="btn btn-sm btn-outline-danger"
                        @click="handleCancelOrder(order.id)"
                        title="Hủy đơn hàng"
                      >
                        <i class="fa fa-times mr-1"></i> Hủy Đơn
                      </button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>

    <!-- MODAL POPUP CHI TIẾT ĐƠN HÀNG KÈM BẢNG SẢN PHẨM -->
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
                  <h6 class="font-weight-bold text-dark border-bottom pb-2">Thông Tin Người Nhận</h6>
                  <p class="m-0 small"><strong>Họ tên:</strong> {{ selectedOrder.customerName }}</p>
                  <p class="m-0 small"><strong>Số điện thoại:</strong> {{ selectedOrder.phone }}</p>
                  <p class="m-0 small"><strong>Email:</strong> {{ selectedOrder.email || selectedOrder.userEmail }}</p>
                </div>
              </div>
              <div class="col-md-6 mb-3">
                <div class="p-3 bg-white border rounded">
                  <h6 class="font-weight-bold text-dark border-bottom pb-2">Địa Chỉ Nhận Hàng</h6>
                  <p class="m-0 small"><strong>Địa chỉ:</strong> {{ selectedOrder.fullAddress }}</p>
                  <p class="m-0 small"><strong>Phương thức:</strong> {{ selectedOrder.paymentMethod }} (COD)</p>
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
                    <th>Kích Cỡ / Màu</th>
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
                <p class="m-0 small">Phí vận chuyển: <strong>Miễn phí</strong></p>
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
