<script setup>
import { ref } from 'vue'

const activeTab = ref('all')

const setTab = (tab) => {
  activeTab.value = tab
}

// Mockup danh sách sản phẩm hiển thị trên trang chủ
const products = ref([
  {
    id: 1,
    name: 'Áo Khoác Biker Piqué',
    category: 'new-arrivals',
    price: 1550000,
    originalPrice: 1800000,
    image: '/img/product/product-1.jpg',
    tag: 'New',
    rating: 5,
  },
  {
    id: 2,
    name: 'Áo Thun Basic Cotton Unisex',
    category: 'hot-sales',
    price: 350000,
    image: '/img/product/product-2.jpg',
    tag: 'Sale',
    rating: 4,
  },
  {
    id: 3,
    name: 'Áo Sơ Mi Nam Tay Dài Oxford',
    category: 'new-arrivals',
    price: 620000,
    image: '/img/product/product-3.jpg',
    rating: 5,
  },
  {
    id: 4,
    name: 'Quần Jean Nam Skinny Stretch',
    category: 'hot-sales',
    price: 890000,
    image: '/img/product/product-4.jpg',
    rating: 4,
  },
  {
    id: 5,
    name: 'Áo Polo Nam Thêu Logo',
    category: 'new-arrivals',
    price: 490000,
    image: '/img/product/product-5.jpg',
    tag: 'New',
    rating: 5,
  },
  {
    id: 6,
    name: 'Áo Khoác Blazer Nam Form Rộng',
    category: 'hot-sales',
    price: 2100000,
    image: '/img/product/product-6.jpg',
    rating: 5,
  },
  {
    id: 7,
    name: 'Quần Short Nam Thể Thao',
    category: 'new-arrivals',
    price: 280000,
    image: '/img/product/product-7.jpg',
    rating: 4,
  },
  {
    id: 8,
    name: 'Áo Hoodies Fleece Nỉ Bông',
    category: 'hot-sales',
    price: 750000,
    image: '/img/product/product-8.jpg',
    tag: 'Sale',
    rating: 5,
  },
])

const formatPrice = (value) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value)
}
</script>

<template>
  <div class="home-page">
    <!-- Hero Section Begin -->
    <section class="hero">
      <div class="hero__slider">
        <div class="hero__items set-bg" style="background-image: url('/img/hero/hero-1.jpg');">
          <div class="container">
            <div class="row">
              <div class="col-xl-5 col-lg-7 col-md-8">
                <div class="hero__text">
                  <h6>Bộ Sưu Tập Mới</h6>
                  <h2>Bộ Sưu Tập Thu - Đông 2030</h2>
                  <p>Thương hiệu chuyên sáng tạo các sản phẩm thời trang cao cấp. Được chế tác đạo đức với cam kết chất lượng vượt trội.</p>
                  <RouterLink to="/products" class="primary-btn">Khám phá ngay <span class="arrow_right"></span></RouterLink>
                  <div class="hero__social">
                    <a href="#"><i class="fa fa-facebook"></i></a>
                    <a href="#"><i class="fa fa-twitter"></i></a>
                    <a href="#"><i class="fa fa-pinterest"></i></a>
                    <a href="#"><i class="fa fa-instagram"></i></a>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
    <!-- Hero Section End -->

    <!-- Banner Section Begin -->
    <section class="banner spad">
      <div class="container">
        <div class="row">
          <div class="col-lg-7 offset-lg-4">
            <div class="banner__item">
              <div class="banner__item__pic">
                <img src="/img/banner/banner-1.jpg" alt="Bộ sưu tập Quần áo">
              </div>
              <div class="banner__item__text">
                <h2>Bộ Sưu Tập Quần Áo 2030</h2>
                <RouterLink to="/products">Mua ngay</RouterLink>
              </div>
            </div>
          </div>
          <div class="col-lg-5">
            <div class="banner__item banner__item--middle">
              <div class="banner__item__pic">
                <img src="/img/banner/banner-2.jpg" alt="Phụ kiện">
              </div>
              <div class="banner__item__text">
                <h2>Phụ Kiện Thời Trang</h2>
                <RouterLink to="/products">Mua ngay</RouterLink>
              </div>
            </div>
          </div>
          <div class="col-lg-7">
            <div class="banner__item banner__item--last">
              <div class="banner__item__pic">
                <img src="/img/banner/banner-3.jpg" alt="Giày Xuân">
              </div>
              <div class="banner__item__text">
                <h2>Bộ Sưu Tập Giày Xuân 2030</h2>
                <RouterLink to="/products">Mua ngay</RouterLink>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
    <!-- Banner Section End -->

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
            class="col-lg-3 col-md-6 col-sm-6"
          >
            <div class="product__item">
              <div class="product__item__pic set-bg" :style="{ backgroundImage: `url(${product.image})` }">
                <span v-if="product.tag" class="label" :class="{ 'sale-label': product.tag === 'Sale' }">{{ product.tag }}</span>
                <ul class="product__hover">
                  <li><a href="#"><img src="/img/icon/heart.png" alt="Yêu thích"></a></li>
                  <li><RouterLink :to="`/products`"><img src="/img/icon/search.png" alt="Chi tiết"></RouterLink></li>
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
    <!-- Product Section End -->

    <!-- Categories / Sale Section Begin -->
    <section class="categories spad">
      <div class="container">
        <div class="row">
          <div class="col-lg-3">
            <div class="categories__text">
              <h2>Mặc Đẹp <br /><span>Bộ Sưu Tập Giày</span> <br />Phụ Kiện</h2>
            </div>
          </div>
          <div class="col-lg-4">
            <div class="categories__hot__deal">
              <img src="/img/product-sale.png" alt="Hot Deal">
              <div class="hot__deal__sticker">
                <span>Giảm giá</span>
                <h5>750.000đ</h5>
              </div>
            </div>
          </div>
          <div class="col-lg-4 offset-lg-1">
            <div class="categories__deal__countdown">
              <span>Ưu Đãi Đặc Biệt</span>
              <h2>Túi Xách Da Cao Cấp</h2>
              <div class="categories__deal__countdown__timer">
                <div class="cd-item">
                  <span>30</span>
                  <p>Ngày</p>
                </div>
                <div class="cd-item">
                  <span>12</span>
                  <p>Giờ</p>
                </div>
                <div class="cd-item">
                  <span>45</span>
                  <p>Phút</p>
                </div>
                <div class="cd-item">
                  <span>18</span>
                  <p>Giây</p>
                </div>
              </div>
              <RouterLink to="/products" class="primary-btn">Mua ngay</RouterLink>
            </div>
          </div>
        </div>
      </div>
    </section>
    <!-- Categories Section End -->

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
    <!-- Instagram Section End -->
  </div>
</template>

<style scoped>
.hero__items {
  padding-top: 180px;
  padding-bottom: 140px;
  background-size: cover;
  background-position: center center;
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
