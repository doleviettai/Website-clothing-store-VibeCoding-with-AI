<script setup>
import { reactive } from 'vue'
import { useAuthStore } from '@/stores/authStore'

const authStore = useAuthStore()

const profile = reactive({
  fullName: authStore.user?.fullName || '',
  email: authStore.user?.email || '',
  phone: authStore.user?.phone || '',
})

const handleUpdateProfile = () => {
  alert('Cập nhật hồ sơ thành công!')
}
</script>

<template>
  <div class="profile-page">
    <section class="breadcrumb-option">
      <div class="container">
        <div class="row">
          <div class="col-lg-12">
            <div class="breadcrumb__text">
              <h4>Hồ sơ cá nhân</h4>
              <div class="breadcrumb__links">
                <RouterLink to="/">Trang chủ</RouterLink>
                <span>Tài khoản</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="checkout spad">
      <div class="container">
        <div class="checkout__form">
          <form @submit.prevent="handleUpdateProfile">
            <div class="row justify-content-center">
              <div class="col-lg-8">
                <h6 class="checkout__title">Thông tin tài khoản</h6>
                <div class="checkout__input">
                  <p>Họ và tên<span>*</span></p>
                  <input v-model="profile.fullName" type="text" required>
                </div>
                <div class="checkout__input">
                  <p>Địa chỉ Email (Không thể thay đổi)</p>
                  <input v-model="profile.email" type="email" disabled class="bg-light">
                </div>
                <div class="checkout__input">
                  <p>Số điện thoại</p>
                  <input v-model="profile.phone" type="text">
                </div>
                <button type="submit" class="site-btn">Lưu thay đổi</button>
              </div>
            </div>
          </form>
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

.site-btn {
  font-size: 14px;
  color: #ffffff;
  background: #111111;
  font-weight: 700;
  border: none;
  letter-spacing: 2px;
  text-transform: uppercase;
  padding: 14px 30px;
  display: inline-block;
}

.site-btn:hover {
  background: #e53637;
}
</style>
