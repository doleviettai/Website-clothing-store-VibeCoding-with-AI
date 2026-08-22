<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import * as bannerApi from '@/api/bannerApi'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'

// ─── State ────────────────────────────────────────────────────
const banners = ref([])
const isLoading = ref(false)
const isSubmitting = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

// Phân trang & Lọc
const filters = reactive({
  keyword: '',
  position: '',
  status: '',
  page: 0,
  size: 10,
})

const pagination = reactive({
  totalPages: 0,
  totalElements: 0,
})

// Modal State
const isModalOpen = ref(false)
const isEditing = ref(false)
const selectedImageFile = ref(null)
const imagePreviewUrl = ref('')

const form = reactive({
  id: null,
  title: '',
  slug: '',
  description: '',
  imageUrl: '',
  targetUrl: '',
  position: 'HOME_TOP',
  status: 'ACTIVE',
  sortOrder: 1,
})

const formErrors = reactive({
  title: '',
})

// Danh sách các vị trí Banner khả dụng
const bannerPositions = [
  { code: 'HOME_TOP', label: 'HOME_TOP (Slide Đầu Trang Chủ)' },
  { code: 'HOME_MIDDLE', label: 'HOME_MIDDLE (Quảng Cáo Giữa Trang Chủ)' },
  { code: 'CATEGORY_TOP', label: 'CATEGORY_TOP (Banner Đầu Cửa Hàng / Chuyên Mục)' },
  { code: 'CATEGORY_MIDDLE', label: 'CATEGORY_MIDDLE (Banner Quảng Cáo Giữa Cửa Hàng)' },
]

// ─── Methods ──────────────────────────────────────────────────

// Tải danh sách Banner cho Bảng (Table)
const fetchBanners = async () => {
  isLoading.value = true
  errorMessage.value = ''
  try {
    const res = await bannerApi.getAdminBanners({
      keyword: filters.keyword || undefined,
      position: filters.position || undefined,
      status: filters.status || undefined,
      page: filters.page,
      size: filters.size,
    })
    const data = res.data.data
    banners.value = data.content
    pagination.totalPages = data.totalPages
    pagination.totalElements = data.totalElements
  } catch (err) {
    errorMessage.value = err.response?.data?.message || 'Không thể tải danh sách banner.'
  } finally {
    isLoading.value = false
  }
}

// Tự tạo slug tiếng Việt
const slugify = (text) => {
  if (!text) return ''
  return text
    .toString()
    .toLowerCase()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/[đĐ]/g, 'd')
    .replace(/[^a-z0-9 -]/g, '')
    .trim()
    .replace(/\s+/g, '-')
    .replace(/-+/g, '-')
}

const onTitleChange = () => {
  if (!isEditing.value || !form.slug) {
    form.slug = slugify(form.title)
  }
}

// Xử lý chọn tệp ảnh banner
const handleImageSelect = (event) => {
  const file = event.target.files[0]
  if (file) {
    selectedImageFile.value = file
    imagePreviewUrl.value = URL.createObjectURL(file)
  }
}

// Mở modal thêm mới
const openAddModal = () => {
  isEditing.value = false
  form.id = null
  form.title = ''
  form.slug = ''
  form.description = ''
  form.imageUrl = ''
  form.targetUrl = ''
  form.position = 'HOME_TOP'
  form.status = 'ACTIVE'
  form.sortOrder = 1
  formErrors.title = ''
  selectedImageFile.value = null
  imagePreviewUrl.value = ''
  isModalOpen.value = true
}

// Mở modal chỉnh sửa
const openEditModal = (banner) => {
  isEditing.value = true
  form.id = banner.id
  form.title = banner.title
  form.slug = banner.slug
  form.description = banner.description || ''
  form.imageUrl = banner.imageUrl || ''
  form.targetUrl = banner.targetUrl || ''
  form.position = banner.position
  form.status = banner.status
  form.sortOrder = banner.sortOrder
  formErrors.title = ''
  selectedImageFile.value = null
  imagePreviewUrl.value = banner.imageUrl || ''
  isModalOpen.value = true
}

// Xử lý lưu banner
const handleSubmit = async () => {
  formErrors.title = ''
  if (!form.title.trim()) {
    formErrors.title = 'Tiêu đề banner không được để trống'
    return
  }

  isSubmitting.value = true
  errorMessage.value = ''

  try {
    const bannerData = {
      title: form.title.trim(),
      slug: form.slug.trim() || slugify(form.title),
      description: form.description,
      imageUrl: form.imageUrl,
      targetUrl: form.targetUrl,
      position: form.position,
      status: form.status,
      sortOrder: Number(form.sortOrder) || 1,
    }

    const formData = new FormData()
    formData.append(
      'banner',
      new Blob([JSON.stringify(bannerData)], { type: 'application/json' })
    )

    if (selectedImageFile.value) {
      formData.append('image', selectedImageFile.value)
    }

    if (isEditing.value) {
      await bannerApi.updateBanner(form.id, formData)
      successMessage.value = 'Cập nhật banner thành công!'
    } else {
      await bannerApi.createBanner(formData)
      successMessage.value = 'Tạo banner mới thành công!'
    }

    isModalOpen.value = false
    await fetchBanners()

    setTimeout(() => {
      successMessage.value = ''
    }, 3000)
  } catch (err) {
    errorMessage.value = err.response?.data?.message || 'Có lỗi xảy ra khi lưu banner.'
  } finally {
    isSubmitting.value = false
  }
}

// Xử lý xóa banner
const handleDelete = async (banner) => {
  if (confirm(`Bạn có chắc chắn muốn xóa banner "${banner.title}"?`)) {
    try {
      await bannerApi.deleteBanner(banner.id)
      successMessage.value = 'Đã xóa banner thành công!'
      await fetchBanners()
      setTimeout(() => {
        successMessage.value = ''
      }, 3000)
    } catch (err) {
      alert(err.response?.data?.message || 'Không thể xóa banner này.')
    }
  }
}

// Phân trang
const changePage = (newPage) => {
  if (newPage >= 0 && newPage < pagination.totalPages) {
    filters.page = newPage
    fetchBanners()
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
      fetchBanners()
    }, 300)
  }
)

onMounted(() => {
  fetchBanners()
})
</script>

<template>
  <div class="admin-banner-page">
    <!-- Header Trực quan -->
    <div class="d-flex align-items-center justify-content-between mb-4 flex-wrap gap-3">
      <div>
        <h3 class="page-title">Quản Lý Banner Quảng Cáo & Slide</h3>
        <p class="text-muted mb-0">Quản lý các banner slider trang chủ, banner giữa trang và thứ tự hiển thị</p>
      </div>
      <button class="btn btn-primary d-flex align-items-center gap-2" @click="openAddModal">
        <i class="fa fa-plus"></i>
        <span>Thêm Banner Mới</span>
      </button>
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
                placeholder="Nhập tiêu đề hoặc slug để tìm kiếm tức thì (Live Search AJAX)..."
              />
            </div>
          </div>
          <div class="col-md-3">
            <select v-model="filters.position" class="form-control" @change="fetchBanners">
              <option value="">-- Tất cả vị trí --</option>
              <option value="HOME_TOP">HOME_TOP (Slide Trang Chủ)</option>
              <option value="HOME_MIDDLE">HOME_MIDDLE (Quảng Cáo Giữa Trang Chủ)</option>
              <option value="CATEGORY_TOP">CATEGORY_TOP (Đầu Cửa Hàng)</option>
              <option value="CATEGORY_MIDDLE">CATEGORY_MIDDLE (Giữa Cửa Hàng)</option>
            </select>
          </div>
          <div class="col-md-2">
            <select v-model="filters.status" class="form-control" @change="fetchBanners">
              <option value="">-- Trạng thái --</option>
              <option value="ACTIVE">ACTIVE</option>
              <option value="INACTIVE">INACTIVE</option>
            </select>
          </div>
          <div class="col-md-2">
            <button class="btn btn-light border w-100" @click="filters.keyword = ''; filters.position = ''; filters.status = ''; fetchBanners()">
              <i class="fa fa-refresh mr-1"></i> Làm mới
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Bảng Banner (Table) -->
    <div class="card border-0 shadow-sm">
      <div class="card-body p-0">
        <LoadingSpinner v-if="isLoading" text="Đang tải danh sách banner..." />

        <div v-else class="table-responsive">
          <table class="table table-hover align-middle mb-0 text-center">
            <thead class="bg-light text-secondary">
              <tr>
                <th style="width: 70px;">ID</th>
                <th style="width: 140px;">Ảnh Banner</th>
                <th class="text-left">Tiêu Đề Banner</th>
                <th>Vị Trí</th>
                <th>Thứ Tự Slide</th>
                <th>Trạng Thái</th>
                <th style="width: 140px;">Thao Tác</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="banner in banners" :key="banner.id">
                <td class="font-weight-bold text-muted">#{{ banner.id }}</td>
                <!-- Ảnh tượng trưng Banner -->
                <td>
                  <div class="banner-img-box">
                    <img
                      v-if="banner.imageUrl"
                      :src="banner.imageUrl"
                      :alt="banner.title"
                      class="banner-img"
                    />
                    <div v-else class="banner-img-placeholder">
                      <i class="fa fa-image"></i>
                    </div>
                  </div>
                </td>
                <td class="text-left">
                  <span class="font-weight-bold text-dark d-block">{{ banner.title }}</span>
                  <code class="small">{{ banner.slug }}</code>
                </td>
                <td>
                  <span
                    class="badge"
                    :class="{
                      'bg-primary text-white': banner.position === 'HOME_TOP',
                      'bg-info text-white': banner.position === 'HOME_MIDDLE',
                      'bg-dark text-white': banner.position === 'CATEGORY_TOP',
                      'bg-secondary text-white': banner.position === 'CATEGORY_MIDDLE'
                    }"
                  >
                    {{ banner.position }}
                  </span>
                </td>
                <td>
                  <span class="badge bg-warning text-dark font-weight-bold">Slide {{ banner.sortOrder }}</span>
                </td>
                <td>
                  <span
                    class="badge"
                    :class="banner.status === 'ACTIVE' ? 'bg-success text-white' : 'bg-danger text-white'"
                  >
                    {{ banner.status === 'ACTIVE' ? 'HOẠT ĐỘNG' : 'VÔ HIỆU' }}
                  </span>
                </td>
                <td>
                  <button class="btn btn-sm btn-outline-info mr-2" @click="openEditModal(banner)" title="Sửa">
                    <i class="fa fa-pencil"></i>
                  </button>
                  <button class="btn btn-sm btn-outline-danger" @click="handleDelete(banner)" title="Xóa">
                    <i class="fa fa-trash"></i>
                  </button>
                </td>
              </tr>
              <tr v-if="banners.length === 0">
                <td colspan="7" class="text-center py-5 text-muted">
                  <i class="fa fa-file-image-o fa-2x mb-2 d-block"></i>
                  Không tìm thấy banner nào phù hợp.
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Footer Phân Trang -->
      <div v-if="pagination.totalPages > 1" class="card-footer bg-white d-flex justify-content-between align-items-center py-3">
        <small class="text-muted">Trang {{ filters.page + 1 }} / {{ pagination.totalPages }} (Tổng {{ pagination.totalElements }} banner)</small>
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

    <!-- Modal Thêm / Chỉnh Sửa Banner -->
    <div v-if="isModalOpen" class="modal-overlay" @click.self="isModalOpen = false">
      <div class="modal-box">
        <div class="modal-header-custom">
          <h5 class="m-0 font-weight-bold">
            <i class="fa mr-2" :class="isEditing ? 'fa-pencil' : 'fa-plus-circle'"></i>
            {{ isEditing ? 'Chỉnh Sửa Banner' : 'Thêm Banner Mới' }}
          </h5>
          <button class="close-btn" @click="isModalOpen = false">&times;</button>
        </div>

        <form @submit.prevent="handleSubmit">
          <div class="modal-body-custom">

            <!-- Tiêu đề & Slug -->
            <div class="row mb-3">
              <div class="col-md-6">
                <label class="form-label font-weight-bold">Tiêu Đề Banner <span class="text-danger">*</span></label>
                <input
                  v-model="form.title"
                  type="text"
                  class="form-control"
                  :class="{ 'is-invalid': formErrors.title }"
                  placeholder="Ví dụ: Bộ Sưu Tập Thu - Đông 2030"
                  @input="onTitleChange"
                />
                <div v-if="formErrors.title" class="invalid-feedback">{{ formErrors.title }}</div>
              </div>
              <div class="col-md-6">
                <label class="form-label font-weight-bold">Slug Đường Dẫn</label>
                <input
                  v-model="form.slug"
                  type="text"
                  class="form-control"
                  placeholder="bo-suu-tap-thu-dong-2030"
                />
              </div>
            </div>

            <!-- Vị trí & Thứ tự hiển thị -->
            <div class="row mb-3">
              <div class="col-md-6">
                <label class="form-label font-weight-bold">Vị Trí Hiển Thị <span class="text-danger">*</span></label>
                <select v-model="form.position" class="form-control" required>
                  <option v-for="pos in bannerPositions" :key="pos.code" :value="pos.code">
                    {{ pos.label }}
                  </option>
                </select>
                <small class="text-muted">Ví dụ: HOME_TOP là Slide chuyển động đầu trang chủ</small>
              </div>
              <div class="col-md-6">
                <label class="form-label font-weight-bold">Thứ Tự Slide (1, 2, 3...)</label>
                <input
                  v-model="form.sortOrder"
                  type="number"
                  class="form-control"
                  placeholder="1"
                  min="1"
                />
                <small class="text-muted">Thứ tự 1 hiển thị trước, thứ tự 2 hiển thị sau</small>
              </div>
            </div>

            <!-- Upload Ảnh Banner -->
            <div class="form-group mb-3">
              <label class="form-label font-weight-bold">Ảnh Banner Quảng Cáo (Upload Cloudinary)</label>
              <div class="d-flex align-items-center gap-3">
                <input
                  type="file"
                  accept="image/*"
                  class="form-control-file"
                  @change="handleImageSelect"
                />
                <div v-if="imagePreviewUrl" class="img-preview-box">
                  <img :src="imagePreviewUrl" alt="Preview Banner" class="img-preview" />
                </div>
              </div>
              <small class="text-muted">Ảnh sẽ được upload tự động lên thư mục Cloudinary: <code>FashionShop2/banners</code></small>
            </div>

            <!-- Target URL & Trạng thái -->
            <div class="row mb-3">
              <div class="col-md-6">
                <label class="form-label font-weight-bold">Đường Dẫn Liên Kết (Link khi nhấp)</label>
                <input
                  v-model="form.targetUrl"
                  type="text"
                  class="form-control"
                  placeholder="/products hoặc https://..."
                />
              </div>
              <div class="col-md-6">
                <label class="form-label font-weight-bold">Trạng Thái</label>
                <select v-model="form.status" class="form-control">
                  <option value="ACTIVE">HOẠT ĐỘNG (ACTIVE)</option>
                  <option value="INACTIVE">VÔ HIỆU (INACTIVE)</option>
                </select>
              </div>
            </div>

            <!-- Mô tả banner -->
            <div class="form-group mb-3">
              <label class="form-label font-weight-bold">Mô Tả Banner</label>
              <textarea
                v-model="form.description"
                rows="3"
                class="form-control"
                placeholder="Mô tả nội dung chương trình khuyến mãi hay thông điệp của banner..."
              ></textarea>
            </div>

          </div>

          <div class="modal-footer-custom">
            <button type="button" class="btn btn-secondary mr-2" @click="isModalOpen = false">Hủy Bỏ</button>
            <button type="submit" class="btn btn-primary d-flex align-items-center gap-2" :disabled="isSubmitting">
              <span v-if="isSubmitting" class="spinner-border spinner-border-sm"></span>
              <span>{{ isSubmitting ? 'ĐANG LƯU...' : 'LƯU BANNER' }}</span>
            </button>
          </div>
        </form>

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

/* Banner image style */
.banner-img-box {
  width: 110px;
  height: 52px;
  border-radius: 6px;
  overflow: hidden;
  margin: 0 auto;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
}

.banner-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.banner-img-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
  font-size: 20px;
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
  max-width: 650px;
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

.img-preview-box {
  width: 120px;
  height: 60px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e2e8f0;
}

.img-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
</style>
