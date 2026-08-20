<script setup>
import { ref } from 'vue'

const orders = ref([
  {
    orderCode: 'ORD-20300819-001',
    placedAt: '19/08/2026 10:30',
    totalAmount: 2530000,
    status: 'COMPLETED',
    statusText: 'Hoàn thành',
    paymentStatus: 'PAID',
    paymentStatusText: 'Đã thanh toán',
    itemsCount: 2,
  },
  {
    orderCode: 'ORD-20300818-004',
    placedAt: '18/08/2026 14:15',
    totalAmount: 890000,
    status: 'SHIPPING',
    statusText: 'Đang giao hàng',
    paymentStatus: 'UNPAID',
    paymentStatusText: 'Chưa thanh toán (COD)',
    itemsCount: 1,
  },
])

const formatPrice = (val) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)
</script>

<template>
  <div class="orders-page">
    <section class="breadcrumb-option">
      <div class="container">
        <div class="row">
          <div class="col-lg-12">
            <div class="breadcrumb__text">
              <h4>Lịch sử đơn hàng</h4>
              <div class="breadcrumb__links">
                <RouterLink to="/">Trang chủ</RouterLink>
                <span>Lịch sử đơn hàng</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="shopping-cart spad">
      <div class="container">
        <div class="row">
          <div class="col-lg-12">
            <div class="table-responsive">
              <table class="table border text-center align-middle">
                <thead class="thead-dark">
                  <tr>
                    <th>Mã đơn hàng</th>
                    <th>Ngày đặt</th>
                    <th>Số lượng</th>
                    <th>Tổng tiền</th>
                    <th>Trạng thái đơn</th>
                    <th>Thanh toán</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="order in orders" :key="order.orderCode">
                    <td class="font-weight-bold text-danger">{{ order.orderCode }}</td>
                    <td>{{ order.placedAt }}</td>
                    <td>{{ order.itemsCount }} sản phẩm</td>
                    <td class="font-weight-bold">{{ formatPrice(order.totalAmount) }}</td>
                    <td>
                      <span class="badge" :class="order.status === 'COMPLETED' ? 'badge-success' : 'badge-primary'">
                        {{ order.statusText }}
                      </span>
                    </td>
                    <td>
                      <span class="badge" :class="order.paymentStatus === 'PAID' ? 'badge-success' : 'badge-warning'">
                        {{ order.paymentStatusText }}
                      </span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.breadcrumb-option {
  background: #f3f2ee;
  padding: 40px 0;
}

.table th {
  background: #111111;
  color: #ffffff;
}
</style>
