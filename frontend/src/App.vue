<script setup>
import { onMounted } from 'vue'
import { useAuthStore } from '@/stores/authStore'

const authStore = useAuthStore()

onMounted(async () => {
  // Nếu có access token từ trước, tự động tải thông tin người dùng
  if (authStore.accessToken && !authStore.user) {
    await authStore.fetchCurrentUser()
  }
})
</script>

<template>
  <RouterView />
</template>

<style>
/* CSS Reset và Global Styles cơ bản */
* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

body {
  font-family: 'Inter', system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
  background-color: #0f0f1a;
  color: #f1f5f9;
  min-height: 100vh;
  -webkit-font-smoothing: antialiased;
}
</style>
