<script setup>
/**
 * AuthLayout — Layout giao diện màu trắng sáng (Light Theme) đồng bộ với MaleFashion.
 * Sử dụng background sáng sạch sẽ, card trắng viền nhẹ, logo thương hiệu tinh tế.
 */
</script>

<template>
  <div class="auth-wrapper">
    <!-- Card container trung tâm -->
    <div class="auth-container">
      <!-- Branding Logo -->
      <div class="auth-brand">
        <RouterLink to="/" class="brand-link">
          <img src="/img/logo.png" alt="Male Fashion Logo" class="brand-logo" />
        </RouterLink>
        <p class="brand-tagline">Thời trang nam hiện đại & phong cách hàng đầu</p>
      </div>

      <!-- Card form (Login / Register) -->
      <div class="auth-card">
        <RouterView />
      </div>

      <!-- Footer copyright nhỏ -->
      <div class="auth-footer">
        <p>© {{ new Date().getFullYear() }} Clothing Store — Male Fashion. All rights reserved.</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.auth-wrapper {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8fafc;
  padding: 40px 20px;
  font-family: 'Nunito Sans', sans-serif;
}

.auth-container {
  width: 100%;
  max-width: 480px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.auth-brand {
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.brand-logo {
  height: 38px;
  width: auto;
}

.brand-tagline {
  font-size: 0.875rem;
  color: #64748b;
  margin: 0;
}

.auth-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  padding: 36px 32px;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.05), 0 8px 10px -6px rgba(0, 0, 0, 0.01);
}

.auth-footer {
  text-align: center;
  font-size: 0.8rem;
  color: #94a3b8;
}

@media (max-width: 575px) {
  .auth-card {
    padding: 28px 20px;
    border-radius: 12px;
  }
}
</style>
