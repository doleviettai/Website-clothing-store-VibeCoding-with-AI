<script setup>
import { ref } from 'vue'

const favorites = ref([
  {
    id: 1,
    name: 'Áo Khoác Biker Piqué',
    price: 1550000,
    image: '/img/product/product-1.jpg',
    inStock: true,
  },
  {
    id: 5,
    name: 'Áo Polo Nam Thêu Logo',
    price: 490000,
    image: '/img/product/product-5.jpg',
    inStock: true,
  },
])

const removeFavorite = (id) => {
  favorites.value = favorites.value.filter(f => f.id !== id)
}

const formatPrice = (val) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)
</script>

<template>
  <div class="favorite-page">
    <section class="breadcrumb-option">
      <div class="container">
        <div class="row">
          <div class="col-lg-12">
            <div class="breadcrumb__text">
              <h4>Danh sách yêu thích</h4>
              <div class="breadcrumb__links">
                <RouterLink to="/">Trang chủ</RouterLink>
                <span>Yêu thích</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="shopping-cart spad">
      <div class="container">
        <div v-if="favorites.length > 0" class="row">
          <div
            v-for="product in favorites"
            :key="product.id"
            class="col-lg-3 col-md-6 col-sm-6"
          >
            <div class="product__item">
              <div class="product__item__pic set-bg" :style="{ backgroundImage: `url(${product.image})` }">
                <ul class="product__hover">
                  <li><a href="#" @click.prevent="removeFavorite(product.id)" title="Bỏ yêu thích"><i class="fa fa-trash text-danger"></i></a></li>
                </ul>
              </div>
              <div class="product__item__text">
                <h6>{{ product.name }}</h6>
                <RouterLink to="/cart" class="add-cart">+ Thêm vào giỏ</RouterLink>
                <h5>{{ formatPrice(product.price) }}</h5>
              </div>
            </div>
          </div>
        </div>

        <div v-else class="text-center py-5">
          <img src="/img/icon/heart.png" alt="Empty Wishlist" style="width: 64px; opacity: 0.5;" class="mb-3">
          <h4>Chưa có sản phẩm yêu thích nào</h4>
          <p class="text-muted mb-4">Hãy nhấn biểu tượng trái tim ở sản phẩm bạn thích để lưu lại đây.</p>
          <RouterLink to="/products" class="primary-btn">Khám phá cửa hàng</RouterLink>
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

.product__item__text .add-cart {
  font-size: 15px;
  color: #e53637;
  font-weight: 700;
  position: absolute;
  left: 0;
  top: 22px;
  opacity: 0;
  visibility: hidden;
  transition: all, 0.3s;
}

.product__item:hover .product__item__text .add-cart {
  opacity: 1;
  visibility: visible;
}
</style>
