<script setup>
import ProductCard from './ProductCard.vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'

const props = defineProps({
  products: {
    type: Array,
    default: () => [],
  },
  loading: {
    type: Boolean,
    default: false,
  },
  error: {
    type: String,
    default: '',
  },
  pagination: {
    type: Object,
    default: () => ({
      page: 0,
      totalPages: 0,
      totalElements: 0,
    }),
  },
})

const emit = defineEmits(['changePage', 'retry'])
</script>

<template>
  <div class="product-grid-wrapper">
    <!-- State 1: Loading Spinner -->
    <div v-if="loading" class="py-5 text-center">
      <LoadingSpinner text="Đang tải danh sách sản phẩm cửa hàng..." />
    </div>

    <!-- State 2: Error Handling -->
    <div v-else-if="error" class="alert alert-danger py-4 text-center my-4 shadow-sm" role="alert">
      <i class="fa fa-exclamation-triangle fa-2x mb-2 d-block text-danger"></i>
      <h6 class="font-weight-bold">Không thể nạp dữ liệu sản phẩm</h6>
      <p class="mb-3 small">{{ error }}</p>
      <button class="btn btn-sm btn-outline-danger" @click="emit('retry')">
        <i class="fa fa-refresh mr-1"></i> Thử lại
      </button>
    </div>

    <!-- State 3: Empty Result (Không tìm thấy sản phẩm phù hợp) -->
    <div v-else-if="products.length === 0" class="py-5 text-center my-4 bg-light rounded shadow-sm">
      <i class="fa fa-shopping-bag fa-3x text-muted mb-3 d-block"></i>
      <h5 class="font-weight-bold text-dark mb-2">Không tìm thấy sản phẩm nào phù hợp</h5>
      <p class="text-muted small">Hãy thử điều chỉnh từ khóa tìm kiếm hoặc chọn chuyên mục/thương hiệu khác.</p>
    </div>

    <!-- State 4: Render Product Grid -->
    <div v-else>
      <div class="row row-cols-1 row-cols-sm-2 row-cols-md-3 g-4">
        <div v-for="product in products" :key="product.id" class="col mb-4">
          <ProductCard :product="product" />
        </div>
      </div>

      <!-- AJAX Pagination -->
      <div v-if="pagination.totalPages > 1" class="d-flex justify-content-between align-items-center mt-4 pt-3 border-top">
        <small class="text-muted">
          Trang {{ pagination.page + 1 }} / {{ pagination.totalPages }} (Tổng {{ pagination.totalElements }} sản phẩm)
        </small>
        <ul class="pagination pagination-sm m-0">
          <li class="page-item" :class="{ disabled: pagination.page === 0 }">
            <button class="page-link" @click="emit('changePage', pagination.page - 1)">Trước</button>
          </li>
          <li
            v-for="p in pagination.totalPages"
            :key="p"
            class="page-item"
            :class="{ active: pagination.page === p - 1 }"
          >
            <button class="page-link" @click="emit('changePage', p - 1)">{{ p }}</button>
          </li>
          <li class="page-item" :class="{ disabled: pagination.page === pagination.totalPages - 1 }">
            <button class="page-link" @click="emit('changePage', pagination.page + 1)">Sau</button>
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<style scoped>
.pagination .page-item.active .page-link {
  background-color: #111111;
  border-color: #111111;
  color: #ffffff;
}
.pagination .page-link {
  color: #111111;
  cursor: pointer;
}
</style>
