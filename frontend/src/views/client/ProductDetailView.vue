<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import * as productApi from '@/api/productApi'
import * as favoriteApi from '@/api/favoriteApi'
import * as cartApi from '@/api/cartApi'
import * as reviewApi from '@/api/reviewApi'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const productId = computed(() => route.params.id)

// State sản phẩm
const product = ref(null)
const isLoadingProduct = ref(true)
const productError = ref('')

// State tương tác mua hàng
const quantity = ref(1)
const selectedSize = ref('')
const selectedColor = ref('')
const isAddingToCart = ref(false)

// State Yêu Thích (Favorite)
const isFavorite = ref(false)
const isTogglingFavorite = ref(false)

// State Reviews (Bình luận & Đánh giá)
const reviews = ref([])
const isLoadingReviews = ref(false)
const userRating = ref(5)
const userComment = ref('')
const isSubmittingReview = ref(false)
const reviewMessage = ref('')
const reviewError = ref('')

// Alert Toast Message
const alertMessage = ref('')
const alertType = ref('success')

const showAlert = (msg, type = 'success') => {
  alertMessage.value = msg
  alertType.value = type
  setTimeout(() => {
    alertMessage.value = ''
  }, 3500)
}

// ─── Danh Sách Kích Cỡ & Màu Sắc ──────────────────────────────────
const availableSizes = computed(() => {
  if (!product.value?.availableSizes) return ['S', 'M', 'L', 'XL', 'XXL']
  return product.value.availableSizes.split(',').map(s => s.trim()).filter(Boolean)
})

const availableColors = computed(() => {
  if (!product.value?.availableColors) return ['Đen', 'Trắng', 'Xanh Navy', 'Xám']
  return product.value.availableColors.split(',').map(c => c.trim()).filter(Boolean)
})

// Tải Chi Tiết Sản Phẩm
const fetchProductDetail = async () => {
  isLoadingProduct.value = true
  productError.value = ''
  try {
    const res = await productApi.getProductById(productId.value)
    product.value = res.data.data

    // Chọn mặc định kích cỡ và màu sắc đầu tiên
    if (availableSizes.value.length > 0) selectedSize.value = availableSizes.value[0]
    if (availableColors.value.length > 0) selectedColor.value = availableColors.value[0]

    // Sau khi nạp sản phẩm ➔ nạp reviews và kiểm tra favorite
    fetchReviews()
    checkFavoriteStatus()
  } catch (err) {
    productError.value = err.response?.data?.message || 'Không thể tải thông tin chi tiết sản phẩm.'
  } finally {
    isLoadingProduct.value = false
  }
}

// Kiểm tra Trạng thái Favorite của User
const checkFavoriteStatus = async () => {
  if (!authStore.isAuthenticated) return
  try {
    const res = await favoriteApi.checkFavorite(productId.value)
    isFavorite.value = res.data?.data?.favorite || false
  } catch {
    //
  }
}

// Toggle Thêm / Xóa Sản phẩm Yêu thích (♡ Add to Favorite / ♥ Remove from Favorite)
const handleToggleFavorite = async () => {
  if (!authStore.isAuthenticated) {
    showAlert('Vui lòng đăng nhập để lưu sản phẩm yêu thích.', 'warning')
    router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }

  isTogglingFavorite.value = true
  try {
    if (isFavorite.value) {
      await favoriteApi.removeFavorite(productId.value)
      isFavorite.value = false
      if (product.value && product.value.favoriteCount > 0) {
        product.value.favoriteCount -= 1
      }
      showAlert('Đã xóa khỏi sản phẩm yêu thích của bạn!', 'info')
    } else {
      await favoriteApi.addFavorite(productId.value)
      isFavorite.value = true
      if (product.value) {
        product.value.favoriteCount += 1
      }
      showAlert('♥ Đã thêm vào sản phẩm yêu thích!', 'success')
    }
  } catch (err) {
    showAlert(err.response?.data?.message || 'Có lỗi xảy ra khi thao tác yêu thích.', 'danger')
  } finally {
    isTogglingFavorite.value = false
  }
}

// Tải Danh Sách Reviews (Đánh giá)
const fetchReviews = async () => {
  isLoadingReviews.value = true
  try {
    const res = await reviewApi.getProductReviews(productId.value)
    reviews.value = res.data?.data || []
  } catch {
    //
  } finally {
    isLoadingReviews.value = false
  }
}

// Gửi Đánh Giá Mới (1 - 5 Sao)
const handleSubmitReview = async () => {
  if (!authStore.isAuthenticated) {
    showAlert('Vui lòng đăng nhập trước khi gửi đánh giá.', 'warning')
    router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }

  if (!userComment.value.trim()) {
    reviewError.value = 'Vui lòng nhập nội dung đánh giá nhận xét.'
    return
  }

  isSubmittingReview.value = true
  reviewError.value = ''
  reviewMessage.value = ''

  try {
    await reviewApi.createReview(productId.value, {
      rating: userRating.value,
      comment: userComment.value.trim(),
    })
    reviewMessage.value = 'Cảm ơn bạn đã gửi đánh giá sản phẩm!'
    userComment.value = ''
    userRating.value = 5

    // Tải lại chi tiết sản phẩm và danh sách reviews
    fetchReviews()
    const res = await productApi.getProductById(productId.value)
    product.value = res.data.data

    setTimeout(() => {
      reviewMessage.value = ''
    }, 4000)
  } catch (err) {
    reviewError.value = err.response?.data?.message || 'Không thể gửi đánh giá.'
  } finally {
    isSubmittingReview.value = false
  }
}

// Khách hàng xóa review của mình
const handleDeleteReview = async (reviewId) => {
  if (!confirm('Bạn có chắc chắn muốn xóa bài đánh giá này?')) return
  try {
    await reviewApi.deleteUserReview(reviewId)
    showAlert('Đã xóa bài đánh giá thành công!', 'info')
    fetchReviews()
    const res = await productApi.getProductById(productId.value)
    product.value = res.data.data
  } catch (err) {
    showAlert(err.response?.data?.message || 'Không thể xóa bài đánh giá.', 'danger')
  }
}

// Xử lý Thêm vào giỏ hàng (Add To Cart)
const handleAddToCart = async () => {
  if (!authStore.isAuthenticated) {
    showAlert('Vui lòng đăng nhập trước khi mua hàng.', 'warning')
    router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }

  if (!selectedSize.value) {
    showAlert('Vui lòng chọn Kích cỡ (Size) sản phẩm.', 'warning')
    return
  }
  if (!selectedColor.value) {
    showAlert('Vui lòng chọn Màu sắc (Color) sản phẩm.', 'warning')
    return
  }
  if (product.value.stockQuantity < quantity.value) {
    showAlert('Số lượng tồn kho không đủ để đáp ứng.', 'danger')
    return
  }

  isAddingToCart.value = true
  try {
    await cartApi.addToCart({
      productId: product.value.id,
      quantity: quantity.value,
      size: selectedSize.value,
      color: selectedColor.value,
    })
    showAlert(`🛒 Đã thêm ${quantity.value} x ${product.value.name} (${selectedSize.value} - ${selectedColor.value}) vào giỏ hàng!`, 'success')
  } catch (err) {
    showAlert(err.response?.data?.message || 'Có lỗi xảy ra khi thêm vào giỏ hàng.', 'danger')
  } finally {
    isAddingToCart.value = false
  }
}

// Xử lý Mua Ngay (Buy Now ➔ Chuyển thẳng Checkout)
const handleBuyNow = async () => {
  if (!authStore.isAuthenticated) {
    showAlert('Vui lòng đăng nhập trước khi thanh toán mua ngay.', 'warning')
    router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }

  if (!selectedSize.value) {
    showAlert('Vui lòng chọn Kích cỡ (Size) sản phẩm.', 'warning')
    return
  }
  if (!selectedColor.value) {
    showAlert('Vui lòng chọn Màu sắc (Color) sản phẩm.', 'warning')
    return
  }
  if (product.value.stockQuantity < quantity.value) {
    showAlert('Sản phẩm đã hết hàng hoặc số lượng tồn kho không đủ.', 'danger')
    return
  }

  try {
    await cartApi.addToCart({
      productId: product.value.id,
      quantity: quantity.value,
      size: selectedSize.value,
      color: selectedColor.value,
    })
    router.push('/checkout')
  } catch (err) {
    showAlert(err.response?.data?.message || 'Không thể thực hiện Mua ngay.', 'danger')
  }
}

const decreaseQuantity = () => {
  if (quantity.value > 1) quantity.value--
}
const increaseQuantity = () => {
  if (product.value && quantity.value < product.value.stockQuantity) {
    quantity.value++
  }
}

const formatPrice = (val) => {
  if (val === null || val === undefined) return '0 đ'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)
}

watch(() => route.params.id, () => {
  if (route.params.id) {
    fetchProductDetail()
  }
})

onMounted(() => {
  fetchProductDetail()
})
</script>

<template>
  <div class="product-detail-page py-4">
    <!-- Floating Alert Toast Notification -->
    <div v-if="alertMessage" class="position-fixed top-0 end-0 p-3" style="z-index: 1080;">
      <div class="alert shadow-lg border-0 fade show" :class="`alert-${alertType}`" role="alert">
        <i class="fa fa-info-circle mr-2"></i> {{ alertMessage }}
      </div>
    </div>

    <!-- Breadcrumb -->
    <div class="bg-light py-3 mb-4 border-bottom">
      <div class="container">
        <nav aria-label="breadcrumb">
          <ol class="breadcrumb m-0 bg-transparent p-0">
            <li class="breadcrumb-item"><RouterLink to="/" class="text-muted">Trang chủ</RouterLink></li>
            <li class="breadcrumb-item"><RouterLink to="/products" class="text-muted">Shop</RouterLink></li>
            <li class="breadcrumb-item active text-dark font-weight-bold" aria-current="page">
              {{ product?.name || 'Chi Tiết Sản Phẩm' }}
            </li>
          </ol>
        </nav>
      </div>
    </div>

    <!-- Container Chính Chi Tiết Sản Phẩm -->
    <div class="container">
      <div v-if="isLoadingProduct" class="py-5 text-center">
        <LoadingSpinner text="Đang nạp chi tiết sản phẩm..." />
      </div>

      <div v-else-if="productError" class="alert alert-danger my-5 text-center p-4">
        <i class="fa fa-exclamation-triangle fa-2x mb-2 d-block"></i>
        <h5 class="font-weight-bold">Không tìm thấy sản phẩm</h5>
        <p>{{ productError }}</p>
        <RouterLink to="/products" class="btn btn-outline-danger btn-sm">Về Trang Cửa Hàng</RouterLink>
      </div>

      <div v-else-if="product" class="row">
        <!-- Cột Ảnh Sản Phẩm (Product Gallery) -->
        <div class="col-lg-6 mb-4">
          <div class="product-gallery-box border rounded shadow-sm overflow-hidden bg-white p-3">
            <div class="main-image-wrapper text-center">
              <img
                :src="product.thumbnailUrl || '/img/product/product-1.jpg'"
                :alt="product.name"
                class="img-fluid rounded main-img"
              />
            </div>
          </div>
        </div>

        <!-- Cột Thông Tin Sản Phẩm & Nút Thao Tác (Product Info & Action) -->
        <div class="col-lg-6 mb-4">
          <div class="product-info-box bg-white p-4 border rounded shadow-sm">
            <div class="d-flex align-items-center justify-content-between mb-2">
              <span class="badge bg-danger text-white text-uppercase font-weight-bold px-3 py-2">
                {{ product.brandName || 'Male Fashion' }}
              </span>
              <span class="badge bg-light text-dark border px-3 py-2">
                {{ product.categoryName || 'Thời Trang' }}
              </span>
            </div>

            <h2 class="font-weight-bold text-dark mb-2 h3">{{ product.name }}</h2>

            <!-- Đánh Giá Stars & Rating Summary -->
            <div class="d-flex align-items-center gap-2 mb-3">
              <div class="rating text-warning font-weight-bold">
                <i v-for="star in 5" :key="star" class="fa" :class="star <= Math.round(product.averageRating || 5) ? 'fa-star' : 'fa-star-o'"></i>
              </div>
              <span class="font-weight-bold text-dark">({{ (product.averageRating || 5.0).toFixed(1) }})</span>
              <span class="text-muted">|</span>
              <span class="text-muted small">Based on <strong>{{ product.reviewCount || reviews.length || 0 }}</strong> reviews</span>
              <span class="text-muted">|</span>
              <span class="text-danger small font-weight-bold"><i class="fa fa-heart"></i> {{ product.favoriteCount || 0 }} Yêu thích</span>
            </div>

            <!-- Giá Bán & Khuyến Mãi -->
            <div class="d-flex align-items-center gap-3 mb-4 p-3 bg-light rounded">
              <h3 class="font-weight-bold text-danger m-0 h2">
                {{ formatPrice(product.salePrice || product.price) }}
              </h3>
              <span v-if="product.salePrice" class="text-muted text-decoration-line-through h5 m-0">
                {{ formatPrice(product.price) }}
              </span>
              <span v-if="product.salePrice" class="badge bg-danger text-white ms-auto font-weight-bold">
                TIẾT KIỆM {{ formatPrice(product.price - product.salePrice) }}
              </span>
            </div>

            <!-- Mô Tả Ngắn -->
            <p class="text-muted small mb-4">
              {{ product.shortDescription || 'Sản phẩm thời trang cao cấp với chất liệu thoáng mát, kiểu dáng hiện đại phù hợp cho mọi hoạt động hàng ngày.' }}
            </p>

            <!-- Lựa Chọn Kích Cỡ (Size: S, M, L, XL, XXL) -->
            <div class="option-group mb-4">
              <label class="form-label font-weight-bold text-dark d-block mb-2">
                <i class="fa fa-ruler-combined text-primary mr-1"></i> Chọn Kích Cỡ (Size):
              </label>
              <div class="d-flex flex-wrap gap-2">
                <button
                  v-for="size in availableSizes"
                  :key="size"
                  class="btn btn-option-size"
                  :class="{ active: selectedSize === size }"
                  @click="selectedSize = size"
                >
                  {{ size }}
                </button>
              </div>
            </div>

            <!-- Lựa Chọn Màu Sắc (Color: Đen, Trắng, Xanh Navy, Xám) -->
            <div class="option-group mb-4">
              <label class="form-label font-weight-bold text-dark d-block mb-2">
                <i class="fa fa-paint-brush text-warning mr-1"></i> Chọn Màu Sắc (Color):
              </label>
              <div class="d-flex flex-wrap gap-2">
                <button
                  v-for="color in availableColors"
                  :key="color"
                  class="btn btn-option-color"
                  :class="{ active: selectedColor === color }"
                  @click="selectedColor = color"
                >
                  {{ color }}
                </button>
              </div>
            </div>

            <!-- Số Lượng & Tồn Kho -->
            <div class="option-group mb-4">
              <label class="form-label font-weight-bold text-dark d-block mb-2">
                Số Lượng Mua & Tồn Kho:
              </label>
              <div class="d-flex align-items-center gap-3">
                <div class="quantity-control input-group" style="max-width: 140px;">
                  <button class="btn btn-outline-secondary" type="button" @click="decreaseQuantity">-</button>
                  <input type="number" class="form-control text-center font-weight-bold" v-model.number="quantity" min="1" :max="product.stockQuantity" readonly />
                  <button class="btn btn-outline-secondary" type="button" @click="increaseQuantity">+</button>
                </div>
                <span class="badge" :class="product.stockQuantity > 0 ? 'bg-success text-white' : 'bg-secondary text-white'">
                  {{ product.stockQuantity > 0 ? `Còn hàng (${product.stockQuantity} sản phẩm)` : 'Hết hàng' }}
                </span>
              </div>
            </div>

            <!-- Các Nút Thao Tác Mua Hàng & Yêu Thích -->
            <div class="action-buttons d-flex flex-wrap gap-2 pt-3 border-top">
              <button
                class="btn btn-danger btn-lg flex-grow-1 font-weight-bold text-uppercase py-3"
                :disabled="product.stockQuantity <= 0 || isAddingToCart"
                @click="handleBuyNow"
              >
                <i class="fa fa-flash mr-2"></i> MUA NGAY (BUY NOW)
              </button>
              <button
                class="btn btn-dark btn-lg flex-grow-1 font-weight-bold text-uppercase py-3"
                :disabled="product.stockQuantity <= 0 || isAddingToCart"
                @click="handleAddToCart"
              >
                <i class="fa fa-shopping-cart mr-2"></i> {{ isAddingToCart ? 'ĐANG THÊM...' : 'THÊM VÀO GIỎ' }}
              </button>
              <button
                class="btn btn-outline-danger btn-lg font-weight-bold py-3"
                :disabled="isTogglingFavorite"
                @click="handleToggleFavorite"
                title="Sản phẩm yêu thích"
              >
                <i class="fa" :class="isFavorite ? 'fa-heart text-danger' : 'fa-heart-o'"></i>
                {{ isFavorite ? '♥ ĐÃ YÊU THÍCH' : '♡ THÊM YÊU THÍCH' }}
              </button>
            </div>
          </div>
        </div>

        <!-- Section Mô Tả Chi Tiết Sản Phẩm -->
        <div class="col-lg-12 my-4">
          <div class="bg-white p-4 border rounded shadow-sm">
            <h4 class="font-weight-bold text-dark mb-3 border-bottom pb-2">
              <i class="fa fa-file-text-o text-danger mr-2"></i> Mô Tả Chi Tiết Sản Phẩm
            </h4>
            <div class="product-description-content text-secondary" style="line-height: 1.8;">
              <p v-if="product.description" style="white-space: pre-line;">{{ product.description }}</p>
              <div v-else>
                <p>Thương hiệu thời trang nam Male Fashion cam kết cung cấp các sản phẩm chất lượng vượt trội.</p>
                <ul>
                  <li>Chất liệu thoáng mát, thấm hút mồ hôi hiệu quả.</li>
                  <li>Đường may tỉ mỉ, chắc chắn, chuẩn form dáng unisex hiện đại.</li>
                  <li>Dễ dàng phối đồ cùng quần jeans, quần short hoặc áo khoác phong cách.</li>
                </ul>
              </div>
            </div>
          </div>
        </div>

        <!-- Section Đánh Giá & Bình Luận (Customer Reviews & Star Rating 1-5) -->
        <div class="col-lg-12 mb-5">
          <div class="bg-white p-4 border rounded shadow-sm">
            <h4 class="font-weight-bold text-dark mb-4 border-bottom pb-2 d-flex align-items-center justify-content-between">
              <span><i class="fa fa-comments text-danger mr-2"></i> ĐÁNH GIÁ TỪ KHÁCH HÀNG (CUSTOMER REVIEWS)</span>
              <span class="badge bg-warning text-dark font-weight-bold px-3 py-2 fs-6">
                <i class="fa fa-star text-white"></i> {{ (product.averageRating || 5.0).toFixed(1) }} / 5.0
              </span>
            </h4>

            <!-- Form Đăng Bài Đánh Giá Review Mới -->
            <div class="card bg-light border-0 mb-4 shadow-sm">
              <div class="card-body p-4">
                <h6 class="font-weight-bold text-dark mb-3">Gửi Nhận Xét & Đánh Giá Của Bạn</h6>

                <div v-if="reviewMessage" class="alert alert-success py-2 mb-3">
                  <i class="fa fa-check-circle mr-1"></i> {{ reviewMessage }}
                </div>
                <div v-if="reviewError" class="alert alert-danger py-2 mb-3">
                  <i class="fa fa-exclamation-triangle mr-1"></i> {{ reviewError }}
                </div>

                <div class="mb-3">
                  <label class="form-label font-weight-bold mr-2">Bạn đánh giá sản phẩm mấy sao?</label>
                  <div class="star-rating-input d-inline-flex gap-2 text-warning cursor-pointer fs-4">
                    <i
                      v-for="star in 5"
                      :key="star"
                      class="fa"
                      :class="star <= userRating ? 'fa-star' : 'fa-star-o'"
                      @click="userRating = star"
                    ></i>
                  </div>
                </div>

                <div class="mb-3">
                  <textarea
                    v-model="userComment"
                    class="form-control"
                    rows="3"
                    placeholder="Viết cảm nhận của bạn về sản phẩm này (chất liệu, form dáng, tốc độ giao hàng)..."
                  ></textarea>
                </div>

                <button
                  class="btn btn-danger font-weight-bold px-4"
                  :disabled="isSubmittingReview"
                  @click="handleSubmitReview"
                >
                  <i class="fa fa-paper-plane mr-1"></i> {{ isSubmittingReview ? 'Đang Gửi...' : 'Gửi Đánh Giá' }}
                </button>
              </div>
            </div>

            <!-- Danh Sách Review Khách Hàng -->
            <div v-if="isLoadingReviews" class="py-4 text-center">
              <LoadingSpinner text="Đang tải nhận xét..." />
            </div>

            <div v-else-if="reviews.length === 0" class="py-4 text-center text-muted">
              <i class="fa fa-comment-o fa-2x mb-2 d-block"></i>
              Chưa có bài đánh giá nào cho sản phẩm này. Hãy là người đầu tiên để lại nhận xét!
            </div>

            <div v-else class="review-list d-flex flex-column gap-3">
              <div v-for="rev in reviews" :key="rev.id" class="review-item p-3 border rounded bg-light">
                <div class="d-flex align-items-center justify-content-between mb-2">
                  <div class="d-flex align-items-center gap-2">
                    <img
                      :src="rev.userAvatarUrl || '/img/icon/heart.png'"
                      class="rounded-circle border"
                      style="width: 36px; height: 36px; object-fit: cover;"
                    />
                    <div>
                      <h6 class="font-weight-bold text-dark m-0">{{ rev.userFullName || 'Khách hàng' }}</h6>
                      <small class="text-muted">{{ new Date(rev.createdAt).toLocaleDateString('vi-VN') }}</small>
                    </div>
                  </div>
                  <div class="text-warning">
                    <i v-for="s in 5" :key="s" class="fa" :class="s <= rev.rating ? 'fa-star' : 'fa-star-o'"></i>
                  </div>
                </div>
                <p class="text-dark m-0 small pl-5" style="line-height: 1.6;">{{ rev.content }}</p>

                <!-- Nút xóa review nếu là chính mình -->
                <div v-if="authStore.user && rev.userId === authStore.user.id" class="text-end mt-2">
                  <button class="btn btn-xs btn-outline-danger" @click="handleDeleteReview(rev.id)">
                    <i class="fa fa-trash mr-1"></i> Xóa bài viết
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.product-gallery-box {
  min-height: 420px;
}
.main-img {
  max-height: 400px;
  object-fit: contain;
}

/* Option Buttons Size & Color */
.btn-option-size {
  border: 1px solid #dee2e6;
  background: #f8f9fa;
  color: #111111;
  font-weight: 600;
  padding: 6px 16px;
  border-radius: 6px;
  transition: all 0.2s;
}
.btn-option-size.active,
.btn-option-size:hover {
  background: #111111;
  color: #ffffff;
  border-color: #111111;
}

.btn-option-color {
  border: 1px solid #dee2e6;
  background: #ffffff;
  color: #333;
  font-weight: 600;
  padding: 6px 16px;
  border-radius: 6px;
  transition: all 0.2s;
}
.btn-option-color.active,
.btn-option-color:hover {
  background: #e53637;
  color: #ffffff;
  border-color: #e53637;
}

.cursor-pointer {
  cursor: pointer;
}
.btn-xs {
  font-size: 0.75rem;
  padding: 2px 8px;
}
</style>
