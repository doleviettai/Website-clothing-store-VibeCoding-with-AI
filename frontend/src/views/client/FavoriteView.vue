<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import * as favoriteApi from '@/api/favoriteApi'
import * as cartApi from '@/api/cartApi'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'

const router = useRouter()
const favorites = ref([])
const isLoading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

const fetchFavorites = async () => {
  isLoading.value = true
  errorMessage.value = ''
  try {
    const res = await favoriteApi.getUserFavorites()
    favorites.value = res.data?.data || []
  } catch (err) {
    errorMessage.value = err.response?.data?.message || 'Không thể tải danh sách sản phẩm yêu thích.'
  } finally {
    isLoading.value = false
  }
}

const handleRemove = async (productId) => {
  try {
    await favoriteApi.removeFavorite(productId)
    favorites.value = favorites.value.filter(f => f.productId !== productId)
    successMessage.value = 'Đã xóa sản phẩm khỏi danh sách yêu thích!'
    setTimeout(() => {
      successMessage.value = ''
    }, 3000)
  } catch (err) {
    errorMessage.value = err.response?.data?.message || 'Không thể xóa sản phẩm.'
  }
}

const handleAddToCart = async (fav) => {
  try {
    await cartApi.addToCart({
      productId: fav.productId,
      quantity: 1,
      size: 'M',
      color: 'Đen',
    })
    successMessage.value = `🛒 Đã thêm "${fav.productName}" vào giỏ hàng!`
    setTimeout(() => {
      successMessage.value = ''
    }, 3000)
  } catch (err) {
    errorMessage.value = err.response?.data?.message || 'Không thể thêm vào giỏ hàng.'
  }
}

const formatPrice = (val) => {
  if (!val) return '0 đ'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)
}

onMounted(() => {
  fetchFavorites()
})
</script>

<template>
  <div class="favorite-page py-4">
    <!-- Breadcrumb -->
    <div class="bg-light py-3 mb-4 border-bottom">
      <div class="container">
        <div class="d-flex align-items-center justify-content-between">
          <h4 class="font-weight-bold text-dark m-0">
            <i class="fa fa-heart text-danger mr-2"></i> SẢN PHẨM YÊU THÍCH (MY FAVORITES)
          </h4>
          <nav aria-label="breadcrumb">
            <ol class="breadcrumb m-0 bg-transparent p-0">
              <li class="breadcrumb-item"><RouterLink to="/" class="text-muted">Trang chủ</RouterLink></li>
              <li class="breadcrumb-item active text-dark font-weight-bold" aria-current="page">Favorites</li>
            </ol>
          </nav>
        </div>
      </div>
    </div>

    <!-- Content Container -->
    <div class="container">
      <div v-if="successMessage" class="alert alert-success alert-dismissible fade show" role="alert">
        <i class="fa fa-check-circle mr-2"></i> {{ successMessage }}
      </div>
      <div v-if="errorMessage" class="alert alert-danger alert-dismissible fade show" role="alert">
        <i class="fa fa-exclamation-triangle mr-2"></i> {{ errorMessage }}
      </div>

      <div v-if="isLoading" class="py-5 text-center">
        <LoadingSpinner text="Đang nạp danh sách sản phẩm yêu thích..." />
      </div>

      <div v-else-if="favorites.length === 0" class="py-5 text-center my-4 bg-white border rounded shadow-sm">
        <i class="fa fa-heart-o fa-4x text-muted mb-3 d-block"></i>
        <h5 class="font-weight-bold text-dark mb-2">Danh sách sản phẩm yêu thích rỗng</h5>
        <p class="text-muted small mb-4">Hãy khám phá bộ sưu tập sản phẩm và bấm biểu tượng trái tim để lưu lại item yêu thích!</p>
        <RouterLink to="/products" class="btn btn-danger font-weight-bold px-4 py-2">Khám Phá Cửa Hàng Ngay</RouterLink>
      </div>

      <!-- Bảng Danh Sách Yêu Thích -->
      <div v-else class="card border-0 shadow-sm rounded-lg overflow-hidden">
        <div class="card-body p-0">
          <div class="table-responsive">
            <table class="table table-hover align-middle m-0 text-center">
              <thead class="table-dark text-uppercase small">
                <tr>
                  <th style="width: 100px;">Hình Ảnh</th>
                  <th class="text-left">Sản Phẩm</th>
                  <th>Chuyên Mục / Thương Hiệu</th>
                  <th>Giá Bán</th>
                  <th>Đánh Giá</th>
                  <th style="width: 220px;">Thao Tác</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="fav in favorites" :key="fav.id">
                  <td>
                    <img
                      :src="fav.thumbnailUrl || '/img/product/product-1.jpg'"
                      :alt="fav.productName"
                      class="rounded border"
                      style="width: 60px; height: 60px; object-fit: cover;"
                    />
                  </td>
                  <td class="text-left">
                    <RouterLink :to="`/products/${fav.productId}`" class="font-weight-bold text-dark text-decoration-none h6 d-block mb-1">
                      {{ fav.productName }}
                    </RouterLink>
                    <small class="text-muted">ID: #{{ fav.productId }}</small>
                  </td>
                  <td>
                    <span class="badge bg-light text-dark border mr-1">{{ fav.categoryName || 'Thời Trang' }}</span>
                    <span class="badge bg-danger text-white">{{ fav.brandName || 'Male Fashion' }}</span>
                  </td>
                  <td>
                    <span class="font-weight-bold text-danger">{{ formatPrice(fav.salePrice || fav.price) }}</span>
                    <small v-if="fav.salePrice" class="text-muted text-decoration-line-through d-block">{{ formatPrice(fav.price) }}</small>
                  </td>
                  <td>
                    <span class="text-warning font-weight-bold">
                      <i class="fa fa-star"></i> {{ (fav.averageRating || 5.0).toFixed(1) }}
                    </span>
                  </td>
                  <td>
                    <div class="d-flex justify-content-center gap-2">
                      <button class="btn btn-sm btn-dark font-weight-bold" @click="handleAddToCart(fav)">
                        <i class="fa fa-cart-plus mr-1"></i> Thêm Giỏ
                      </button>
                      <button class="btn btn-sm btn-outline-danger" @click="handleRemove(fav.productId)" title="Xóa khỏi Yêu thích">
                        <i class="fa fa-trash"></i>
                      </button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
