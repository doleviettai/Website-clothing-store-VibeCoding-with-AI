<script setup>
import { ref } from 'vue'
import ClientHeader from '@/components/client/ClientHeader.vue'
import ClientFooter from '@/components/client/ClientFooter.vue'
import SearchModal from '@/components/client/SearchModal.vue'

const isSearchOpen = ref(false)
</script>

<template>
  <div class="client-layout">
    <!-- Header -->
    <ClientHeader @openSearch="isSearchOpen = true" />

    <!-- Router View chính của khu vực Client -->
    <main class="main-content">
      <RouterView />
    </main>

    <!-- Footer -->
    <ClientFooter />

    <!-- Search Modal -->
    <SearchModal :isOpen="isSearchOpen" @close="isSearchOpen = false" />
  </div>
</template>

<style scoped>
.client-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #ffffff;
  color: #111111;
}

.main-content {
  flex: 1;
}
</style>
