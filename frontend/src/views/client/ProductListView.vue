<script setup>
import { ref, computed } from 'vue'

const selectedCategory = ref('all')
const selectedBrand = ref('all')
const selectedSort = ref('newest')
const searchKeyword = ref('')

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
                <span>Cửa hàng</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
    <!-- Breadcrumb End -->

    <!-- Shop Section Begin -->
    <section class="shop spad">
      <div class="container">
        <div class="row">
          <!-- Sidebar bộ lọc -->
          <div class="col-lg-3">
            <div class="shop__sidebar">
              <div class="shop__sidebar__search">
                <form @submit.prevent>
                  <input v-model="searchKeyword" type="text" placeholder="Tìm kiếm sản phẩm...">
                  <button type="submit"><span class="icon_search"></span></button>
                </form>
              </div>

              <div class="shop__sidebar__accordion">
                <div class="accordion" id="accordionExample">
                  <!-- Danh mục -->
                  <div class="card">
                    <div class="card-heading">
                      <a data-toggle="collapse" data-target="#collapseOne">Chuyên Mục</a>
                    </div>
                    <div id="collapseOne" class="collapse show" data-parent="#accordionExample">
                      <div class="card-body">
                        <div class="shop__sidebar__categories">
                          <ul class="nice-scroll">
                            <li v-for="cat in categories" :key="cat.id">
                              <a
                                href="#"
                                :class="{ active: selectedCategory === cat.id }"
                                @click.prevent="selectedCategory = cat.id"
                              >
                                {{ cat.name }} ({{ cat.count }})
                              </a>
                            </li>
                          </ul>
                        </div>
                      </div>
                    </div>
                  </div>

                  <!-- Thương hiệu -->
                  <div class="card">
                    <div class="card-heading">
                      <a data-toggle="collapse" data-target="#collapseTwo">Thương Hiệu</a>
                    </div>
                    <div id="collapseTwo" class="collapse show" data-parent="#accordionExample">
                      <div class="card-body">
                        <div class="shop__sidebar__brand">
                          <ul>
                            <li v-for="brand in brands" :key="brand.id">
                              <a
                                href="#"
                                :class="{ active: selectedBrand === brand.id }"
                                @click.prevent="selectedBrand = brand.id"
                              >
                                {{ brand.name }}
                              </a>
                            </li>
                          </ul>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

            </div>
          </div>

          <!-- Danh sách sản phẩm -->
          <div class="col-lg-9">
            <div class="shop__product__option">
              <div class="row">
                <div class="col-lg-6 col-md-6 col-sm-6">
                  <div class="shop__product__option__left">
                    <p>Hiển thị {{ filteredProducts.length }} trên tổng số {{ allProducts.length }} sản phẩm</p>
                  </div>
                </div>
                <div class="col-lg-6 col-md-6 col-sm-6">
                  <div class="shop__product__option__right">
                    <p>Sắp xếp theo:</p>
                    <select v-model="selectedSort" class="form-control d-inline-block w-auto border-0">
                      <option value="newest">Mới nhất</option>
                      <option value="price-asc">Giá: Thấp đến Cao</option>
                      <option value="price-desc">Giá: Cao đến Thấp</option>
                    </select>
                  </div>
                </div>
              </div>
            </div>

            <!-- Grid sản phẩm -->
            <div class="row">
              <div
                v-for="product in filteredProducts"
                :key="product.id"
                class="col-lg-4 col-md-6 col-sm-6"
              >
                <div class="product__item">
                  <div class="product__item__pic set-bg" :style="{ backgroundImage: `url(${product.image})` }">
                    <span v-if="product.isNew" class="label">New</span>
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

            <!-- Phân trang -->
            <div class="row">
              <div class="col-lg-12">
                <div class="product__pagination">
                  <a class="active" href="#">1</a>
                  <a href="#">2</a>
                  <a href="#">3</a>
                  <span>...</span>
                  <a href="#">10</a>
                </div>
              </div>
            </div>

          </div>
        </div>
      </div>
    </section>
    <!-- Shop Section End -->
  </div>
</template>

<style scoped>
.breadcrumb-option {
  background: #f3f2ee;
  padding: 40px 0;
}

.shop__sidebar__categories ul li a.active,
.shop__sidebar__brand ul li a.active {
  color: #e53637;
  font-weight: 700;
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
