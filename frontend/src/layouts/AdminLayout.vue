<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

const authStore = useAuthStore()
const router = useRouter()

const isSidebarOpen = ref(true)

const handleLogout = async () => {
  await authStore.logoutAction()
  router.push('/login')
}
</script>

<template>
  <div class="admin-wrapper" :class="{ 'sidebar-collapsed': !isSidebarOpen }">
    <!-- Header Admin Bar (Style DeskApp) -->
    <header class="admin-header">
      <div class="header-left">
        <button class="menu-toggle" @click="isSidebarOpen = !isSidebarOpen" title="Ẩn/Hiện Sidebar">
          <i class="fa fa-bars"></i>
        </button>
        <div class="header-logo">
          <RouterLink to="/admin/dashboard" class="logo-link">
            <span class="logo-text">DeskApp</span>
            <span class="logo-badge">Admin</span>
          </RouterLink>
        </div>
      </div>

      <div class="header-right">
        <RouterLink to="/" class="btn-client-link" target="_blank" title="Xem cửa hàng Client">
          <i class="fa fa-globe"></i> <span>Xem Cửa Hàng</span>
        </RouterLink>

        <!-- User profile dropdown -->
        <div class="user-info">
          <div class="user-avatar">
            <i class="fa fa-user-circle-o"></i>
          </div>
          <div class="user-details">
            <span class="user-name">{{ authStore.user?.fullName || 'Administrator' }}</span>
            <span class="user-role">Quản trị viên</span>
          </div>
          <button class="btn-logout" @click="handleLogout" title="Đăng xuất">
            <i class="fa fa-sign-out"></i>
          </button>
        </div>
      </div>
    </header>

    <!-- Main Container -->
    <div class="admin-body">
      <!-- Sidebar Nav -->
      <aside class="admin-sidebar">
        <div class="sidebar-menu">
          <div class="menu-category">TỔNG QUAN</div>
          <ul class="nav-list">
            <li>
              <RouterLink to="/admin/dashboard" active-class="active">
                <i class="fa fa-dashboard nav-icon"></i>
                <span>Dashboard</span>
              </RouterLink>
            </li>
          </ul>

          <div class="menu-category">QUẢN LÝ DỮ LIỆU</div>
          <ul class="nav-list">
            <li>
              <RouterLink to="/admin/categories" active-class="active">
                <i class="fa fa-folder-open nav-icon"></i>
                <span>Chuyên mục</span>
              </RouterLink>
            </li>
            <li>
              <RouterLink to="/admin/brands" active-class="active">
                <i class="fa fa-tags nav-icon"></i>
                <span>Thương hiệu</span>
              </RouterLink>
            </li>
            <li>
              <RouterLink to="/admin/banners" active-class="active">
                <i class="fa fa-image nav-icon"></i>
                <span>Banner Quảng cáo</span>
              </RouterLink>
            </li>
            <li>
              <RouterLink to="/admin/products" active-class="active">
                <i class="fa fa-shopping-bag nav-icon"></i>
                <span>Sản phẩm & Biến thể</span>
              </RouterLink>
            </li>
          </ul>

          <div class="menu-category">ĐƠN HÀNG & KHÁCH HÀNG</div>
          <ul class="nav-list">
            <li>
              <RouterLink to="/admin/orders" active-class="active">
                <i class="fa fa-shopping-cart nav-icon"></i>
                <span>Quản lý Đơn hàng</span>
              </RouterLink>
            </li>
            <li>
              <RouterLink to="/admin/reviews" active-class="active">
                <i class="fa fa-star nav-icon"></i>
                <span>Đánh giá sản phẩm</span>
              </RouterLink>
            </li>
            <li>
              <RouterLink to="/admin/users" active-class="active">
                <i class="fa fa-users nav-icon"></i>
                <span>Quản lý Người dùng</span>
              </RouterLink>
            </li>
            <li>
              <RouterLink to="/admin/payments" active-class="active">
                <i class="fa fa-credit-card nav-icon"></i>
                <span>Giao dịch Thanh toán</span>
              </RouterLink>
            </li>
          </ul>
        </div>
      </aside>

      <!-- Main View Content -->
      <main class="admin-main">
        <div class="main-container">
          <RouterView />
        </div>
      </main>
    </div>
  </div>
</template>

<style scoped>
.admin-wrapper {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f4f5f7;
  font-family: 'Inter', system-ui, sans-serif;
  color: #333333;
}

/* Header */
.admin-header {
  height: 65px;
  background: #ffffff;
  border-bottom: 1px solid #e6e8ec;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  position: sticky;
  top: 0;
  z-index: 1000;
  box-shadow: 0 2px 4px rgba(0,0,0,0.02);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.menu-toggle {
  background: none;
  border: none;
  font-size: 20px;
  color: #555555;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
  transition: background 0.2s;
}

.menu-toggle:hover {
  background: #f0f2f5;
}

.logo-text {
  font-size: 1.35rem;
  font-weight: 800;
  color: #1b00ff;
  letter-spacing: -0.5px;
}

.logo-badge {
  background: #e53637;
  color: #ffffff;
  font-size: 0.7rem;
  font-weight: 700;
  padding: 2px 6px;
  border-radius: 4px;
  margin-left: 6px;
  text-transform: uppercase;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.btn-client-link {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.875rem;
  font-weight: 600;
  color: #4b5563;
  text-decoration: none;
  padding: 6px 12px;
  background: #f3f4f6;
  border-radius: 6px;
  transition: all 0.2s;
}

.btn-client-link:hover {
  background: #e5e7eb;
  color: #111827;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  border-left: 1px solid #e5e7eb;
  padding-left: 16px;
}

.user-avatar i {
  font-size: 28px;
  color: #6b7280;
}

.user-details {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: 0.875rem;
  font-weight: 700;
  color: #111827;
  line-height: 1.2;
}

.user-role {
  font-size: 0.75rem;
  color: #6b7280;
}

.btn-logout {
  background: none;
  border: none;
  color: #ef4444;
  font-size: 18px;
  cursor: pointer;
  padding: 6px;
  border-radius: 6px;
  transition: background 0.2s;
}

.btn-logout:hover {
  background: #fee2e2;
}

/* Body & Sidebar */
.admin-body {
  display: flex;
  flex: 1;
}

.admin-sidebar {
  width: 250px;
  background: #ffffff;
  border-right: 1px solid #e6e8ec;
  padding: 20px 0;
  transition: width 0.3s ease;
  flex-shrink: 0;
}

.sidebar-collapsed .admin-sidebar {
  width: 0;
  overflow: hidden;
  padding: 0;
  border: none;
}

.menu-category {
  font-size: 0.7rem;
  font-weight: 700;
  color: #9ca3af;
  padding: 12px 24px 6px;
  letter-spacing: 1px;
}

.nav-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.nav-list li a {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 11px 24px;
  color: #4b5563;
  font-size: 0.9rem;
  font-weight: 600;
  text-decoration: none;
  transition: all 0.2s;

}

.nav-list li a:hover {
  color: #1b00ff;
  background: #f8fafc;
}

.nav-list li a.active {
  color: #1b00ff;
  background: #eff6ff;
  border-right: 4px solid #1b00ff;
}

.nav-icon {
  font-size: 16px;
  width: 20px;
  text-align: center;
}

.admin-main {
  flex: 1;
  padding: 24px;
  overflow-x: hidden;
}

.main-container {
  max-width: 1400px;
  margin: 0 auto;
}
</style>
