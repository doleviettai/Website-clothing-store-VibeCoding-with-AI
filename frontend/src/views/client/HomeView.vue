<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import * as bannerApi from '@/api/bannerApi'
import * as productApi from '@/api/productApi'

const router = useRouter()

// ─── State Tab Product Spad ─────────────────────────────────────
// Mặc định tab 'best-sellers' (Bán Chạy Nhất)
const activeTab = ref('best-sellers')

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
    //
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

const startAutoSlide1 = () => {
  stopAutoSlide()
  autoSlideTimer = setInterval(() => {
    nextSlides()
  }, 3000)
}
const startAutoSlide2 = () => {
  stopAutoSlide()
  autoSlideTimer = setInterval(() => {
    nextSlides()
  }, 3000)
}

const stopAutoSlide = () => {
  if (autoSlideTimer) {
    clearInterval(autoSlideTimer)
    autoSlideTimer = null
  }
}

// Bấm trượt thủ công
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

// ─── Danh Sách Sản Phẩm Cho Product Spad (15 Sản Phẩm) ─────────────
const rawProducts = ref([
  { id: 1, name: 'Áo Khoác Biker Piqué Nam', price: 1550000, salePrice: 1250000, thumbnail: '/img/product/product-1.jpg', rating: 5.0, tag: 'Hot', isSale: true, createdAt: '2026-08-20' },
  { id: 2, name: 'Áo Thun Basic Cotton Unisex', price: 350000, salePrice: 290000, thumbnail: '/img/product/product-2.jpg', rating: 5.0, tag: 'Sale', isSale: true, createdAt: '2026-08-25' },
  { id: 3, name: 'Áo Sơ Mi Nam Tay Dài Oxford', price: 620000, salePrice: null, thumbnail: '/img/product/product-3.jpg', rating: 5.0, tag: 'New', isSale: false, createdAt: '2026-08-28' },
  { id: 4, name: 'Quần Jean Nam Skinny Stretch Slimfit', price: 890000, salePrice: 690000, thumbnail: '/img/product/product-4.jpg', rating: 4.8, tag: 'Sale', isSale: true, createdAt: '2026-08-22' },
  { id: 5, name: 'Áo Polo Nam Thêu Logo Cao Cấp', price: 490000, salePrice: null, thumbnail: '/img/product/product-5.jpg', rating: 5.0, tag: 'New', isSale: false, createdAt: '2026-08-27' },
  { id: 6, name: 'Áo Khoác Blazer Nam Form Rộng Phong Cách', price: 2100000, salePrice: 1750000, thumbnail: '/img/product/product-6.jpg', rating: 5.0, tag: 'Sale', isSale: true, createdAt: '2026-08-21' },
  { id: 7, name: 'Quần Short Nam Thể Thao Co Giãn', price: 280000, salePrice: null, thumbnail: '/img/product/product-7.jpg', rating: 4.6, tag: 'New', isSale: false, createdAt: '2026-08-26' },
  { id: 8, name: 'Áo Hoodies Fleece Nỉ Bông Ấm Áp', price: 750000, salePrice: 590000, thumbnail: '/img/product/product-8.jpg', rating: 5.0, tag: 'Hot', isSale: true, createdAt: '2026-08-24' },
  { id: 9, name: 'Áo Len Nam Cổ Lọ Dệt Kim', price: 580000, salePrice: 480000, thumbnail: '/img/product/product-1.jpg', rating: 5.0, tag: 'Sale', isSale: true, createdAt: '2026-08-23' },
  { id: 10, name: 'Quần Kaki Nam Dáng Dài Regular', price: 520000, salePrice: null, thumbnail: '/img/product/product-2.jpg', rating: 4.7, tag: 'New', isSale: false, createdAt: '2026-08-28' },
  { id: 11, name: 'Áo Khoác Gió Nam 2 Lớp Chống Nước', price: 690000, salePrice: 550000, thumbnail: '/img/product/product-3.jpg', rating: 5.0, tag: 'Sale', isSale: true, createdAt: '2026-08-19' },
  { id: 12, name: 'Bộ Thể Thao Nam Thun Lạnh Co Giãn 4 Chiều', price: 450000, salePrice: 380000, thumbnail: '/img/product/product-4.jpg', rating: 5.0, tag: 'Hot', isSale: true, createdAt: '2026-08-25' },
  { id: 13, name: 'Áo Sơ Mi Họa Tiết Hawaii Đi Biển', price: 390000, salePrice: null, thumbnail: '/img/product/product-5.jpg', rating: 4.5, tag: 'New', isSale: false, createdAt: '2026-08-27' },
  { id: 14, name: 'Quần Jogger Nam Thể Thao Bo Gấu', price: 420000, salePrice: 350000, thumbnail: '/img/product/product-6.jpg', rating: 5.0, tag: 'Sale', isSale: true, createdAt: '2026-08-22' },
  { id: 15, name: 'Áo Cardigan Nam Dệt Kim Cổ V', price: 650000, salePrice: null, thumbnail: '/img/product/product-7.jpg', rating: 4.9, tag: 'New', isSale: false, createdAt: '2026-08-28' },
])

// Tải sản phẩm thực từ API Backend
const fetchProductsFromApi = async () => {
  try {
    const res = await productApi.getClientProducts()
    if (res.data?.data?.length > 0) {
      rawProducts.value = res.data.data.map(p => ({
        id: p.id,
        name: p.name,
        price: p.price,
        salePrice: p.salePrice,
        thumbnail: p.thumbnailUrl || '/img/product/product-1.jpg',
        rating: p.averageRating || 5.0,
        tag: p.salePrice ? 'Sale' : (p.isFeatured ? 'Hot' : 'New'),
        isSale: !!p.salePrice,
        createdAt: p.createdAt
      }))
    }
  } catch {
    //
  }
}

// ─── Computed Lọc 10 - 15 Sản Phẩm Cho Từng Mục ────────────────────

// 1. Mục Bán Chạy Nhất (Các sản phẩm có đánh giá sao cao 5 sao / >= 4.8)
const bestSellerProducts = computed(() => {
  return rawProducts.value
    .filter(p => p.rating >= 4.8)
    .slice(0, 15)
})

// 2. Mục Bán Hàng Mới Về (Tất cả sản phẩm xếp mới nhất)
const newArrivalProducts = computed(() => {
  return [...rawProducts.value]
    .sort((a, b) => b.id - a.id)
    .slice(0, 15)
})

// 3. Mục Hot Sale (Sản phẩm có giá khuyến mãi giảm giá)
const hotSaleProducts = computed(() => {
  return rawProducts.value
    .filter(p => p.salePrice && p.salePrice < p.price)
    .slice(0, 15)
})

// Danh sách sản phẩm hiển thị theo tab đang chọn
const displayedProducts = computed(() => {
  if (activeTab.value === 'best-sellers') return bestSellerProducts.value
  if (activeTab.value === 'new-arrivals') return newArrivalProducts.value
  if (activeTab.value === 'hot-sales') return hotSaleProducts.value
  return rawProducts.value.slice(0, 15)
})

// Điều hướng sang trang cửa hàng khi bấm nút xem thêm
const goToShop = () => {
  router.push('/products')
}

const formatPrice = (value) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value)

onMounted(() => {
  fetchHomeBanners()
  fetchProductsFromApi()
  startAutoSlide1()
})

onUnmounted(() => {
  stopAutoSlide()
})
</script>

<template>
  <div class="home-page">
    <!-- 1. HOME_TOP Slide Banner (Hero Section Đầu Trang - Auto Slide 3s) -->
    <section class="hero mb-5">
      <div class="hero__slider position-relative overflow-hidden" @mouseenter="stopAutoSlide" @mouseleave="startAutoSlide1">
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
              @mouseleave="startAutoSlide2"
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
                    <span class="badge bg-danger text-white mb-2">HOT BANNER</span>
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
              @mouseleave="startAutoSlide2"
            >
              <Transition name="slide-card" mode="out-in">
                <div
                  :key="indexCategoryTop"
                  class="horizontal-banner-card bg-white d-flex align-items-center flex-row"
                >
                  <div class="banner-img-col w-50 overflow-hidden">
                    <img :src="categoryTopBanners[indexCategoryTop]?.imageUrl" :alt="categoryTopBanners[indexCategoryTop]?.title" class="img-fluid w-100 h-100 object-cover">
                  </div>
                  <div class="banner-text-col w-50 p-4">
                    <span class="badge bg-primary text-white mb-2">BỘ SƯU TẬP TỪ MỚI VỀ</span>
                    <h3 class="h4 font-weight-bold text-dark mb-2">{{ categoryTopBanners[indexCategoryTop]?.title }}</h3>
                    <p class="text-muted small mb-3">{{ categoryTopBanners[indexCategoryTop]?.description }}</p>
                    <RouterLink :to="categoryTopBanners[indexCategoryTop]?.targetUrl || '/products'" class="btn btn-outline-primary btn-sm text-uppercase font-weight-bold">
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

    <!-- 4. CATEGORY_MIDDLE Banner (Nằm Dưới CATEGORY_TOP - DỰNG CỘT PHẢI THẲNG HÀNG VỚI HOME_MIDDLE) -->
    <section class="banner-section my-5">
      <div class="container">
        <div class="row justify-content-end">
          <div class="col-lg-8 col-md-10">
            <div
              class="horizontal-banner-wrapper shadow-sm rounded overflow-hidden position-relative"
              @mouseenter="stopAutoSlide"
              @mouseleave="startAutoSlide2"
            >
              <Transition name="slide-card" mode="out-in">
                <div
                  :key="indexCategoryMiddle"
                  class="horizontal-banner-card bg-white d-flex align-items-center flex-row"
                >
                  <div class="banner-img-col w-50 overflow-hidden">
                    <img :src="categoryMiddleBanners[indexCategoryMiddle]?.imageUrl" :alt="categoryMiddleBanners[indexCategoryMiddle]?.title" class="img-fluid w-100 h-100 object-cover">
                  </div>
                  <div class="banner-text-col w-50 p-4">
                    <span class="badge bg-warning text-dark mb-2">ƯU ĐÃI THÀNH VIÊN</span>
                    <h3 class="h4 font-weight-bold text-dark mb-2">{{ categoryMiddleBanners[indexCategoryMiddle]?.title }}</h3>
                    <p class="text-muted small mb-3">{{ categoryMiddleBanners[indexCategoryMiddle]?.description }}</p>
                    <RouterLink :to="categoryMiddleBanners[indexCategoryMiddle]?.targetUrl || '/products'" class="btn btn-outline-warning btn-sm text-uppercase font-weight-bold text-dark">
                      Xem ngay <i class="fa fa-arrow-right ml-1"></i>
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

    <!-- NÂNG CẤP PRODUCT SPAD (Khu vực danh sách sản phẩm) -->
    <section class="product spad py-5">
      <div class="container">
        <div class="row">
          <div class="col-lg-12">
            <!-- Filter Controls Tabs -->
            <ul class="filter__controls text-center mb-5">
              <li :class="{ active: activeTab === 'best-sellers' }" @click="setTab('best-sellers')">
                <i class="fa fa-fire mr-1 text-danger"></i> Bán Chạy Nhất (⭐ 5 Sao)
              </li>
              <li :class="{ active: activeTab === 'new-arrivals' }" @click="setTab('new-arrivals')">
                <i class="fa fa-tag mr-1 text-primary"></i> Hàng Mới Về
              </li>
              <li :class="{ active: activeTab === 'hot-sales' }" @click="setTab('hot-sales')">
                <i class="fa fa-percent mr-1 text-warning"></i> Hot Sale Giảm Giá
              </li>
            </ul>
          </div>
        </div>

        <!-- Danh Sách Sản Phẩm (Lọc 10 - 15 Sản Phẩm) -->
        <div class="row product__filter">
          <div
            v-for="product in displayedProducts"
            :key="product.id"
            class="col-lg-3 col-md-4 col-sm-6 mb-4"
          >
            <div class="product__item h-100 border-0 shadow-sm rounded overflow-hidden bg-white">
              <div class="product__item__pic set-bg position-relative" :style="{ backgroundImage: `url(${product.thumbnail})` }">
                <span
                  v-if="product.isSale || product.tag"
                  class="label"
                  :class="product.isSale ? 'sale-label bg-danger text-white' : 'bg-dark text-white'"
                >
                  {{ product.isSale ? 'GIẢM GIÁ' : product.tag }}
                </span>
                <ul class="product__hover">
                  <li><a href="#" title="Yêu thích"><img src="/img/icon/heart.png" alt="Yêu thích"></a></li>
                  <li><RouterLink to="/products" title="Xem chi tiết"><img src="/img/icon/search.png" alt="Chi tiết"></RouterLink></li>
                </ul>
              </div>
              <div class="product__item__text p-3">
                <h6 class="font-weight-bold text-dark mb-2 text-truncate" :title="product.name">{{ product.name }}</h6>
                <RouterLink to="/cart" class="add-cart text-danger font-weight-bold d-block mb-2">+ Thêm vào giỏ</RouterLink>
                <div class="rating text-warning small mb-2">
                  <i v-for="star in 5" :key="star" class="fa" :class="star <= Math.round(product.rating) ? 'fa-star' : 'fa-star-o'"></i>
                  <span class="text-muted ml-1 font-weight-normal">({{ product.rating.toFixed(1) }})</span>
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
          </div>
        </div>

        <!-- Nút XEM THÊM sản phẩm (Chuyển sang trang Cửa Hàng /products) -->
        <div class="row">
          <div class="col-lg-12 text-center mt-4">
            <button class="primary-btn btn-load-more px-5 py-3 border-0 rounded font-weight-bold" @click="goToShop">
              XEM THÊM SẢN PHẨM <i class="fa fa-arrow-right ml-2"></i>
            </button>
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
  transition: all 0.5s ease-in-out;
}
.slide-fade-enter-from {
  opacity: 0;
  transform: translateX(30px);
}
.slide-fade-leave-to {
  opacity: 0;
  transform: translateX(-30px);
}

.slide-card-enter-active,
.slide-card-leave-active {
  transition: transform 0.5s cubic-bezier(0.4, 0, 0.2, 1), opacity 0.5s ease;
}
.slide-card-enter-from {
  transform: translateX(100%);
  opacity: 0;
}
.slide-card-leave-to {
  transform: translateX(-100%);
  opacity: 0;
}

/* Slide dots */
.slide-dots {
  position: absolute;
  bottom: 20px;
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
  background: rgba(255, 255, 255, 0.5);
  cursor: pointer;
  transition: background 0.3s;
}

.slide-dot.active {
  background: #e53637;
  width: 28px;
  border-radius: 6px;
}

/* Horizontal Banner style */
.horizontal-banner-wrapper {
  min-height: 220px;
  background: #ffffff;
}

.horizontal-banner-card {
  min-height: 220px;
}

.banner-img-col {
  height: 220px;
}

.banner-img-col img {
  object-fit: cover;
}

.card-nav-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  background: rgba(0, 0, 0, 0.4);
  color: #fff;
  border: none;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background 0.2s;
  z-index: 5;
}

.card-nav-btn:hover {
  background: #e53637;
}

.prev-card {
  left: 10px;
}

.next-card {
  right: 10px;
}

.banner-mini-dots {
  position: absolute;
  bottom: 8px;
  right: 20px;
  display: flex;
  gap: 6px;
  z-index: 5;
}

.mini-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.2);
  cursor: pointer;
}

.mini-dot.active {
  background: #e53637;
}

/* Product Spad Filter Controls */
.filter__controls {
  display: flex;
  justify-content: center;
  gap: 20px;
  padding-left: 0;
  list-style: none;
}

.filter__controls li {
  font-size: 1.1rem;
  font-weight: 700;
  color: #b7b7b7;
  cursor: pointer;
  padding: 8px 20px;
  border-radius: 30px;
  transition: all 0.3s ease;
}

.filter__controls li.active,
.filter__controls li:hover {
  color: #111111;
  background: #f3f2ee;
}

.btn-load-more {
  background: #111111;
  color: #ffffff;
  letter-spacing: 2px;
  transition: background 0.3s;
  cursor: pointer;
}

.btn-load-more:hover {
  background: #e53637;
  color: #ffffff;
}

.product__item:hover .product__item__text h6,
.product__item__text h6 {
  opacity: 1 !important;
  visibility: visible !important;
}
</style>
