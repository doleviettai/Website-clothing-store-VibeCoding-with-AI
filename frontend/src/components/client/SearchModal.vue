<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  isOpen: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['close'])

const router = useRouter()
const keyword = ref('')

const handleSearch = () => {
  if (keyword.value.trim()) {
    router.push({ path: '/products', query: { keyword: keyword.value.trim() } })
    emit('close')
    keyword.value = ''
  }
}
</script>

<template>
  <div class="search-model" :class="{ active: isOpen }">
    <div class="h-100 d-flex align-items-center justify-content-center">
      <div class="search-close-switch" @click="$emit('close')">+</div>
      <form class="search-model-form" @submit.prevent="handleSearch">
        <input
          v-model="keyword"
          type="text"
          id="search-input"
          placeholder="Nhập tên sản phẩm để tìm kiếm....."
          autocomplete="off"
        >
      </form>
    </div>
  </div>
</template>

<style scoped>
.search-model {
  display: none;
  position: fixed;
  width: 100%;
  height: 100%;
  left: 0;
  top: 0;
  background: #000000;
  z-index: 99999;
  opacity: 0;
  transition: opacity 0.4s ease;
}

.search-model.active {
  display: block;
  opacity: 0.95;
}

.search-close-switch {
  position: absolute;
  width: 50px;
  height: 50px;
  background: #333333;
  color: #ffffff;
  text-align: center;
  line-height: 46px;
  font-size: 32px;
  border-radius: 50%;
  cursor: pointer;
  display: inline-block;
  right: 30px;
  top: 30px;
  transform: rotate(45deg);
  transition: all 0.3s;
}

.search-close-switch:hover {
  background: #e53637;
}

.search-model-form input {
  width: 500px;
  font-size: 32px;
  border: none;
  border-bottom: 2px solid #333333;
  background: transparent;
  color: #ffffff;
  padding-bottom: 10px;
}

.search-model-form input::placeholder {
  color: #666666;
}

@media (max-width: 575px) {
  .search-model-form input {
    width: 280px;
    font-size: 20px;
  }
}
</style>
