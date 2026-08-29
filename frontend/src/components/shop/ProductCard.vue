<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  product: {
    type: Object,
    required: true,
  },
})

// Tách danh sách kích cỡ & màu sắc
const sizes = computed(() => {
  const raw = props.product.availableSizes || 'S, M, L, XL, XXL'
  return raw.split(',').map(s => s.trim()).filter(Boolean)
})

const colors = computed(() => {
  const raw = props.product.availableColors || 'Đen, Trắng, Xanh Navy, Xám'
  return raw.split(',').map(c => c.trim()).filter(Boolean)
})

const selectedSize = ref(sizes.value[0] || 'M')
const selectedColor = ref(colors.value[0] || 'Đen')

const formatPrice = (value) => {
  if (!value) return '0 đ'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value)
}
</script>

<template>
  <div class="product__item h-100 border-0 shadow-sm rounded overflow-hidden bg-white d-flex flex-column">
    <div
      class="product__item__pic set-bg position-relative"
      :style="{ backgroundImage: `url(${product.thumbnailUrl || product.thumbnail || product.image || '/img/product/product-1.jpg'})` }"
    >
      <span v-if="product.salePrice" class="label bg-danger text-white">
        GIẢM GIÁ
      </span>
      <span v-else-if="product.isFeatured" class="label bg-dark text-white">
        HOT
      </span>
      <ul class="product__hover">
        <li><a href="#" title="Thêm vào yêu thích"><img src="/img/icon/heart.png" alt="Yêu thích" /></a></li>
        <li><RouterLink :to="`/products/${product.id}`" title="Xem chi tiết"><img src="/img/icon/search.png" alt="Chi tiết" /></RouterLink></li>
      </ul>
    </div>

    <div class="product__item__text p-3 d-flex flex-column flex-grow-1">
      <div class="d-flex align-items-center justify-content-between mb-1">
        <small class="text-muted text-uppercase font-weight-bold">{{ product.brandName || 'Male Fashion' }}</small>
        <small class="badge bg-light text-dark border">{{ product.categoryName || 'Thời Trang' }}</small>
      </div>

      <h6 class="font-weight-bold text-dark mb-2 text-truncate" :title="product.name">
        {{ product.name }}
      </h6>

      <!-- Kích Cỡ Pill Choices -->
      <div class="size-selection-box mb-2">
        <span class="text-muted extra-small d-block mb-1 font-weight-bold">Kích cỡ:</span>
        <div class="d-flex flex-wrap gap-1">
          <span
            v-for="sz in sizes"
            :key="sz"
            class="size-pill"
            :class="{ active: selectedSize === sz }"
            @click="selectedSize = sz"
          >
            {{ sz }}
          </span>
        </div>
      </div>

      <!-- Màu Sắc Choice Badges -->
      <div class="color-selection-box mb-3">
        <span class="text-muted extra-small d-block mb-1 font-weight-bold">Màu sắc:</span>
        <div class="d-flex flex-wrap gap-1">
          <span
            v-for="cl in colors"
            :key="cl"
            class="color-pill"
            :class="{ active: selectedColor === cl }"
            @click="selectedColor = cl"
          >
            {{ cl }}
          </span>
        </div>
      </div>

      <div class="mt-auto pt-2 border-top">
        <div class="d-flex align-items-center justify-content-between mb-2">
          <div class="d-flex align-items-center gap-2">
            <h5 class="font-weight-bold text-danger m-0">
              {{ formatPrice(product.salePrice || product.price) }}
            </h5>
            <small v-if="product.salePrice" class="text-muted text-decoration-line-through">
              {{ formatPrice(product.price) }}
            </small>
          </div>
          <div class="rating text-warning small">
            <i class="fa fa-star"></i>
            <span class="text-dark ml-1 font-weight-bold">{{ (product.averageRating || 5.0).toFixed(1) }}</span>
          </div>
        </div>

        <!-- <RouterLink
          to="/cart"
          class="btn btn-outline-danger btn-sm w-100 font-weight-bold text-uppercase rounded"
        >
          + Thêm {{ selectedSize }} ({{ selectedColor }}) Vào Giỏ
        </RouterLink> -->
      </div>
    </div>
  </div>
</template>

<style scoped>
.product__item {
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}
.product__item:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1) !important;
}

.product__item:hover .product__item__text h6,
.product__item__text h6 {
  opacity: 1 !important;
  visibility: visible !important;
}

.extra-small {
  font-size: 0.75rem;
}

.size-pill {
  font-size: 0.75rem;
  padding: 2px 7px;
  border-radius: 4px;
  border: 1px solid #dee2e6;
  background: #f8f9fa;
  color: #333;
  cursor: pointer;
  transition: all 0.2s ease;
}

.size-pill.active,
.size-pill:hover {
  background: #111111;
  color: #ffffff;
  border-color: #111111;
  font-weight: bold;
}

.color-pill {
  font-size: 0.72rem;
  padding: 2px 6px;
  border-radius: 4px;
  border: 1px solid #e2e8f0;
  background: #ffffff;
  color: #4a5568;
  cursor: pointer;
  transition: all 0.2s ease;
}

.color-pill.active,
.color-pill:hover {
  background: #e53637;
  color: #ffffff;
  border-color: #e53637;
  font-weight: bold;
}
</style>
