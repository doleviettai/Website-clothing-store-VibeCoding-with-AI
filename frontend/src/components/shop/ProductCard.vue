<script setup>
defineProps({
  product: {
    type: Object,
    required: true,
  },
})

const formatPrice = (value) => {
  if (!value) return '0 đ'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value)
}
</script>

<template>
  <div class="product__item h-100 border-0 shadow-sm rounded overflow-hidden bg-white">
    <div
      class="product__item__pic set-bg position-relative"
      :style="{ backgroundImage: `url(${product.thumbnailUrl || product.image || '/img/product/product-1.jpg'})` }"
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
    <div class="product__item__text p-3">
      <div class="d-flex align-items-center justify-content-between mb-1">
        <small class="text-muted text-uppercase font-weight-bold">{{ product.brandName || 'Male Fashion' }}</small>
        <small class="badge bg-light text-dark border">{{ product.categoryName || 'Thời Trang' }}</small>
      </div>
      <h6 class="font-weight-bold text-dark mb-2 text-truncate" :title="product.name">
        {{ product.name }}
      </h6>
      <RouterLink to="/cart" class="add-cart text-danger font-weight-bold d-block mb-2">+ Thêm vào giỏ</RouterLink>
      <div class="rating text-warning small mb-2">
        <i
          v-for="star in 5"
          :key="star"
          class="fa"
          :class="star <= Math.round(product.averageRating || 5) ? 'fa-star' : 'fa-star-o'"
        ></i>
        <span class="text-muted ml-1 font-weight-normal">({{ (product.averageRating || 5.0).toFixed(1) }})</span>
      </div>
      <div class="d-flex align-items-center gap-2">
        <h5 class="font-weight-bold text-danger m-0">
          {{ formatPrice(product.salePrice || product.price) }}
        </h5>
        <small v-if="product.salePrice" class="text-muted text-decoration-line-through">
          {{ formatPrice(product.price) }}
        </small>
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
</style>
