<script setup>
import { ref } from 'vue'

const payments = ref([
  { id: 1, orderCode: 'ORD-20300819-001', provider: 'VNPAY', method: 'BANK_TRANSFER', amount: 2530000, status: 'PAID', statusText: 'Thành công', txnId: 'VNPAY14890231', date: '19/08/2026' },
  { id: 2, orderCode: 'ORD-20300819-002', provider: 'COD', method: 'CASH_ON_DELIVERY', amount: 890000, status: 'UNPAID', statusText: 'Chưa thanh toán', txnId: '-', date: '19/08/2026' },
  { id: 3, orderCode: 'ORD-20300819-003', provider: 'MOMO', method: 'E_WALLET', amount: 1550000, status: 'PAID', statusText: 'Thành công', txnId: 'MOMO9928174', date: '19/08/2026' },
])

const formatPrice = (val) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)
</script>

<template>
  <div class="admin-page">
    <div class="d-flex align-items-center justify-content-between mb-4">
      <div>
        <h3 class="page-title">Quản Lý Thanh Toán & Giao Dịch</h3>
        <p class="text-muted mb-0">Theo dõi nhật ký giao dịch qua các cổng thanh toán (COD, VNPAY, MOMO)</p>
      </div>
    </div>

    <div class="card border-0 shadow-sm">
      <div class="card-body p-0">
        <div class="table-responsive">
          <table class="table table-hover align-middle mb-0 text-center">
            <thead class="bg-light">
              <tr>
                <th>ID</th>
                <th>Mã Đơn Hàng</th>
                <th>Cổng Thanh Toán</th>
                <th>Phương Thức</th>
                <th>Mã Giao Dịch Cổng</th>
                <th>Số Tiền</th>
                <th>Trạng Thái</th>
                <th>Ngày Giao Dịch</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="p in payments" :key="p.id">
                <td>#{{ p.id }}</td>
                <td class="font-weight-bold text-danger">{{ p.orderCode }}</td>
                <td><span class="badge bg-dark text-white">{{ p.provider }}</span></td>
                <td>{{ p.method }}</td>
                <td><code>{{ p.txnId }}</code></td>
                <td class="font-weight-bold text-success">{{ formatPrice(p.amount) }}</td>
                <td>
                  <span class="badge" :class="p.status === 'PAID' ? 'bg-success text-white' : 'bg-warning text-dark'">
                    {{ p.statusText }}
                  </span>
                </td>
                <td>{{ p.date }}</td>
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
