<script setup>
import { ref } from 'vue'

const summary = ref({
  totalUsers: 1250,
  totalProducts: 340,
  totalOrders: 820,
  totalRevenue: 358000000,
  newOrdersCount: 15,
  lowStockCount: 4,
})

const recentOrders = ref([
  { code: 'ORD-20300819-001', customer: 'Nguyễn Văn A', amount: 2530000, status: 'COMPLETED', statusText: 'Hoàn thành', date: '19/08/2026' },
  { code: 'ORD-20300819-002', customer: 'Trần Thị B', amount: 890000, status: 'SHIPPING', statusText: 'Đang giao', date: '19/08/2026' },
  { code: 'ORD-20300819-003', customer: 'Lê Hoàng C', amount: 1550000, status: 'PROCESSING', statusText: 'Đang xử lý', date: '19/08/2026' },
  { code: 'ORD-20300818-005', customer: 'Phạm Minh D', amount: 490000, status: 'PENDING', statusText: 'Chờ xác nhận', date: '18/08/2026' },
])

const formatPrice = (val) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)
</script>

<template>
  <div class="dashboard-page">
    <div class="page-header mb-4">
      <h3 class="page-title">Dashboard Tổng Quan</h3>
      <p class="text-muted">Báo cáo chỉ số hoạt động kinh doanh của Cửa hàng</p>
    </div>

    <!-- Stat Widgets (DeskApp Style) -->
    <div class="row">
      <div class="col-xl-3 col-lg-6 col-md-6 mb-4">
        <div class="stat-card border-left-primary">
          <div class="stat-body">
            <div>
              <div class="stat-title">Doanh Thu Tích Lũy</div>
              <div class="stat-value text-primary">{{ formatPrice(summary.totalRevenue) }}</div>
            </div>
            <div class="stat-icon bg-primary-light">
              <i class="fa fa-dollar"></i>
            </div>
          </div>
        </div>
      </div>

      <div class="col-xl-3 col-lg-6 col-md-6 mb-4">
        <div class="stat-card border-left-success">
          <div class="stat-body">
            <div>
              <div class="stat-title">Tổng Đơn Hàng</div>
              <div class="stat-value text-success">{{ summary.totalOrders }}</div>
            </div>
            <div class="stat-icon bg-success-light">
              <i class="fa fa-shopping-cart"></i>
            </div>
          </div>
        </div>
      </div>

      <div class="col-xl-3 col-lg-6 col-md-6 mb-4">
        <div class="stat-card border-left-info">
          <div class="stat-body">
            <div>
              <div class="stat-title">Tổng Sản Phẩm</div>
              <div class="stat-value text-info">{{ summary.totalProducts }}</div>
            </div>
            <div class="stat-icon bg-info-light">
              <i class="fa fa-shopping-bag"></i>
            </div>
          </div>
        </div>
      </div>

      <div class="col-xl-3 col-lg-6 col-md-6 mb-4">
        <div class="stat-card border-left-warning">
          <div class="stat-body">
            <div>
              <div class="stat-title">Khách Hàng</div>
              <div class="stat-value text-warning">{{ summary.totalUsers }}</div>
            </div>
            <div class="stat-icon bg-warning-light">
              <i class="fa fa-users"></i>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Recent Orders Table -->
    <div class="card shadow-sm border-0 mb-4">
      <div class="card-header bg-white py-3 d-flex align-items-center justify-content-between">
        <h5 class="m-0 font-weight-bold text-dark">Đơn Hàng Mới Nhất</h5>
        <RouterLink to="/admin/orders" class="btn btn-sm btn-outline-primary">Xem tất cả</RouterLink>
      </div>
      <div class="card-body p-0">
        <div class="table-responsive">
          <table class="table table-hover align-middle mb-0 text-center">
            <thead class="bg-light">
              <tr>
                <th>Mã Đơn</th>
                <th>Khách Hàng</th>
                <th>Tổng Tiền</th>
                <th>Trạng Thái</th>
                <th>Ngày Đặt</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="order in recentOrders" :key="order.code">
                <td class="font-weight-bold">{{ order.code }}</td>
                <td>{{ order.customer }}</td>
                <td class="font-weight-bold text-dark">{{ formatPrice(order.amount) }}</td>
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
                <td>{{ order.date }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: #111827;
  margin: 0;
}

.stat-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}

.border-left-primary { border-left: 4px solid #3b82f6; }
.border-left-success { border-left: 4px solid #10b981; }
.border-left-info { border-left: 4px solid #06b6d4; }
.border-left-warning { border-left: 4px solid #f59e0b; }

.stat-body {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.stat-title {
  font-size: 0.8rem;
  font-weight: 700;
  color: #6b7280;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 6px;
}

.stat-value {
  font-size: 1.35rem;
  font-weight: 800;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.bg-primary-light { background: #eff6ff; color: #3b82f6; }
.bg-success-light { background: #ecfdf5; color: #10b981; }
.bg-info-light { background: #ecfeff; color: #06b6d4; }
.bg-warning-light { background: #fffbeb; color: #f59e0b; }
</style>
