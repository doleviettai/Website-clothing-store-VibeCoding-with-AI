<script setup>
import { ref, watch, computed } from 'vue'
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
const isSearching = ref(false)
const searchResults = ref([])

// Danh sách sản phẩm mẫu hỗ trợ Live Search gợi ý trực tiếp
const sampleProducts = [
  { id: 1, name: 'Áo Khoác Biker Piqué', price: 1550000, image: '/img/product/product-1.jpg' },
  { id: 2, name: 'Áo Thun Basic Cotton Unisex', price: 350000, image: '/img/product/product-2.jpg' },
  { id: 3, name: 'Áo Sơ Mi Nam Tay Dài Oxford', price: 620000, image: '/img/product/product-3.jpg' },
  { id: 4, name: 'Quần Jean Nam Skinny Stretch', price: 890000, image: '/img/product/product-4.jpg' },
  { id: 5, name: 'Áo Polo Nam Thêu Logo', price: 490000, image: '/img/product/product-5.jpg' },
  { id: 6, name: 'Áo Khoác Blazer Nam Form Rộng', price: 2100000, image: '/img/product/product-6.jpg' },
  { id: 7, name: 'Quần Short Nam Thể Thao', price: 280000, image: '/img/product/product-7.jpg' },
  { id: 8, name: 'Áo Hoodies Fleece Nỉ Bông', price: 750000, image: '/img/product/product-8.jpg' },
]

let debounceTimer = null

watch(keyword, (newVal) => {
  clearTimeout(debounceTimer)
  if (!newVal.trim()) {
    searchResults.value = []
    isSearching.value = false
    return
  }

  isSearching.value = true
  debounceTimer = setTimeout(() => {
    const q = newVal.trim().toLowerCase()
    searchResults.value = sampleProducts.filter(p => p.name.toLowerCase().includes(q))
    isSearching.value = false
  }, 250)
})

const selectProduct = () => {
  router.push({ path: '/products', query: { keyword: keyword.value.trim() } })
  emit('close')
  keyword.value = ''
}

const handleSearch = () => {
  if (keyword.value.trim()) {
    selectProduct()
  }
}

const formatPrice = (val) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)
</script>

<template>
  <div class="search-model" :class="{ active: isOpen }">
    <div class="h-100 d-flex flex-column align-items-center justify-content-center position-relative">
      <div class="search-close-switch" @click="$emit('close')">+</div>

      <div class="search-box-wrapper">
        <form class="search-model-form" @submit.prevent="handleSearch">
          <input
            v-model="keyword"
            type="text"
            id="search-input"
            placeholder="Gõ tên sản phẩm để tìm kiếm....."
            autocomplete="off"
          >
        </form>

        <!-- Dropdown Gợi ý Kết quả AJAX Live Search -->
        <div v-if="keyword.trim()" class="live-search-results shadow-lg">
          <div v-if="isSearching" class="p-3 text-center text-muted">
            <span class="spinner-border spinner-border-sm mr-2"></span> Đang tìm kiếm...
          </div>

          <div v-else-if="searchResults.length > 0" class="results-list">
            <div
              v-for="item in searchResults"
              :key="item.id"
              class="result-item"
              @click="selectProduct"
            >
              <img :src="item.image" :alt="item.name" class="result-img" />
              <div class="result-info">
                <span class="result-name">{{ item.name }}</span>
                <span class="result-price">{{ formatPrice(item.price) }}</span>
              </div>
            </div>
          </div>

          <div v-else class="p-3 text-center text-muted">
            Không tìm thấy sản phẩm nào với từ khóa "{{ keyword }}".
          </div>
        </div>
      </div>
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
  background: rgba(0, 0, 0, 0.92);
  backdrop-filter: blur(8px);
  z-index: 99999;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.search-model.active {
  display: block;
  opacity: 1;
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

.search-box-wrapper {
  position: relative;
  width: 100%;
  max-width: 550px;
  padding: 0 20px;
}

.search-model-form input {
  width: 100%;
  font-size: 28px;
  border: none;
  border-bottom: 2px solid #e53637;
  background: transparent;
  color: #ffffff;
  padding-bottom: 12px;
  outline: none;
}

.search-model-form input::placeholder {
  color: #888888;
}

/* AJAX Live Search Dropdown */
.live-search-results {
  position: absolute;
  top: 100%;
  left: 20px;
  right: 20px;
  background: #ffffff;
  border-radius: 12px;
  margin-top: 12px;
  max-height: 360px;
  overflow-y: auto;
  z-index: 10;
}

.results-list {
  display: flex;
  flex-direction: column;
}

.result-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 16px;
  border-bottom: 1px solid #f1f5f9;
  cursor: pointer;
  transition: background 0.2s;
}

.result-item:hover {
  background: #f8fafc;
}

.result-img {
  width: 44px;
  height: 44px;
  object-fit: cover;
  border-radius: 6px;
}

.result-info {
  display: flex;
  flex-direction: column;
}

.result-name {
  font-size: 0.9375rem;
  font-weight: 700;
  color: #111827;
}

.result-price {
  font-size: 0.85rem;
  color: #e53637;
  font-weight: 700;
}
</style>
