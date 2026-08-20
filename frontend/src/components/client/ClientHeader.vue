<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

const authStore = useAuthStore()
const router = useRouter()

const isOffcanvasOpen = ref(false)

const emit = defineEmits(['openSearch'])

const handleLogout = async () => {
  await authStore.logoutAction()
  router.push('/login')
}

const triggerSearch = () => {
  emit('openSearch')
}
</script>

<template>
  <!-- Offcanvas Menu Mobile -->
  <div class="offcanvas-menu-overlay" :class="{ active: isOffcanvasOpen }" @click="isOffcanvasOpen = false"></div>
  <div class="offcanvas-menu-wrapper" :class="{ active: isOffcanvasOpen }">
    <div class="offcanvas__option">
      <div class="offcanvas__links">
        <template v-if="!authStore.isAuthenticated">
          <RouterLink to="/login">Đăng nhập</RouterLink>
          <RouterLink to="/register">Đăng ký</RouterLink>
        </template>
        <template v-else>
          <span class="user-greeting">Xin chào, {{ authStore.user?.fullName }}</span>
          <RouterLink v-if="authStore.isAdmin" to="/admin/dashboard">Quản trị</RouterLink>
          <a href="#" @click.prevent="handleLogout">Đăng xuất</a>
        </template>
      </div>
    </div>
    <div class="offcanvas__nav__option">
      <a href="#" @click.prevent="triggerSearch"><img src="/img/icon/search.png" alt="Search"></a>
      <RouterLink to="/favorites"><img src="/img/icon/heart.png" alt="Wishlist"></RouterLink>
      <RouterLink to="/cart"><img src="/img/icon/cart.png" alt="Cart"> <span>0</span></RouterLink>
      <div class="price">0đ</div>
    </div>
    <div class="offcanvas__text">
      <p>Miễn phí vận chuyển, đổi trả trong vòng 30 ngày.</p>
    </div>
  </div>

  <!-- Main Header -->
  <header class="header">
    <div class="header__top">
      <div class="container">
        <div class="row">
          <div class="col-lg-6 col-md-7">
            <div class="header__top__left">
              <p>Miễn phí vận chuyển, bảo hành đổi trả trong 30 ngày.</p>
            </div>
          </div>
          <div class="col-lg-6 col-md-5">
            <div class="header__top__right">
              <div class="header__top__links">
                <template v-if="!authStore.isAuthenticated">
                  <RouterLink to="/login">Đăng nhập</RouterLink>
                  <RouterLink to="/register">Đăng ký</RouterLink>
                </template>
                <template v-else>
                  <span class="user-name">Xin chào, {{ authStore.user?.fullName }}</span>
                  <RouterLink v-if="authStore.isAdmin" to="/admin/dashboard" class="admin-badge">Trang Admin</RouterLink>
                  <a href="#" @click.prevent="handleLogout" class="logout-link">Đăng xuất</a>
                </template>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="container">
      <div class="row align-items-center">
        <div class="col-lg-3 col-md-3">
          <div class="header__logo">
            <RouterLink to="/"><img src="/img/logo.png" alt="Male Fashion Logo"></RouterLink>
          </div>
        </div>
        <div class="col-lg-6 col-md-6">
          <nav class="header__menu mobile-menu">
            <ul>
              <li><RouterLink to="/" active-class="active">Trang chủ</RouterLink></li>
              <li><RouterLink to="/products" active-class="active">Cửa hàng</RouterLink></li>
              <li v-if="authStore.isAuthenticated"><RouterLink to="/favorites" active-class="active">Yêu thích</RouterLink></li>
              <li v-if="authStore.isAuthenticated"><RouterLink to="/orders" active-class="active">Đơn hàng</RouterLink></li>
            </ul>
          </nav>
        </div>
        <div class="col-lg-3 col-md-3">
          <div class="header__nav__option">
            <a href="#" @click.prevent="triggerSearch" title="Tìm kiếm"><img src="/img/icon/search.png" alt="Search"></a>
            <RouterLink to="/favorites" title="Sản phẩm yêu thích"><img src="/img/icon/heart.png" alt="Wishlist"></RouterLink>
            <RouterLink to="/cart" title="Giỏ hàng"><img src="/img/icon/cart.png" alt="Cart"> <span>0</span></RouterLink>
            <div class="price">0đ</div>
          </div>
        </div>
      </div>
      <div class="canvas__open" @click="isOffcanvasOpen = true"><i class="fa fa-bars"></i></div>
    </div>
  </header>
</template>

<style scoped>
.header__top__links a,
.header__top__links span {
  color: #ffffff;
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 1px;
  margin-right: 15px;
  display: inline-block;
}

.header__top__links a:hover {
  color: #e53637;
}

.user-name {
  color: #ffffff !important;
  font-weight: 600;
}

.admin-badge {
  color: #f59e0b !important;
  font-weight: 700;
}

.logout-link {
  color: #f87171 !important;
}

.offcanvas-menu-wrapper.active {
  left: 0;
  opacity: 1;
  visibility: visible;
}

.offcanvas-menu-overlay.active {
  opacity: 1;
  visibility: visible;
}
</style>
