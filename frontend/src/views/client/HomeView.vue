<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as bannerApi from '@/api/bannerApi'

const activeTab = ref('all')

const setTab = (tab) => {
  activeTab.value = tab
}

// ─── State Banner Động từ Backend API cho 4 vị trí ─────────────
const homeTopBanners = ref([
  {
    id: 1,
    title: 'Bộ Sưu Tập Thu - Đông 2030',
    description: 'Thương hiệu chuyên sáng tạo các sản phẩm thời trang cao cấp với cam kết chất lượng vượt trội.',
    imageUrl: '/img/hero/hero-1.jpg',
    targetUrl: '/products',
    sortOrder: 1
  },
  {
    id: 2,
    title: 'Khuyến Mãi Đặc Biệt Mùa Hè 2030',
    description: 'Giảm giá lên đến 50% cho toàn bộ bộ sưu tập áo thun và quần short thể thao nam.',
    imageUrl: '/img/hero/hero-2.jpg',
    targetUrl: '/products',
    sortOrder: 2
  }
])

const homeMiddleBanners = ref([
  { id: 101, title: 'Banner 4: Bộ Sưu Tập Quần Áo Nam', description: 'Phong cách nam tính, lịch lãm và hiện đại.', imageUrl: '/img/banner/banner-1.jpg', targetUrl: '/products', sortOrder: 1 },
  { id: 102, title: 'Banner 5: Thời Trang Streetwear Phố', description: 'BST Streetwear phong cách giới trẻ siêu hot.', imageUrl: '/img/banner/banner-2.jpg', targetUrl: '/products', sortOrder: 2 }
])

const categoryTopBanners = ref([
  { id: 201, title: 'Banner 3: Ưu Đãi Danh Mục Sản Phẩm', description: 'Khám phá các sản phẩm chất lượng cao bán chạy nhất.', imageUrl: '/img/banner/banner-3.jpg', targetUrl: '/products', sortOrder: 1 },
  { id: 202, title: 'Banner 6: BST Phụ Kiện Thời Trang Nam', description: 'Túi xách, thắt lưng & ví da nam cao cấp.', imageUrl: '/img/banner/banner-2.jpg', targetUrl: '/products', sortOrder: 2 }
])

const categoryMiddleBanners = ref([
  { id: 301, title: 'Banner 7: Đặc Quyền Khách Hàng Thân Thiết', description: 'Sở hữu ngay các item thời trang hot nhất mùa giải.', imageUrl: '/img/banner/banner-1.jpg', targetUrl: '/products', sortOrder: 1 },
  { id: 302, title: 'Banner 8: Xu Hướng Giày & Sneakers 2030', description: 'Mẫu giày thể thao và cổ điển dẫn đầu xu hướng.', imageUrl: '/img/banner/banner-3.jpg', targetUrl: '/products', sortOrder: 2 }
])

// ─── Chỉ Số Trượt Slide 3 Giây Cho Mỗi Vị Trí ───────────────────────
const indexHomeTop = ref(0)
const indexHomeMiddle = ref(0)
const indexCategoryTop = ref(0)
const indexCategoryMiddle = ref(0)

let autoSlideTimer = null

// Tải dữ liệu Banner từ API Backend
const fetchHomeBanners = async () => {
  try {
    const [resTop, resMiddle, resCatTop, resCatMiddle] = await Promise.all([
      bannerApi.getClientBanners('HOME_TOP'),
      bannerApi.getClientBanners('HOME_MIDDLE'),
      bannerApi.getClientBanners('CATEGORY_TOP'),
      bannerApi.getClientBanners('CATEGORY_MIDDLE')
    ])

    if (resTop.data?.data?.length > 0) homeTopBanners.value = resTop.data.data
    if (resMiddle.data?.data?.length > 0) homeMiddleBanners.value = resMiddle.data.data
    if (resCatTop.data?.data?.length > 0) categoryTopBanners.value = resCatTop.data.data
    if (resCatMiddle.data?.data?.length > 0) categoryMiddleBanners.value = resCatMiddle.data.data
  } catch {
    // Nếu chưa có backend API sẵn sàng thì dùng dữ liệu mẫu phía trên
  }
}

// Chuyển slide tự động 3 giây (3000ms)
const nextSlides = () => {
  if (homeTopBanners.value.length > 0) {
    indexHomeTop.value = (indexHomeTop.value + 1) % homeTopBanners.value.length
  }
  if (homeMiddleBanners.value.length > 0) {
    indexHomeMiddle.value = (indexHomeMiddle.value + 1) % homeMiddleBanners.value.length
  }
  if (categoryTopBanners.value.length > 0) {
    indexCategoryTop.value = (indexCategoryTop.value + 1) % categoryTopBanners.value.length
  }
  if (categoryMiddleBanners.value.length > 0) {
    indexCategoryMiddle.value = (indexCategoryMiddle.value + 1) % categoryMiddleBanners.value.length
  }
}

const startAutoSlide = () => {
  stopAutoSlide()
  autoSlideTimer = setInterval(() => {
    nextSlides()
  }, 3000) // Trượt Slide sau mỗi 3 giây
}

const stopAutoSlide = () => {
  if (autoSlideTimer) {
    clearInterval(autoSlideTimer)
    autoSlideTimer = null
  }
}

// Cho phép người dùng bấm trượt thủ công
const prevMiddleSlide = () => {
  indexHomeMiddle.value = (indexHomeMiddle.value - 1 + homeMiddleBanners.value.length) % homeMiddleBanners.value.length
}
const nextMiddleSlide = () => {
  indexHomeMiddle.value = (indexHomeMiddle.value + 1) % homeMiddleBanners.value.length
}

const prevCatTopSlide = () => {
  indexCategoryTop.value = (indexCategoryTop.value - 1 + categoryTopBanners.value.length) % categoryTopBanners.value.length
}
const nextCatTopSlide = () => {
  indexCategoryTop.value = (indexCategoryTop.value + 1) % categoryTopBanners.value.length
}

const prevCatMiddleSlide = () => {
  indexCategoryMiddle.value = (indexCategoryMiddle.value - 1 + categoryMiddleBanners.value.length) % categoryMiddleBanners.value.length
}
const nextCatMiddleSlide = () => {
  indexCategoryMiddle.value = (indexCategoryMiddle.value + 1) % categoryMiddleBanners.value.length
}

// Mockup danh sách sản phẩm
const products = ref([
  { id: 1, name: 'Áo Khoác Biker Piqué', category: 'new-arrivals', price: 1550000, originalPrice: 1800000, image: '/img/product/product-1.jpg', tag: 'New', rating: 5 },
  { id: 2, name: 'Áo Thun Basic Cotton Unisex', category: 'hot-sales', price: 350000, image: '/img/product/product-2.jpg', tag: 'Sale', rating: 4 },
  { id: 3, name: 'Áo Sơ Mi Nam Tay Dài Oxford', category: 'new-arrivals', price: 620000, image: '/img/product/product-3.jpg', rating: 5 },
  { id: 4, name: 'Quần Jean Nam Skinny Stretch', category: 'hot-sales', price: 890000, image: '/img/product/product-4.jpg', rating: 4 },
  { id: 5, name: 'Áo Polo Nam Thêu Logo', category: 'new-arrivals', price: 490000, image: '/img/product/product-5.jpg', tag: 'New', rating: 5 },
  { id: 6, name: 'Áo Khoác Blazer Nam Form Rộng', category: 'hot-sales', price: 2100000, image: '/img/product/product-6.jpg', rating: 5 },
  { id: 7, name: 'Quần Short Nam Thể Thao', category: 'new-arrivals', price: 280000, image: '/img/product/product-7.jpg', rating: 4 },
  { id: 8, name: 'Áo Hoodies Fleece Nỉ Bông', category: 'hot-sales', price: 750000, image: '/img/product/product-8.jpg', tag: 'Sale', rating: 5 },
])

const formatPrice = (value) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value)

onMounted(() => {
  fetchHomeBanners()
  startAutoSlide()
})

onUnmounted(() => {
  stopAutoSlide()
})
</script>

<template>
  <div class="home-page">
    <!-- 1. HOME_TOP Slide Banner (Hero Section Đầu Trang - Auto Slide 3s) -->
    <section class="hero mb-5">
      <div class="hero__slider position-relative overflow-hidden" @mouseenter="stopAutoSlide" @mouseleave="startAutoSlide">
        <Transition name="slide-fade" mode="out-in">
          <div
            :key="indexHomeTop"
            class="hero__items set-bg"
            :style="{ backgroundImage: `url(${homeTopBanners[indexHomeTop]?.imageUrl || '/img/hero/hero-1.jpg'})` }"
          >
            <div class="container">
              <div class="row">
                <div class="col-xl-6 col-lg-7 col-md-8">
                  <div class="hero__text">
                    <h6>
                      <span class="badge bg-danger text-white mr-2">HOME_TOP • Slide {{ homeTopBanners[indexHomeTop]?.sortOrder }}</span>
                    </h6>
                    <h2>{{ homeTopBanners[indexHomeTop]?.title }}</h2>
                    <p>{{ homeTopBanners[indexHomeTop]?.description || 'Thương hiệu chuyên sáng tạo các sản phẩm thời trang cao cấp.' }}</p>
                    <RouterLink :to="homeTopBanners[indexHomeTop]?.targetUrl || '/products'" class="primary-btn">
                      Khám phá ngay <span class="arrow_right"></span>
                    </RouterLink>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </Transition>

        <!-- Indicators chấm tròn -->
        <div v-if="homeTopBanners.length > 1" class="slide-dots">
          <span
            v-for="(banner, idx) in homeTopBanners"
            :key="banner.id"
            class="slide-dot"
            :class="{ active: idx === indexHomeTop }"
            @click="indexHomeTop = idx"
          ></span>
        </div>
      </div>
    </section>

    <!-- 2. HOME_MIDDLE Banner (Nằm Ngang - CỘT PHẢI - Auto Slide 3s Trượt Ngang) -->
    <section class="banner-section my-5">
      <div class="container">
        <div class="row justify-content-end">
          <div class="col-lg-8 col-md-10">
            <div
              class="horizontal-banner-wrapper shadow-sm rounded overflow-hidden position-relative"
              @mouseenter="stopAutoSlide"
              @mouseleave="startAutoSlide"
            >
              <Transition name="slide-card" mode="out-in">
                <div
                  :key="indexHomeMiddle"
                  class="horizontal-banner-card bg-white d-flex align-items-center flex-row"
                >
                  <div class="banner-img-col w-50 overflow-hidden">
                    <img :src="homeMiddleBanners[indexHomeMiddle]?.imageUrl" :alt="homeMiddleBanners[indexHomeMiddle]?.title" class="img-fluid w-100 h-100 object-cover">
                  </div>
                  <div class="banner-text-col w-50 p-4">
                    <span class="badge bg-info text-white mb-2">HOME_MIDDLE (Cột Phải) • Slide {{ homeMiddleBanners[indexHomeMiddle]?.sortOrder }}</span>
                    <h3 class="h4 font-weight-bold text-dark mb-2">{{ homeMiddleBanners[indexHomeMiddle]?.title }}</h3>
                    <p class="text-muted small mb-3">{{ homeMiddleBanners[indexHomeMiddle]?.description }}</p>
                    <RouterLink :to="homeMiddleBanners[indexHomeMiddle]?.targetUrl || '/products'" class="btn btn-outline-danger btn-sm text-uppercase font-weight-bold">
                      Mua ngay <i class="fa fa-arrow-right ml-1"></i>
                    </RouterLink>
                  </div>
                </div>
              </Transition>

              <!-- Mũi tên trượt Card -->
              <button v-if="homeMiddleBanners.length > 1" class="card-nav-btn prev-card" @click="prevMiddleSlide">
                <i class="fa fa-chevron-left"></i>
              </button>
              <button v-if="homeMiddleBanners.length > 1" class="card-nav-btn next-card" @click="nextMiddleSlide">
                <i class="fa fa-chevron-right"></i>
              </button>

              <!-- Page indicator -->
              <div v-if="homeMiddleBanners.length > 1" class="banner-mini-dots">
                <span
                  v-for="(b, idx) in homeMiddleBanners"
                  :key="b.id"
                  class="mini-dot"
                  :class="{ active: idx === indexHomeMiddle }"
                  @click="indexHomeMiddle = idx"
                ></span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 3. CATEGORY_TOP Banner (Nằm Ngang - CỘT TRÁI - Auto Slide 3s Trượt Ngang) -->
    <section class="banner-section my-5">
      <div class="container">
        <div class="row justify-content-start">
          <div class="col-lg-8 col-md-10">
            <div
              class="horizontal-banner-wrapper shadow-sm rounded overflow-hidden position-relative"
              @mouseenter="stopAutoSlide"
              @mouseleave="startAutoSlide"
            >
              <Transition name="slide-card" mode="out-in">
                <div
                  :key="indexCategoryTop"
                  class="horizontal-banner-card bg-dark text-white d-flex align-items-center flex-row-reverse"
                >
                  <div class="banner-img-col w-50 overflow-hidden">
                    <img :src="categoryTopBanners[indexCategoryTop]?.imageUrl" :alt="categoryTopBanners[indexCategoryTop]?.title" class="img-fluid w-100 h-100 object-cover">
                  </div>
                  <div class="banner-text-col w-50 p-4">
                    <span class="badge bg-warning text-dark mb-2">CATEGORY_TOP (Cột Trái) • Slide {{ categoryTopBanners[indexCategoryTop]?.sortOrder }}</span>
                    <h3 class="h4 font-weight-bold text-white mb-2">{{ categoryTopBanners[indexCategoryTop]?.title }}</h3>
                    <p class="text-white-50 small mb-3">{{ categoryTopBanners[indexCategoryTop]?.description }}</p>
                    <RouterLink :to="categoryTopBanners[indexCategoryTop]?.targetUrl || '/products'" class="btn btn-danger btn-sm text-uppercase font-weight-bold">
                      Khám phá ngay <i class="fa fa-arrow-right ml-1"></i>
                    </RouterLink>
                  </div>
                </div>
              </Transition>

              <!-- Mũi tên trượt Card -->
              <button v-if="categoryTopBanners.length > 1" class="card-nav-btn prev-card" @click="prevCatTopSlide">
                <i class="fa fa-chevron-left"></i>
              </button>
              <button v-if="categoryTopBanners.length > 1" class="card-nav-btn next-card" @click="nextCatTopSlide">
                <i class="fa fa-chevron-right"></i>
              </button>

              <!-- Page indicator -->
              <div v-if="categoryTopBanners.length > 1" class="banner-mini-dots">
                <span
                  v-for="(b, idx) in categoryTopBanners"
                  :key="b.id"
                  class="mini-dot"
                  :class="{ active: idx === indexCategoryTop }"
                  @click="indexCategoryTop = idx"
                ></span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 4. CATEGORY_MIDDLE Banner (Nằm Dưới CATEGORY_TOP - CỘT PHẢI - Auto Slide 3s Trượt Ngang) -->
    <section class="banner-section my-5">
      <div class="container">
        <div class="row justify-content-end">
          <div class="col-lg-8 col-md-10">
            <div
              class="horizontal-banner-wrapper shadow-sm rounded overflow-hidden position-relative"
              @mouseenter="stopAutoSlide"
              @mouseleave="startAutoSlide"
            >
              <Transition name="slide-card" mode="out-in">
                <div
                  :key="indexCategoryMiddle"
                  class="horizontal-banner-card bg-light border d-flex align-items-center flex-row"
                >
                  <div class="banner-img-col w-50 overflow-hidden">
                    <img :src="categoryMiddleBanners[indexCategoryMiddle]?.imageUrl" :alt="categoryMiddleBanners[indexCategoryMiddle]?.title" class="img-fluid w-100 h-100 object-cover">
                  </div>
                  <div class="banner-text-col w-50 p-4">
                    <span class="badge bg-secondary text-white mb-2">CATEGORY_MIDDLE (Cột Phải) • Slide {{ categoryMiddleBanners[indexCategoryMiddle]?.sortOrder }}</span>
                    <h3 class="h4 font-weight-bold text-dark mb-2">{{ categoryMiddleBanners[indexCategoryMiddle]?.title }}</h3>
                    <p class="text-muted small mb-3">{{ categoryMiddleBanners[indexCategoryMiddle]?.description }}</p>
                    <RouterLink :to="categoryMiddleBanners[indexCategoryMiddle]?.targetUrl || '/products'" class="btn btn-dark btn-sm text-uppercase font-weight-bold">
                      Xem ưu đãi ngay <i class="fa fa-arrow-right ml-1"></i>
                    </RouterLink>
                  </div>
                </div>
              </Transition>

              <!-- Mũi tên trượt Card -->
              <button v-if="categoryMiddleBanners.length > 1" class="card-nav-btn prev-card" @click="prevCatMiddleSlide">
                <i class="fa fa-chevron-left"></i>
              </button>
              <button v-if="categoryMiddleBanners.length > 1" class="card-nav-btn next-card" @click="nextCatMiddleSlide">
                <i class="fa fa-chevron-right"></i>
              </button>

              <!-- Page indicator -->
              <div v-if="categoryMiddleBanners.length > 1" class="banner-mini-dots">
                <span
                  v-for="(b, idx) in categoryMiddleBanners"
                  :key="b.id"
                  class="mini-dot"
                  :class="{ active: idx === indexCategoryMiddle }"
                  @click="indexCategoryMiddle = idx"
                ></span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Product Section Begin -->
    <section class="product spad">
      <div class="container">
        <div class="row">
          <div class="col-lg-12">
            <ul class="filter__controls">
              <li :class="{ active: activeTab === 'all' }" @click="setTab('all')">Bán Chạy Nhất</li>
              <li :class="{ active: activeTab === 'new-arrivals' }" @click="setTab('new-arrivals')">Hàng Mới Về</li>
              <li :class="{ active: activeTab === 'hot-sales' }" @click="setTab('hot-sales')">Hot Sale</li>
            </ul>
          </div>
        </div>

        <div class="row product__filter">
          <div
            v-for="product in products.filter(p => activeTab === 'all' || p.category === activeTab)"
            :key="product.id"
            class="col-lg-3 col-md-6 col-sm-6 mb-4"
          >
            <div class="product__item">
              <div class="product__item__pic set-bg" :style="{ backgroundImage: `url(${product.image})` }">
                <span v-if="product.tag" class="label" :class="{ 'sale-label': product.tag === 'Sale' }">{{ product.tag }}</span>
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
      </div>
    </section>

    <!-- Instagram Section Begin -->
    <section class="instagram spad">
      <div class="container">
        <div class="row">
          <div class="col-lg-8">
            <div class="instagram__pic">
              <div class="instagram__pic__item set-bg" style="background-image: url('/img/instagram/instagram-1.jpg');"></div>
              <div class="instagram__pic__item set-bg" style="background-image: url('/img/instagram/instagram-2.jpg');"></div>
              <div class="instagram__pic__item set-bg" style="background-image: url('/img/instagram/instagram-3.jpg');"></div>
              <div class="instagram__pic__item set-bg" style="background-image: url('/img/instagram/instagram-4.jpg');"></div>
              <div class="instagram__pic__item set-bg" style="background-image: url('/img/instagram/instagram-5.jpg');"></div>
              <div class="instagram__pic__item set-bg" style="background-image: url('/img/instagram/instagram-6.jpg');"></div>
            </div>
          </div>
          <div class="col-lg-4">
            <div class="instagram__text">
              <h2>Instagram</h2>
              <p>Khám phá phong cách thời trang mới nhất cùng Male Fashion trên mạng xã hội Instagram.</p>
              <h3>#Male_Fashion</h3>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.hero__items {
  padding-top: 180px;
  padding-bottom: 140px;
  background-size: cover;
  background-position: center center;
}

/* ─── Vue Transition Slide Animations (Trượt Ngang Mượt Mà) ─── */
.slide-fade-enter-active,
.slide-fade-leave-active {
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}
.slide-fade-enter-from {
  opacity: 0;
  transform: translateX(40px);
}
.slide-fade-leave-to {
  opacity: 0;
  transform: translateX(-40px);
}

.slide-card-enter-active,
.slide-card-leave-active {
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}
.slide-card-enter-from {
  opacity: 0;
  transform: translateX(60px);
}
.slide-card-leave-to {
  opacity: 0;
  transform: translateX(-60px);
}

.slide-dots {
  position: absolute;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 10px;
  z-index: 10;
}

.slide-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.4);
  cursor: pointer;
  transition: all 0.3s;
}

.slide-dot.active {
  background: #e53637;
  width: 28px;
  border-radius: 6px;
}

/* Horizontal Card Banners */
.horizontal-banner-wrapper {
  position: relative;
  min-height: 220px;
}

.horizontal-banner-card {
  min-height: 220px;
}

.object-cover {
  object-fit: cover;
  min-height: 220px;
}

/* Nút mũi tên chuyển card */
.card-nav-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 32px;
  height: 32px;
  background: rgba(0, 0, 0, 0.4);
  color: #ffffff;
  border: none;
  border-radius: 50%;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 10;
  transition: all 0.2s;
}

.card-nav-btn:hover {
  background: #e53637;
}

.prev-card { left: 10px; }
.next-card { right: 10px; }

.banner-mini-dots {
  position: absolute;
  bottom: 12px;
  right: 20px;
  display: flex;
  gap: 6px;
  z-index: 5;
}

.mini-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.3);
  cursor: pointer;
  transition: all 0.3s;
}

.mini-dot.active {
  background: #e53637;
  width: 20px;
  border-radius: 4px;
}

.sale-label {
  background: #111111 !important;
  color: #ffffff !important;
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
