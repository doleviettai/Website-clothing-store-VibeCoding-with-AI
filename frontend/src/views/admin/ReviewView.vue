<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import * as reviewApi from '@/api/reviewApi'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'

// ─── State ────────────────────────────────────────────────────
const reviews = ref([])
const isLoading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

// Phân trang & Lọc
const filters = reactive({
  keyword: '',
  status: '',
  rating: '',
  page: 0,
  size: 10,
})

const pagination = reactive({
  totalPages: 0,
  totalElements: 0,
})

// Modal xem chi tiết
const selectedReview = ref(null)
const isDetailModalOpen = ref(false)

// ─── Methods ──────────────────────────────────────────────────

// Tải danh sách Bình luận đánh giá cho Bảng (Table)
const fetchReviews = async () => {
  isLoading.value = true
  errorMessage.value = ''
  try {
    const res = await reviewApi.getAdminReviews({
      keyword: filters.keyword || undefined,
      status: filters.status || undefined,
      rating: filters.rating ? Number(filters.rating) : undefined,
      page: filters.page,
      size: filters.size,
    })
    const data = res.data.data
    reviews.value = data.content
    pagination.totalPages = data.totalPages
    pagination.totalElements = data.totalElements
  } catch (err) {
    errorMessage.value = err.response?.data?.message || 'Không thể tải danh sách đánh giá.'
  } finally {
    isLoading.value = false
  }
}

// Ẩn / Hiện bình luận đánh giá
const handleToggleStatus = async (review) => {
  const nextStatus = review.status === 'VISIBLE' ? 'HIDDEN' : 'VISIBLE'
  const actionText = nextStatus === 'HIDDEN' ? 'ẨN' : 'HIỂN THỊ'
  
  if (confirm(`Bạn có chắc chắn muốn ${actionText} bình luận đánh giá này?`)) {
    try {
      await reviewApi.toggleReviewStatus(review.id, nextStatus)
      successMessage.value = `Đã ${actionText.toLowerCase()} bình luận thành công!`
      await fetchReviews()
      setTimeout(() => {
        successMessage.value = ''
      }, 3000)
    } catch (err) {
      alert(err.response?.data?.message || 'Không thể thay đổi trạng thái bình luận.')
    }
  }
}

// Xóa bình luận đánh giá
const handleDeleteReview = async (review) => {
  if (confirm(`Bạn có chắc chắn muốn xóa bình luận đánh giá của "${review.userFullName}"?`)) {
    try {
      await reviewApi.deleteReview(review.id)
      successMessage.value = 'Đã xóa bình luận đánh giá thành công!'
      await fetchReviews()
      setTimeout(() => {
        successMessage.value = ''
      }, 3000)
    } catch (err) {
      alert(err.response?.data?.message || 'Không thể xóa bình luận này.')
    }
  }
}

// Mở modal xem chi tiết bình luận
const openDetailModal = (review) => {
  selectedReview.value = review
  isDetailModalOpen.value = true
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return new Intl.DateTimeFormat('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date)
}

// Phân trang
const changePage = (newPage) => {
  if (newPage >= 0 && newPage < pagination.totalPages) {
    filters.page = newPage
    fetchReviews()
  }
}

// ─── AJAX Live Search (Tự động nạp dữ liệu khi nhập từ khóa) ───
let searchDebounceTimer = null

watch(
  () => filters.keyword,
  () => {
    clearTimeout(searchDebounceTimer)
    searchDebounceTimer = setTimeout(() => {
      filters.page = 0
      fetchReviews()
    }, 300)
  }
)

onMounted(() => {
  fetchReviews()
})
</script>

<template>
  <div class="admin-review-page">
    <!-- Header Trực quan -->
    <div class="d-flex align-items-center justify-content-between mb-4 flex-wrap gap-3">
      <div>
        <h3 class="page-title">Quản Lý Đánh Giá & Bình Luận</h3>
        <p class="text-muted mb-0">Quản lý phản hồi đánh giá sản phẩm từ người dùng, thực hiện xem và ẩn/hiện bình luận</p>
      </div>
    </div>

    <!-- Alert thông báo -->
    <div v-if="successMessage" class="alert alert-success alert-dismissible fade show mb-4" role="alert">
      <i class="fa fa-check-circle mr-2"></i> {{ successMessage }}
    </div>
    <div v-if="errorMessage" class="alert alert-danger alert-dismissible fade show mb-4" role="alert">
      <i class="fa fa-exclamation-circle mr-2"></i> {{ errorMessage }}
    </div>

    <!-- Thanh Tìm kiếm & Bộ lọc (AJAX Live Search) -->
    <div class="card border-0 shadow-sm mb-4">
      <div class="card-body">
        <div class="row g-3 align-items-center">
          <div class="col-md-5">
            <div class="input-group">
              <span class="input-group-text bg-white border-end-0">
                <i class="fa fa-search text-muted"></i>
              </span>
              <input
                v-model="filters.keyword"
                type="text"
                class="form-control border-start-0"
                placeholder="Nhập tên sản phẩm, tên người dùng, email hoặc nội dung bình luận (Live Search)..."
              />
            </div>
          </div>
          <div class="col-md-3">
            <select v-model="filters.rating" class="form-control" @change="fetchReviews">
              <option value="">-- Tất cả số sao rating --</option>
              <option value="5">⭐⭐⭐⭐⭐ (5 Sao)</option>
              <option value="4">⭐⭐⭐⭐ (4 Sao)</option>
              <option value="3">⭐⭐⭐ (3 Sao)</option>
              <option value="2">⭐⭐ (2 Sao)</option>
              <option value="1">⭐ (1 Sao)</option>
            </select>
          </div>
          <div class="col-md-2">
            <select v-model="filters.status" class="form-control" @change="fetchReviews">
              <option value="">-- Trạng thái --</option>
              <option value="VISIBLE">VISIBLE (Đang hiển thị)</option>
              <option value="HIDDEN">HIDDEN (Đã ẩn)</option>
            </select>
          </div>
          <div class="col-md-2">
            <button class="btn btn-light border w-100" @click="filters.keyword = ''; filters.rating = ''; filters.status = ''; fetchReviews()">
              <i class="fa fa-refresh mr-1"></i> Làm mới
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Bảng Đánh Giá & Bình Luận (Table) -->
    <div class="card border-0 shadow-sm">
      <div class="card-body p-0">
        <LoadingSpinner v-if="isLoading" text="Đang tải danh sách đánh giá bình luận..." />

        <div v-else class="table-responsive">
          <table class="table table-hover align-middle mb-0 text-center">
            <thead class="bg-light text-secondary">
              <tr>
                <th style="width: 60px;">ID</th>
                <th style="width: 80px;">Ảnh SP</th>
                <th class="text-left" style="min-width: 180px;">Tên Sản Phẩm</th>
                <th class="text-left" style="min-width: 180px;">Người Đánh Giá</th>
                <th style="width: 120px;">Đánh Giá Sao</th>
                <th class="text-left" style="min-width: 220px;">Nội Dung Bình Luận</th>
                <th style="width: 150px;">Thời Gian</th>
                <th style="width: 110px;">Trạng Thái</th>
                <th style="width: 140px;">Hành Động</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="review in reviews" :key="review.id">
                <td class="font-weight-bold text-muted">#{{ review.id }}</td>
                <!-- Ảnh tượng trưng sản phẩm -->
                <td>
                  <div class="thumb-box">
                    <img
                      v-if="review.productThumbnailUrl"
                      :src="review.productThumbnailUrl"
                      :alt="review.productName"
                      class="thumb-img"
                    />
                    <div v-else class="thumb-placeholder">
                      <i class="fa fa-shopping-bag"></i>
                    </div>
                  </div>
                </td>
                <td class="text-left">
                  <span class="font-weight-bold text-dark d-block">{{ review.productName || 'Sản phẩm #' + review.productId }}</span>
                </td>
                <!-- Người đánh giá -->
                <td class="text-left">
                  <div class="d-flex align-items-center gap-2">
                    <div class="user-avatar-small">
                      <img v-if="review.userAvatarUrl" :src="review.userAvatarUrl" alt="Avatar" class="avatar-img" />
                      <i v-else class="fa fa-user"></i>
                    </div>
                    <div>
                      <span class="font-weight-bold text-dark d-block">{{ review.userFullName || 'Khách hàng' }}</span>
                      <small class="text-muted">{{ review.userEmail }}</small>
                    </div>
                  </div>
                </td>
                <!-- Đánh giá Sao -->
                <td>
                  <div class="text-warning font-weight-bold">
                    <i v-for="star in review.rating" :key="star" class="fa fa-star"></i>
                    <i v-for="emptyStar in (5 - review.rating)" :key="'empty-' + emptyStar" class="fa fa-star-o text-muted"></i>
                  </div>
                </td>
                <!-- Nội dung bình luận -->
                <td class="text-left">
                  <p class="mb-0 text-dark comment-text-truncate" :title="review.content">
                    {{ review.content || '(Không có nội dung nhận xét)' }}
                  </p>
                </td>
                <!-- Thời gian -->
                <td>
                  <small class="text-muted d-block">{{ formatDate(review.createdAt) }}</small>
                </td>
                <!-- Trạng thái (HIỂN THỊ / ĐÃ ẨN) -->
                <td>
                  <span
                    class="badge px-2 py-1"
                    :class="review.status === 'VISIBLE' ? 'bg-success text-white' : 'bg-secondary text-white'"
                  >
                    {{ review.status === 'VISIBLE' ? 'HIỂN THỊ' : 'ĐÃ ẨN' }}
                  </span>
                </td>
                <!-- Hành động (Xem chi tiết, Ẩn/Hiện, Xóa) -->
                <td>
                  <button class="btn btn-sm btn-outline-primary mr-1" @click="openDetailModal(review)" title="Xem Chi Tiết">
                    <i class="fa fa-eye"></i>
                  </button>
                  <button
                    class="btn btn-sm mr-1"
                    :class="review.status === 'VISIBLE' ? 'btn-outline-warning' : 'btn-outline-success'"
                    @click="handleToggleStatus(review)"
                    :title="review.status === 'VISIBLE' ? 'Ẩn Bình Luận' : 'Hiện Bình Luận'"
                  >
                    <i class="fa" :class="review.status === 'VISIBLE' ? 'fa-eye-slash' : 'fa-check'"></i>
                  </button>
                  <button class="btn btn-sm btn-outline-danger" @click="handleDeleteReview(review)" title="Xóa">
                    <i class="fa fa-trash"></i>
                  </button>
                </td>
              </tr>
              <tr v-if="reviews.length === 0">
                <td colspan="9" class="text-center py-5 text-muted">
                  <i class="fa fa-comments-o fa-2x mb-2 d-block"></i>
                  Không tìm thấy bình luận đánh giá nào phù hợp.
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Footer Phân Trang -->
      <div v-if="pagination.totalPages > 1" class="card-footer bg-white d-flex justify-content-between align-items-center py-3">
        <small class="text-muted">Trang {{ filters.page + 1 }} / {{ pagination.totalPages }} (Tổng {{ pagination.totalElements }} bình luận)</small>
        <ul class="pagination pagination-sm m-0">
          <li class="page-item" :class="{ disabled: filters.page === 0 }">
            <button class="page-link" @click="changePage(filters.page - 1)">Trước</button>
          </li>
          <li
            v-for="p in pagination.totalPages"
            :key="p"
            class="page-item"
            :class="{ active: filters.page === p - 1 }"
          >
            <button class="page-link" @click="changePage(p - 1)">{{ p }}</button>
          </li>
          <li class="page-item" :class="{ disabled: filters.page === pagination.totalPages - 1 }">
            <button class="page-link" @click="changePage(filters.page + 1)">Sau</button>
          </li>
        </ul>
      </div>
    </div>

    <!-- Modal Xem Chi Tiết Bình Luận -->
    <div v-if="isDetailModalOpen && selectedReview" class="modal-overlay" @click.self="isDetailModalOpen = false">
      <div class="modal-box">
        <div class="modal-header-custom">
          <h5 class="m-0 font-weight-bold">
            <i class="fa fa-commenting mr-2 text-primary"></i>
            Chi Tiết Đánh Giá Sản Phẩm #{{ selectedReview.id }}
          </h5>
          <button class="close-btn" @click="isDetailModalOpen = false">&times;</button>
        </div>

        <div class="modal-body-custom">
          <!-- Sản phẩm -->
          <div class="detail-section mb-4">
            <h6 class="text-uppercase text-muted font-weight-bold small mb-2">Sản Phẩm Được Đánh Giá</h6>
            <div class="d-flex align-items-center gap-3">
              <img
                v-if="selectedReview.productThumbnailUrl"
                :src="selectedReview.productThumbnailUrl"
                class="detail-product-img"
              />
              <div>
                <h6 class="m-0 font-weight-bold text-dark">{{ selectedReview.productName }}</h6>
                <small class="text-muted">Mã SP: #{{ selectedReview.productId }}</small>
              </div>
            </div>
          </div>

          <!-- Người đánh giá -->
          <div class="detail-section mb-4">
            <h6 class="text-uppercase text-muted font-weight-bold small mb-2">Người Thực Hiện Đánh Giá</h6>
            <div class="d-flex align-items-center gap-3">
              <div class="user-avatar-large">
                <img v-if="selectedReview.userAvatarUrl" :src="selectedReview.userAvatarUrl" class="avatar-img" />
                <i v-else class="fa fa-user"></i>
              </div>
              <div>
                <h6 class="m-0 font-weight-bold text-dark">{{ selectedReview.userFullName }}</h6>
                <small class="text-muted">{{ selectedReview.userEmail }}</small>
              </div>
            </div>
          </div>

          <!-- Số sao & Thời gian -->
          <div class="row mb-4">
            <div class="col-6">
              <h6 class="text-uppercase text-muted font-weight-bold small mb-1">Mức Đánh Giá</h6>
              <div class="text-warning font-weight-bold fs-5">
                <i v-for="star in selectedReview.rating" :key="star" class="fa fa-star"></i>
                <i v-for="emptyStar in (5 - selectedReview.rating)" :key="'empty-' + emptyStar" class="fa fa-star-o text-muted"></i>
                <span class="ml-2 text-dark font-weight-bold">({{ selectedReview.rating }}/5 Sao)</span>
              </div>
            </div>
            <div class="col-6">
              <h6 class="text-uppercase text-muted font-weight-bold small mb-1">Thời Gian Đăng</h6>
              <span class="text-dark font-weight-bold">{{ formatDate(selectedReview.createdAt) }}</span>
            </div>
          </div>

          <!-- Nội dung chi tiết bình luận -->
          <div class="detail-section">
            <h6 class="text-uppercase text-muted font-weight-bold small mb-2">Nội Dung Bình Luận Chi Tiết</h6>
            <div class="comment-full-box">
              {{ selectedReview.content || '(Người dùng không để lại nội dung nhận xét bằng chữ)' }}
            </div>
          </div>
        </div>

        <div class="modal-footer-custom">
          <button
            class="btn mr-2"
            :class="selectedReview.status === 'VISIBLE' ? 'btn-warning' : 'btn-success'"
            @click="handleToggleStatus(selectedReview); isDetailModalOpen = false"
          >
            <i class="fa mr-1" :class="selectedReview.status === 'VISIBLE' ? 'fa-eye-slash' : 'fa-check'"></i>
            {{ selectedReview.status === 'VISIBLE' ? 'ẨN BÌNH LUẬN NÀY' : 'HIỆN BÌNH LUẬN NÀY' }}
          </button>
          <button class="btn btn-secondary" @click="isDetailModalOpen = false">Đóng Window</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: #111827;
}

/* Product & Avatar Thumbnail */
.thumb-box {
  width: 44px;
  height: 44px;
  border-radius: 6px;
  overflow: hidden;
  margin: 0 auto;
  border: 1px solid #e2e8f0;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.thumb-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.thumb-placeholder {
  width: 100%;
  height: 100%;
  background: #f1f5f9;
  color: #94a3b8;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}

.user-avatar-small {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  overflow: hidden;
  border: 1px solid #e2e8f0;
  background: #f1f5f9;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #64748b;
  flex-shrink: 0;
}

.user-avatar-large {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  overflow: hidden;
  border: 1px solid #e2e8f0;
  background: #f1f5f9;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #64748b;
  flex-shrink: 0;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.comment-text-truncate {
  max-width: 250px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.comment-full-box {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 16px;
  font-size: 14px;
  line-height: 1.6;
  color: #1e293b;
  white-space: pre-wrap;
}

.detail-product-img {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  object-fit: cover;
  border: 1px solid #e2e8f0;
}

/* Modal Overlay & Box */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(3px);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.modal-box {
  background: #ffffff;
  border-radius: 12px;
  width: 100%;
  max-width: 600px;
  box-shadow: 0 20px 25px -5px rgba(0,0,0,0.1), 0 10px 10px -5px rgba(0,0,0,0.04);
  overflow: hidden;
}

.modal-header-custom {
  padding: 16px 24px;
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.modal-body-custom {
  padding: 24px;
  max-height: 75vh;
  overflow-y: auto;
}

.modal-footer-custom {
  padding: 16px 24px;
  background: #f8fafc;
  border-top: 1px solid #e2e8f0;
  display: flex;
  justify-content: flex-end;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  color: #64748b;
  cursor: pointer;
  line-height: 1;
}

.close-btn:hover {
  color: #0f172a;
}
</style>
