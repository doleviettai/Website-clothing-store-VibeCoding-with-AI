<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import * as bannerApi from '@/api/bannerApi'

const selectedCategory = ref('all')
const selectedBrand = ref('all')
const selectedSort = ref('newest')
const searchKeyword = ref('')

// ─── State Banner Động CATEGORY_TOP và CATEGORY_MIDDLE ───────
const categoryTopBanners = ref([
  { id: 201, title: 'Ưu Đãi Đặc Biệt Chuyên Mục Thời Trang', imageUrl: '/img/hero/hero-1.jpg', targetUrl: '/products', sortOrder: 1 }
])

const categoryMiddleBanners = ref([
  { id: 202, title: 'Khuyến Mãi Quần Áo Nam Mùa Hè', imageUrl: '/img/banner/banner-1.jpg', targetUrl: '/products', sortOrder: 1 }
])

const currentCategoryTopIndex = ref(0)
const currentCategoryMiddleIndex = ref(0)
let autoSlideTimer = null

const fetchCategoryBanners = async () => {
  try {
    const resTop = await bannerApi.getClientBanners('CATEGORY_TOP')
    if (resTop.data.data && resTop.data.data.length > 0) {
      categoryTopBanners.value = resTop.data.data
    }

    const resMiddle = await bannerApi.getClientBanners('CATEGORY_MIDDLE')
    if (resMiddle.data.data && resMiddle.data.data.length > 0) {
      categoryMiddleBanners.value = resMiddle.data.data
    }
  } catch {
    //
  }
}

const startAutoSlide = () => {
  stopAutoSlide()
  autoSlideTimer = setInterval(() => {
    if (categoryTopBanners.value.length > 0) {
      currentCategoryTopIndex.value = (currentCategoryTopIndex.value + 1) % categoryTopBanners.value.length
    }
    if (categoryMiddleBanners.value.length > 0) {
      currentCategoryMiddleIndex.value = (currentCategoryMiddleIndex.value + 1) % categoryMiddleBanners.value.length
    }
  }, 5000) // Tự động trượt Slide sau 5 giây
}

const stopAutoSlide = () => {
  if (autoSlideTimer) clearInterval(autoSlideTimer)
}

const categories = [
  { id: 'all', name: 'Tất cả chuyên mục', count: 120 },
  { id: 'ao-thun', name: 'Áo thun & Polo', count: 45 },
  { id: 'ao-so-mi', name: 'Áo sơ mi', count: 30 },
  { id: 'ao-khoac', name: 'Áo khoác & Blazer', count: 25 },
  { id: 'quan-jean', name: 'Quần Jean & Tây', count: 20 },
]

const brands = [
  { id: 'all', name: 'Tất cả thương hiệu' },
  { id: 'nike', name: 'Nike' },
  { id: 'adidas', name: 'Adidas' },
  { id: 'uniqlo', name: 'Uniqlo' },
  { id: 'zara', name: 'Zara' },
]

const allProducts = ref([
  { id: 1, name: 'Áo Khoác Biker Piqué', category: 'ao-khoac', brand: 'zara', price: 1550000, image: '/img/product/product-1.jpg', rating: 5, isNew: true },
  { id: 2, name: 'Áo Thun Basic Cotton Unisex', category: 'ao-thun', brand: 'uniqlo', price: 350000, image: '/img/product/product-2.jpg', rating: 4 },
  { id: 3, name: 'Áo Sơ Mi Nam Tay Dài Oxford', category: 'ao-so-mi', brand: 'zara', price: 620000, image: '/img/product/product-3.jpg', rating: 5 },
  { id: 4, name: 'Quần Jean Nam Skinny Stretch', category: 'quan-jean', brand: 'nike', price: 890000, image: '/img/product/product-4.jpg', rating: 4 },
  { id: 5, name: 'Áo Polo Nam Thêu Logo', category: 'ao-thun', brand: 'adidas', price: 490000, image: '/img/product/product-5.jpg', rating: 5, isNew: true },
  { id: 6, name: 'Áo Khoác Blazer Nam Form Rộng', category: 'ao-khoac', brand: 'uniqlo', price: 2100000, image: '/img/product/product-6.jpg', rating: 5 },
  { id: 7, name: 'Quần Short Nam Thể Thao', category: 'quan-jean', brand: 'nike', price: 280000, image: '/img/product/product-7.jpg', rating: 4 },
  { id: 8, name: 'Áo Hoodies Fleece Nỉ Bông', category: 'ao-khoac', brand: 'adidas', price: 750000, image: '/img/product/product-8.jpg', rating: 5 },
])

const filteredProducts = computed(() => {
  return allProducts.value.filter(p => {
    const matchCategory = selectedCategory.value === 'all' || p.category === selectedCategory.value
    const matchBrand = selectedBrand.value === 'all' || p.brand === selectedBrand.value
    const matchKeyword = !searchKeyword.value || p.name.toLowerCase().includes(searchKeyword.value.toLowerCase())
    return matchCategory && matchBrand && matchKeyword
  }).sort((a, b) => {
    if (selectedSort.value === 'price-asc') return a.price - b.price
    if (selectedSort.value === 'price-desc') return b.price - a.price
    return b.id - a.id
  })
})

const formatPrice = (val) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)

onMounted(() => {
  fetchCategoryBanners()
  startAutoSlide()
})

onUnmounted(() => {
  stopAutoSlide()
})
</script>

<template>
  <div class="shop-page">
    <!-- Breadcrumb Begin -->
    <section class="breadcrumb-option">
      <div class="container">
        <div class="row">
          <div class="col-lg-12">
            <div class="breadcrumb__text">
              <h4>Cửa hàng</h4>
              <div class="breadcrumb__links">
                <RouterLink to="/">Trang chủ</RouterLink>
                <span>Sản phẩm</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Banner SECTION CATEGORY_TOP Slide (Đầu Trang Cửa Hàng - Tự động trượt 5s) -->
    <section v-if="categoryTopBanners.length > 0" class="category-top-banner my-4">
      <div class="container">
        <div class="position-relative overflow-hidden rounded shadow-sm">
          <template v-for="(banner, idx) in categoryTopBanners" :key="banner.id">
            <div
              v-show="idx === currentCategoryTopIndex"
              class="category-banner-item p-5 text-white fade-in-slide"
              :style="{ backgroundImage: `linear-gradient(rgba(0,0,0,0.4), rgba(0,0,0,0.4)), url(${banner.imageUrl})` }"
            >
              <span class="badge bg-danger mb-2">CATEGORY_TOP • Slide {{ banner.sortOrder }}</span>
              <h2 class="display-5 font-weight-bold text-white mb-2">{{ banner.title }}</h2>
              <p v-if="banner.description" class="lead mb-4">{{ banner.description }}</p>
              <RouterLink :to="banner.targetUrl || '/products'" class="btn btn-danger btn-lg font-weight-bold">
                Khám phá ngay
              </RouterLink>
            </div>
          </template>
        </div>
      </div>
    </section>

    <!-- Shop Section Begin -->
    <section class="shop spad">
      <div class="container">
        <div class="row">
          <!-- Sidebar Bộ lọc -->
          <div class="col-lg-3">
            <div class="shop__sidebar">
              <div class="shop__sidebar__search mb-4">
                <div class="input-group">
                  <input
                    v-model="searchKeyword"
                    type="text"
                    class="form-control"
                    placeholder="Tìm kiếm sản phẩm tức thì (Live Search)..."
                  />
                </div>
              </div>

              <!-- Lọc Chuyên mục -->
              <div class="shop__sidebar__accordion">
                <div class="accordion">
                  <div class="card border-0 mb-3">
                    <div class="card-heading font-weight-bold mb-2">Chuyên Mục</div>
                    <div class="card-body p-0">
                      <ul class="list-unstyled">
                        <li
                          v-for="cat in categories"
                          :key="cat.id"
                          class="py-1 cursor-pointer"
                          :class="{ 'font-weight-bold text-danger': selectedCategory === cat.id }"
                          @click="selectedCategory = cat.id"
                        >
                          {{ cat.name }} ({{ cat.count }})
                        </li>
                      </ul>
                    </div>
                  </div>

                  <!-- Lọc Thương hiệu -->
                  <div class="card border-0 mb-3">
                    <div class="card-heading font-weight-bold mb-2">Thương Hiệu</div>
                    <div class="card-body p-0">
                      <ul class="list-unstyled">
                        <li
                          v-for="brand in brands"
                          :key="brand.id"
                          class="py-1 cursor-pointer"
                          :class="{ 'font-weight-bold text-danger': selectedBrand === brand.id }"
                          @click="selectedBrand = brand.id"
                        >
                          {{ brand.name }}
                        </li>
                      </ul>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Danh sách sản phẩm -->
          <div class="col-lg-9">
            <div class="shop__product__option mb-4">
              <div class="row align-items-center">
                <div class="col-lg-6 col-md-6 col-sm-6">
                  <p class="mb-0">Hiển thị {{ filteredProducts.length }} sản phẩm</p>
                </div>
                <div class="col-lg-6 col-md-6 col-sm-6">
                  <div class="d-flex align-items-center justify-content-end gap-2">
                    <span>Sắp xếp:</span>
                    <select v-model="selectedSort" class="form-control w-auto">
                      <option value="newest">Mới nhất</option>
                      <option value="price-asc">Giá: Thấp đến Cao</option>
                      <option value="price-desc">Giá: Cao đến Thấp</option>
                    </select>
                  </div>
                </div>
              </div>
            </div>

            <div class="row">
              <div
                v-for="product in filteredProducts"
                :key="product.id"
                class="col-lg-4 col-md-6 col-sm-6 mb-4"
              >
                <div class="product__item">
                  <div class="product__item__pic set-bg" :style="{ backgroundImage: `url(${product.image})` }">
                    <span v-if="product.isNew" class="label">New</span>
                    <ul class="product__hover">
                      <li><a href="#"><img src="/img/icon/heart.png" alt="Yêu thích"></a></li>
                      <li><RouterLink to="/products"><img src="/img/icon/search.png" alt="Chi tiết"></RouterLink></li>
                    </ul>
                  </div>
                  <div class="product__item__text">
                    <h6>{{ product.name }}</h6>
                    <RouterLink to="/cart" class="add-cart">+ Thêm vào giỏ</RouterLink>
                    <div class="rating">
                      <i v-for="star in 5" :key="star" class="fa" :class="star <= product.rating ? 'fa-star' : 'fa-star-o'"></i>
                    </div>
                    <h5>{{ formatPrice(product.price) }}</h5>
                  </div>
                </div>
              </div>
            </div>

            <!-- Banner SECTION CATEGORY_MIDDLE Slide (Giữa Cửa Hàng - Tự động trượt 5s) -->
            <div v-if="categoryMiddleBanners.length > 0" class="category-middle-banner my-5">
              <div class="position-relative overflow-hidden rounded shadow-sm">
                <template v-for="(banner, idx) in categoryMiddleBanners" :key="banner.id">
                  <div
                    v-show="idx === currentCategoryMiddleIndex"
                    class="category-middle-item p-4 text-white fade-in-slide"
                    :style="{ backgroundImage: `linear-gradient(rgba(0,0,0,0.5), rgba(0,0,0,0.5)), url(${banner.imageUrl})` }"
                  >
                    <span class="badge bg-danger mb-2">CATEGORY_MIDDLE • Slide {{ banner.sortOrder }}</span>
                    <h3 class="font-weight-bold text-white mb-2">{{ banner.title }}</h3>
                    <RouterLink :to="banner.targetUrl || '/products'" class="btn btn-outline-light btn-sm mt-2">
                      Xem ngay <i class="fa fa-arrow-right ml-1"></i>
                    </RouterLink>
                  </div>
                </template>
              </div>
            </div>

          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.category-banner-item {
  min-height: 220px;
  background-size: cover;
  background-position: center center;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.category-middle-item {
  min-height: 160px;
  background-size: cover;
  background-position: center center;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.fade-in-slide {
  animation: fadeIn 0.6s ease-in-out;
}

@keyframes fadeIn {
  from { opacity: 0.4; }
  to { opacity: 1; }
}

.cursor-pointer {
  cursor: pointer;
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

.rating i {
  color: #f7941d;
  margin-right: 2px;
}
</style>
