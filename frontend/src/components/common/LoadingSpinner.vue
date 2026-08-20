<script setup>
defineProps({
  text: {
    type: String,
    default: 'Đang tải dữ liệu...'
  },
  overlay: {
    type: Boolean,
    default: false
  }
})
</script>

<template>
  <div :class="['spinner-container', { 'is-overlay': overlay }]">
    <div class="spinner-box">
      <div class="loader-circle"></div>
      <p v-if="text" class="spinner-text">{{ text }}</p>
    </div>
  </div>
</template>

<style scoped>
.spinner-container {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.spinner-container.is-overlay {
  position: fixed;
  inset: 0;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(4px);
  z-index: 9999;
  padding: 0;
}

.spinner-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.loader-circle {
  width: 44px;
  height: 44px;
  border: 3px solid #e2e8f0;
  border-top-color: #e53637;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.spinner-text {
  font-size: 0.9rem;
  color: #64748b;
  font-weight: 600;
  margin: 0;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
