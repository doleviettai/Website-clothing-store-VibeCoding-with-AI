<script setup>
import { ref } from 'vue'

const orders = ref([
  { code: 'ORD-20300819-001', customer: 'Nguyễn Văn A', phone: '0901234567', total: 2530000, status: 'COMPLETED', statusText: 'Hoàn thành', payment: 'PAID', paymentText: 'Đã thanh toán', date: '19/08/2026' },
  { code: 'ORD-20300819-002', customer: 'Trần Thị B', phone: '0912345678', total: 890000, status: 'SHIPPING', statusText: 'Đang giao', payment: 'UNPAID', paymentText: 'Chưa thanh toán (COD)', date: '19/08/2026' },
  { code: 'ORD-20300819-003', customer: 'Lê Hoàng C', phone: '0987654321', total: 1550000, status: 'PROCESSING', statusText: 'Đang xử lý', payment: 'PAID', paymentText: 'Đã thanh toán', date: '19/08/2026' },
  { code: 'ORD-20300818-005', customer: 'Phạm Minh D', phone: '0933445566', total: 490000, status: 'PENDING', statusText: 'Chờ xác nhận', payment: 'UNPAID', paymentText: 'Chưa thanh toán', date: '18/08/2026' },
])

const updateStatus = (order, newStatus) => {
  order.status = newStatus
  alert(`Cập nhật đơn hàng ${order.code} sang trạng thái: ${newStatus}`)
}

const formatPrice = (val) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)
</script>

<template>
  <div class="admin-page">
    <div class="d-flex align-items-center justify-content-between mb-4">
      <div>
        <h3 class="page-title">Quản Lý Đơn Hàng</h3>
        <p class="text-muted mb-0">Theo dõi, duyệt đơn hàng và cập nhật trạng thái vận chuyển</p>
      </div>
    </div>

    <div class="card border-0 shadow-sm">
      <div class="card-body p-0">
        <div class="table-responsive">
          <table class="table table-hover align-middle mb-0 text-center">
            <thead class="bg-light">
              <tr>
                <th>Mã Đơn</th>
                <th>Khách Hàng</th>
                <th>Số Điện Thoại</th>
                <th>Tổng Tiền</th>
                <th>Thanh Toán</th>
                <th>Trạng Thái Đơn</th>
                <th>Cập Nhật Trạng Thái</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="order in orders" :key="order.code">
                <td class="font-weight-bold text-danger">{{ order.code }}</td>
                <td class="font-weight-bold">{{ order.customer }}</td>
                <td>{{ order.phone }}</td>
                <td class="font-weight-bold">{{ formatPrice(order.total) }}</td>
                <td>
                  <span class="badge" :class="order.payment === 'PAID' ? 'bg-success text-white' : 'bg-warning text-dark'">
                    {{ order.paymentText }}
                  </span>
                </td>
                <td>
                  <span class="badge" :class="{
                    'bg-success text-white': order.status === 'COMPLETED',
                    'bg-primary text-white': order.status === 'SHIPPING',
                    'bg-info text-white': order.status === 'PROCESSING',
                    'bg-warning text-dark': order.status === 'PENDING'
                  }">
                    {{ order.statusText }}
                  </span>
                </td>
                <td>
                  <select
                    :value="order.status"
                    @change="updateStatus(order, $event.target.value)"
                    class="form-control form-control-sm d-inline-block w-auto"
                  >
                    <option value="PENDING">Chờ xác nhận</option>
                    <option value="PROCESSING">Đang xử lý</option>
                    <option value="SHIPPING">Đang giao hàng</option>
                    <option value="COMPLETED">Hoàn thành</option>
                    <option value="CANCELLED">Hủy đơn</option>
                  </select>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-title { font-size: 1.5rem; font-weight: 700; color: #111827; }
</style>
