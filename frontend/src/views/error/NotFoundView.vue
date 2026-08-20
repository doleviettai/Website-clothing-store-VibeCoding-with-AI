<script setup>
/**
 * Trang 404 Not Found — Không tìm thấy trang
 */
</script>

<template>
  <div class="error-page">
    <div class="error-card">
      <h1 class="error-code">404</h1>
      <h2 class="error-title">Trang không tồn tại</h2>
      <p class="error-desc">Rất tiếc, trang bạn đang tìm kiếm không tồn tại hoặc đã được di chuyển sang một đường dẫn khác.</p>
      <div class="error-actions">
        <RouterLink to="/" class="btn-home">Quay về Trang Chủ</RouterLink>
        <RouterLink to="/products" class="btn-shop">Xem Cửa Hàng</RouterLink>
      </div>
    </div>
  </div>
</template>

<style scoped>
.error-page {
  min-height: 80vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  background: #f8fafc;
  font-family: 'Nunito Sans', sans-serif;
}

.error-card {
  max-width: 500px;
  text-align: center;
  background: #ffffff;
  padding: 48px 36px;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.05);
}

.error-code {
  font-size: 5rem;
  font-weight: 900;
  color: #e53637;
  margin: 0;
  line-height: 1;
}

.error-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: #111111;
  margin: 16px 0 12px;
}

.error-desc {
  font-size: 0.9375rem;
  color: #64748b;
  margin-bottom: 28px;
  line-height: 1.6;
}

.error-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
}

.btn-home {
  padding: 12px 24px;
  background: #111111;
  color: #ffffff;
  border-radius: 8px;
  font-weight: 700;
  text-decoration: none;
  transition: background 0.2s;
}

.btn-home:hover {
  background: #e53637;
}

.btn-shop {
  padding: 12px 24px;
  background: #f1f5f9;
  color: #334155;
  border-radius: 8px;
  font-weight: 700;
  text-decoration: none;
  transition: background 0.2s;
}

.btn-shop:hover {
  background: #e2e8f0;
}
</style>
