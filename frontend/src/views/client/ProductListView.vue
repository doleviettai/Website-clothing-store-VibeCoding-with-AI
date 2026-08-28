<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import ProductSearch from '@/components/shop/ProductSearch.vue'
import CategoryFilter from '@/components/shop/CategoryFilter.vue'
import BrandFilter from '@/components/shop/BrandFilter.vue'
import ProductSort from '@/components/shop/ProductSort.vue'
import ProductGrid from '@/components/shop/ProductGrid.vue'
import * as productApi from '@/api/productApi'
import * as categoryApi from '@/api/categoryApi'
import * as brandApi from '@/api/brandApi'

// ─── State Quản Lý Trang Cửa Hàng (ShopView) ─────────────────────
const products = ref([])
const categories = ref([])
const brands = ref([])

const keyword = ref('')
const selectedCategoryId = ref(null)
const selectedBrandId = ref(null)
const sort = ref('newest')

const loading = ref(false)
const error = ref('')

const pagination = reactive({
  page: 0,
  size: 9,
  totalPages: 0,
  totalElements: 0,
})

// ─── Tải Danh Sách Chuyên Mục & Thương Hiệu Động Từ Database API ───
const fetchFilterOptions = async () => {
  try {
    const [resCat, resBrand] = await Promise.all([
      categoryApi.getCategoryDropdown(),
      brandApi.getBrandDropdown()
    ])
    categories.value = resCat.data?.data || []
    brands.value = resBrand.data?.data || []
  } catch {
    //
  }
}

// ─── Hàm Tải Danh Sách Sản Phẩm (Realtime AJAX không reload trang) ───
const loadProducts = async () => {
  loading.value = true
  error.value = ''
  try {
    const params = {
      keyword: keyword.value.trim() || undefined,
      categoryId: selectedCategoryId.value || undefined,
      brandId: selectedBrandId.value || undefined,
      sort: sort.value || 'newest',
      page: pagination.page,
      size: pagination.size,
    }

    const res = await productApi.getClientProducts(params)
    const pageData = res.data?.data

    if (pageData) {
      products.value = pageData.content || []
      pagination.totalPages = pageData.totalPages || 0
      pagination.totalElements = pageData.totalElements || 0
    } else {
      products.value = []
      pagination.totalPages = 0
      pagination.totalElements = 0
    }
  } catch (err) {
    error.value = err.response?.data?.message || 'Không thể kết nối đến máy chủ. Vui lòng thử lại sau.'
    products.value = []
  } finally {
    loading.value = false
  }
}

// ─── Xử lý Sự Kiện Tương Tác Cửa Hàng ─────────────────────────────

// Tìm kiếm realtime (khi debounce 400ms bên ProductSearch phát sự kiện search)
const handleSearch = (newKeyword) => {
  keyword.value = newKeyword
  pagination.page = 0
  loadProducts()
}

// Chọn Chuyên mục
const handleSelectCategory = (catId) => {
  selectedCategoryId.value = catId
  pagination.page = 0
  loadProducts()
}

// Chọn Thương hiệu
const handleSelectBrand = (brandId) => {
  selectedBrandId.value = brandId
  pagination.page = 0
  loadProducts()
}

// Đổi kiểu Sắp xếp (Sort)
const handleChangeSort = (newSort) => {
  sort.value = newSort
  pagination.page = 0
  loadProducts()
}

// Đổi trang AJAX (Pagination)
const handleChangePage = (newPage) => {
  if (newPage >= 0 && newPage < pagination.totalPages) {
    pagination.page = newPage
    loadProducts()
  }
}

// Xóa tất cả bộ lọc (Reset Filters)
const resetFilters = () => {
  keyword.value = ''
  selectedCategoryId.value = null
  selectedBrandId.value = null
  sort.value = 'newest'
  pagination.page = 0
  loadProducts()
}

onMounted(() => {
  fetchFilterOptions()
  loadProducts()
})
</script>

<template>
  <div class="shop-view-page py-4">
    <!-- Breadcrumb Header -->
    <div class="bg-light py-3 mb-4 border-bottom">
      <div class="container">
        <div class="d-flex align-items-center justify-content-between">
          <h4 class="font-weight-bold text-dark m-0">
            <i class="fa fa-shopping-bag text-danger mr-2"></i> CỬA HÀNG THỜI TRANG (SHOP)
          </h4>
          <nav aria-label="breadcrumb">
            <ol class="breadcrumb m-0 bg-transparent p-0">
              <li class="breadcrumb-item"><RouterLink to="/" class="text-muted">Trang chủ</RouterLink></li>
              <li class="breadcrumb-item active text-dark font-weight-bold" aria-current="page">Shop</li>
            </ol>
          </nav>
        </div>
      </div>
    </div>

    <!-- Main Container 2 Cột -->
    <div class="container">
      <div class="row">
        <!-- Sidebar Bên Trái (Bộ Lọc, Tìm Kiếm, Chuyên Mục, Thương Hiệu) -->
        <div class="col-lg-3 col-md-4 mb-4">
          <div class="shop-sidebar bg-white p-3 border rounded shadow-sm">
            <!-- 1. Search Realtime Input -->
            <ProductSearch
              v-model="keyword"
              @search="handleSearch"
            />

            <!-- 2. Category Filter (Nạp động từ DB API) -->
            <CategoryFilter
              :categories="categories"
              :selectedCategoryId="selectedCategoryId"
              @selectCategory="handleSelectCategory"
            />

            <!-- 3. Brand Filter (Nạp động từ DB API) -->
            <BrandFilter
              :brands="brands"
              :selectedBrandId="selectedBrandId"
              @selectBrand="handleSelectBrand"
            />

            <!-- 4. Reset Filters Button -->
            <button class="btn btn-outline-secondary w-100 font-weight-bold mt-2" @click="resetFilters">
              <i class="fa fa-refresh mr-1"></i> Xóa tất cả bộ lọc
            </button>
          </div>
        </div>

        <!-- Cột Chính Bên Phải (Header Bar, Sort, Product Grid) -->
        <div class="col-lg-9 col-md-8">
          <!-- Top Bar: Counter & Product Sort -->
          <div class="shop-top-bar bg-white p-3 border rounded shadow-sm mb-4 d-flex justify-content-between align-items-center flex-wrap gap-3">
            <div>
              <span class="text-muted small">
                Hiển thị <strong class="text-dark">{{ products.length }}</strong> / tổng số <strong class="text-danger">{{ pagination.totalElements }}</strong> sản phẩm
              </span>
            </div>
            <!-- Product Sort Dropdown -->
            <ProductSort
              v-model="sort"
              @changeSort="handleChangeSort"
            />
          </div>

          <!-- Product Grid Component -->
          <ProductGrid
            :products="products"
            :loading="loading"
            :error="error"
            :pagination="pagination"
            @changePage="handleChangePage"
            @retry="loadProducts"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.shop-sidebar {
  position: sticky;
  top: 90px;
}
</style>
