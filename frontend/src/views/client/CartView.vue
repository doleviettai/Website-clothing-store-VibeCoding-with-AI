<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import * as cartApi from '@/api/cartApi'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'

const router = useRouter()
const cart = ref(null)
const isLoading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

const fetchCart = async () => {
  isLoading.value = true
  errorMessage.value = ''
  try {
    const res = await cartApi.getCart()
    cart.value = res.data?.data
  } catch (err) {
    errorMessage.value = err.response?.data?.message || 'Không thể nạp thông tin giỏ hàng.'
  } finally {
    isLoading.value = false
  }
}

const items = computed(() => cart.value?.items || [])
const subtotal = computed(() => cart.value?.subtotal || 0)
const totalItems = computed(() => cart.value?.totalItems || 0)

const updateQuantity = async (item, delta) => {
  const newQty = item.quantity + delta
  if (newQty < 1) return
  if (newQty > item.stockQuantity) {
    errorMessage.value = `Tồn kho chỉ còn ${item.stockQuantity} sản phẩm.`
    setTimeout(() => { errorMessage.value = '' }, 3000)
    return
  }

  try {
    const res = await cartApi.updateCartItemQuantity(item.id, newQty)
    cart.value = res.data?.data
  } catch (err) {
    errorMessage.value = err.response?.data?.message || 'Không thể cập nhật số lượng.'
  }
}

const removeItem = async (itemId) => {
  try {
    const res = await cartApi.removeCartItem(itemId)
    cart.value = res.data?.data
    successMessage.value = 'Đã xóa sản phẩm khỏi giỏ hàng!'
    setTimeout(() => { successMessage.value = '' }, 3000)
  } catch (err) {
    errorMessage.value = err.response?.data?.message || 'Không thể xóa sản phẩm.'
  }
}

const goToCheckout = () => {
  if (items.value.length === 0) return
  router.push('/checkout')
}

const formatPrice = (val) => {
  if (!val) return '0 đ'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)
}

onMounted(() => {
  fetchCart()
})
</script>

<template>
  <div class="cart-page py-4">
    <!-- Breadcrumb -->
    <div class="bg-light py-3 mb-4 border-bottom">
      <div class="container">
        <div class="d-flex align-items-center justify-content-between">
          <h4 class="font-weight-bold text-dark m-0">
            <i class="fa fa-shopping-cart text-danger mr-2"></i> GIỎ HÀNG CỦA BẠN (MY SHOPPING CART)
          </h4>
          <nav aria-label="breadcrumb">
            <ol class="breadcrumb m-0 bg-transparent p-0">
              <li class="breadcrumb-item"><RouterLink to="/" class="text-muted">Trang chủ</RouterLink></li>
              <li class="breadcrumb-item active text-dark font-weight-bold" aria-current="page">Cart</li>
            </ol>
          </nav>
        </div>
      </div>
    </div>

    <div class="container">
      <div v-if="successMessage" class="alert alert-success alert-dismissible fade show" role="alert">
        <i class="fa fa-check-circle mr-2"></i> {{ successMessage }}
      </div>
      <div v-if="errorMessage" class="alert alert-danger alert-dismissible fade show" role="alert">
        <i class="fa fa-exclamation-triangle mr-2"></i> {{ errorMessage }}
      </div>

      <div v-if="isLoading" class="py-5 text-center">
        <LoadingSpinner text="Đang tải dữ liệu giỏ hàng..." />
      </div>

      <div v-else-if="items.length === 0" class="py-5 text-center my-4 bg-white border rounded shadow-sm">
        <i class="fa fa-shopping-cart fa-4x text-muted mb-3 d-block"></i>
        <h5 class="font-weight-bold text-dark mb-2">Giỏ hàng của bạn đang rỗng</h5>
        <p class="text-muted small mb-4">Hãy chọn những mẫu quần áo ưng ý từ cửa hàng để thêm vào giỏ hàng nhé!</p>
        <RouterLink to="/products" class="btn btn-danger font-weight-bold px-5 py-2">Khám Phá Cửa Hàng Ngay</RouterLink>
      </div>

      <div v-else class="row">
        <!-- Cột Bảng Sản Phẩm Trong Giỏ Hàng -->
        <div class="col-lg-8 mb-4">
          <div class="card border-0 shadow-sm rounded-lg overflow-hidden">
            <div class="card-body p-0">
              <div class="table-responsive">
                <table class="table table-hover align-middle m-0 text-center">
                  <thead class="table-dark text-uppercase small">
                    <tr>
                      <th style="width: 80px;">Hình Ảnh</th>
                      <th class="text-left">Sản Phẩm</th>
                      <th>Kích Cỡ / Màu</th>
                      <th>Đơn Giá</th>
                      <th style="width: 130px;">Số Lượng</th>
                      <th>Thành Tiền</th>
                      <th style="width: 60px;">Xóa</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="item in items" :key="item.id">
                      <td>
                        <img
                          :src="item.thumbnailUrl || '/img/product/product-1.jpg'"
                          :alt="item.productName"
                          class="rounded border"
                          style="width: 54px; height: 54px; object-fit: cover;"
                        />
                      </td>
                      <td class="text-left">
                        <RouterLink :to="`/products/${item.productId}`" class="font-weight-bold text-dark text-decoration-none d-block mb-1">
                          {{ item.productName }}
                        </RouterLink>
                        <small class="text-muted">{{ item.brandName }} • {{ item.categoryName }}</small>
                      </td>
                      <td>
                        <span v-if="item.size" class="badge bg-dark text-white mr-1">{{ item.size }}</span>
                        <span v-if="item.color" class="badge bg-secondary text-white">{{ item.color }}</span>
                      </td>
                      <td>
                        <span class="font-weight-bold text-dark">{{ formatPrice(item.price) }}</span>
                      </td>
                      <td>
                        <div class="input-group input-group-sm">
                          <button class="btn btn-outline-secondary" type="button" @click="updateQuantity(item, -1)">-</button>
                          <input type="number" class="form-control text-center font-weight-bold" :value="item.quantity" readonly />
                          <button class="btn btn-outline-secondary" type="button" @click="updateQuantity(item, 1)">+</button>
                        </div>
                      </td>
                      <td>
                        <span class="font-weight-bold text-danger">{{ formatPrice(item.itemTotal) }}</span>
                      </td>
                      <td>
                        <button class="btn btn-sm btn-outline-danger" @click="removeItem(item.id)" title="Xóa item">
                          <i class="fa fa-times"></i>
                        </button>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>

        <!-- Cột Tổng Tiền & Thanh Toán (Cart Summary) -->
        <div class="col-lg-4 mb-4">
          <div class="card border-0 shadow-sm rounded-lg bg-white p-4">
            <h5 class="font-weight-bold text-dark mb-4 border-bottom pb-2">TỔNG ĐƠN HÀNG (CART SUMMARY)</h5>

            <div class="d-flex justify-content-between align-items-center mb-3">
              <span class="text-muted">Tổng số sản phẩm:</span>
              <span class="font-weight-bold text-dark">{{ totalItems }} món</span>
            </div>

            <div class="d-flex justify-content-between align-items-center mb-3">
              <span class="text-muted">Tạm tính (Subtotal):</span>
              <span class="font-weight-bold text-dark h5 mb-0">{{ formatPrice(subtotal) }}</span>
            </div>

            <div class="d-flex justify-content-between align-items-center mb-4 pt-3 border-top">
              <span class="font-weight-bold text-dark h6 mb-0">TỔNG THANH TOÁN:</span>
              <span class="font-weight-bold text-danger h4 mb-0">{{ formatPrice(subtotal) }}</span>
            </div>

            <button
              class="btn btn-danger btn-lg w-100 font-weight-bold text-uppercase py-3"
              @click="goToCheckout"
            >
              PROCEED TO CHECKOUT <i class="fa fa-arrow-right ml-2"></i>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
