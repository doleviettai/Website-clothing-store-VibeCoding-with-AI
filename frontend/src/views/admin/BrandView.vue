<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import * as brandApi from '@/api/brandApi'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'

// ─── State ────────────────────────────────────────────────────
const brands = ref([])
const isLoading = ref(false)
const isSubmitting = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

// Phân trang & Lọc
const filters = reactive({
  keyword: '',
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
  name: '',
  slug: '',
  description: '',
  logoUrl: '',
  status: 'ACTIVE',
})

const formErrors = reactive({
  name: '',
})

// ─── Methods ──────────────────────────────────────────────────

// Tải danh sách thương hiệu cho Bảng (Table)
const fetchBrands = async () => {
  isLoading.value = true
  errorMessage.value = ''
  try {
    const res = await brandApi.getAdminBrands({
      keyword: filters.keyword || undefined,
      status: filters.status || undefined,
      page: filters.page,
      size: filters.size,
    })
    const data = res.data.data
    brands.value = data.content
    pagination.totalPages = data.totalPages
    pagination.totalElements = data.totalElements
  } catch (err) {
    errorMessage.value = err.response?.data?.message || 'Không thể tải danh sách thương hiệu.'
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

const onNameChange = () => {
  if (!isEditing.value || !form.slug) {
    form.slug = slugify(form.name)
  }
}

// Xử lý chọn tệp ảnh logo
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
  form.name = ''
  form.slug = ''
  form.description = ''
  form.logoUrl = ''
  form.status = 'ACTIVE'
  formErrors.name = ''
  selectedImageFile.value = null
  imagePreviewUrl.value = ''
  isModalOpen.value = true
}

// Mở modal chỉnh sửa
const openEditModal = (brand) => {
  isEditing.value = true
  form.id = brand.id
  form.name = brand.name
  form.slug = brand.slug
  form.description = brand.description || ''
  form.logoUrl = brand.logoUrl || ''
  form.status = brand.status
  formErrors.name = ''
  selectedImageFile.value = null
  imagePreviewUrl.value = brand.logoUrl || ''
  isModalOpen.value = true
}

// Xử lý lưu thương hiệu
const handleSubmit = async () => {
  formErrors.name = ''
  if (!form.name.trim()) {
    formErrors.name = 'Tên thương hiệu không được để trống'
    return
  }

  isSubmitting.value = true
  errorMessage.value = ''

  try {
    const brandData = {
      name: form.name.trim(),
      slug: form.slug.trim() || slugify(form.name),
      description: form.description,
      logoUrl: form.logoUrl,
      status: form.status,
    }

    const formData = new FormData()
    formData.append(
      'brand',
      new Blob([JSON.stringify(brandData)], { type: 'application/json' })
    )

    if (selectedImageFile.value) {
      formData.append('image', selectedImageFile.value)
    }

    if (isEditing.value) {
      await brandApi.updateBrand(form.id, formData)
      successMessage.value = 'Cập nhật thương hiệu thành công!'
    } else {
      await brandApi.createBrand(formData)
      successMessage.value = 'Tạo thương hiệu mới thành công!'
    }

    isModalOpen.value = false
    await fetchBrands()

    setTimeout(() => {
      successMessage.value = ''
    }, 3000)
  } catch (err) {
    errorMessage.value = err.response?.data?.message || 'Có lỗi xảy ra khi lưu thương hiệu.'
  } finally {
    isSubmitting.value = false
  }
}

// Xử lý xóa thương hiệu
const handleDelete = async (brand) => {
  if (confirm(`Bạn có chắc chắn muốn xóa thương hiệu "${brand.name}"?`)) {
    try {
      await brandApi.deleteBrand(brand.id)
      successMessage.value = 'Đã xóa thương hiệu thành công!'
      await fetchBrands()
      setTimeout(() => {
        successMessage.value = ''
      }, 3000)
    } catch (err) {
      alert(err.response?.data?.message || 'Không thể xóa thương hiệu này.')
    }
  }
}

// Phân trang
const changePage = (newPage) => {
  if (newPage >= 0 && newPage < pagination.totalPages) {
    filters.page = newPage
    fetchBrands()
  }
}

// ─── AJAX Live Search (Tự động tải dữ liệu theo thời gian thực mỗi khi nhập từ khóa) ───
let searchDebounceTimer = null

watch(
  () => filters.keyword,
  () => {
    clearTimeout(searchDebounceTimer)
    searchDebounceTimer = setTimeout(() => {
      filters.page = 0
      fetchBrands()
    }, 300)
  }
)

onMounted(() => {
  fetchBrands()
})
</script>

<template>
  <div class="admin-brand-page">
    <!-- Header Trực quan -->
    <div class="d-flex align-items-center justify-content-between mb-4 flex-wrap gap-3">
      <div>
        <h3 class="page-title">Quản Lý Thương Hiệu</h3>
        <p class="text-muted mb-0">Quản lý thương hiệu đối tác, logo tượng trưng và trạng thái hiển thị</p>
      </div>
      <button class="btn btn-primary d-flex align-items-center gap-2" @click="openAddModal">
        <i class="fa fa-plus"></i>
        <span>Thêm Thương Hiệu</span>
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
          <div class="col-md-6">
            <div class="input-group">
              <span class="input-group-text bg-white border-end-0">
                <i class="fa fa-search text-muted"></i>
              </span>
              <input
                v-model="filters.keyword"
                type="text"
                class="form-control border-start-0"
                placeholder="Nhập tên thương hiệu hoặc slug để tìm kiếm tức thì (Live Search AJAX)..."
              />
            </div>
          </div>
          <div class="col-md-4">
            <select v-model="filters.status" class="form-control" @change="fetchBrands">
              <option value="">-- Tất cả trạng thái --</option>
              <option value="ACTIVE">HOẠT ĐỘNG (ACTIVE)</option>
              <option value="INACTIVE">VÔ HIỆU (INACTIVE)</option>
            </select>
          </div>
          <div class="col-md-2">
            <button class="btn btn-light border w-100" @click="filters.keyword = ''; filters.status = ''; fetchBrands()">
              <i class="fa fa-refresh mr-1"></i> Làm mới
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Bảng Thương Hiệu (Table) -->
    <div class="card border-0 shadow-sm">
      <div class="card-body p-0">
        <LoadingSpinner v-if="isLoading" text="Đang tải danh sách thương hiệu..." />

        <div v-else class="table-responsive">
          <table class="table table-hover align-middle mb-0 text-center">
            <thead class="bg-light text-secondary">
              <tr>
                <th style="width: 70px;">ID</th>
                <th style="width: 100px;">Logo</th>
                <th class="text-left">Tên Thương Hiệu</th>
                <th class="text-left">Slug</th>
                <th>Mô Tả</th>
                <th>Trạng Thái</th>
                <th style="width: 140px;">Thao Tác</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="brand in brands" :key="brand.id">
                <td class="font-weight-bold text-muted">#{{ brand.id }}</td>
                <!-- Logo tượng trưng thương hiệu -->
                <td>
                  <div class="brand-logo-box">
                    <img
                      v-if="brand.logoUrl"
                      :src="brand.logoUrl"
                      :alt="brand.name"
                      class="brand-logo-img"
                    />
                    <div v-else class="brand-logo-placeholder">
                      <i class="fa fa-tag"></i>
                    </div>
                  </div>
                </td>
                <td class="text-left">
                  <span class="font-weight-bold text-dark d-block">{{ brand.name }}</span>
                </td>
                <td class="text-left">
                  <code>{{ brand.slug }}</code>
                </td>
                <td class="text-left" style="max-width: 250px;">
                  <span v-if="brand.description" class="text-muted small text-truncate d-inline-block" style="max-width: 240px;">
                    {{ brand.description }}
                  </span>
                  <span v-else class="text-muted small">-</span>
                </td>
                <td>
                  <span
                    class="badge"
                    :class="brand.status === 'ACTIVE' ? 'bg-success text-white' : 'bg-danger text-white'"
                  >
                    {{ brand.status === 'ACTIVE' ? 'HOẠT ĐỘNG' : 'VÔ HIỆU' }}
                  </span>
                </td>
                <td>
                  <button class="btn btn-sm btn-outline-info mr-2" @click="openEditModal(brand)" title="Sửa">
                    <i class="fa fa-pencil"></i>
                  </button>
                  <button class="btn btn-sm btn-outline-danger" @click="handleDelete(brand)" title="Xóa">
                    <i class="fa fa-trash"></i>
                  </button>
                </td>
              </tr>
              <tr v-if="brands.length === 0">
                <td colspan="7" class="text-center py-5 text-muted">
                  <i class="fa fa-tags fa-2x mb-2 d-block"></i>
                  Không tìm thấy thương hiệu nào phù hợp.
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Footer Phân Trang -->
      <div v-if="pagination.totalPages > 1" class="card-footer bg-white d-flex justify-content-between align-items-center py-3">
        <small class="text-muted">Trang {{ filters.page + 1 }} / {{ pagination.totalPages }} (Tổng {{ pagination.totalElements }} thương hiệu)</small>
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

    <!-- Modal Thêm / Chỉnh Sửa Thương Hiệu -->
    <div v-if="isModalOpen" class="modal-overlay" @click.self="isModalOpen = false">
      <div class="modal-box">
        <div class="modal-header-custom">
          <h5 class="m-0 font-weight-bold">
            <i class="fa mr-2" :class="isEditing ? 'fa-pencil' : 'fa-plus-circle'"></i>
            {{ isEditing ? 'Chỉnh Sửa Thương Hiệu' : 'Thêm Thương Hiệu Mới' }}
          </h5>
          <button class="close-btn" @click="isModalOpen = false">&times;</button>
        </div>

        <form @submit.prevent="handleSubmit">
          <div class="modal-body-custom">

            <!-- Tên thương hiệu & Slug -->
            <div class="row mb-3">
              <div class="col-md-6">
                <label class="form-label font-weight-bold">Tên Thương Hiệu <span class="text-danger">*</span></label>
                <input
                  v-model="form.name"
                  type="text"
                  class="form-control"
                  :class="{ 'is-invalid': formErrors.name }"
                  placeholder="Ví dụ: Nike, Adidas, Zara"
                  @input="onNameChange"
                />
                <div v-if="formErrors.name" class="invalid-feedback">{{ formErrors.name }}</div>
              </div>
              <div class="col-md-6">
                <label class="form-label font-weight-bold">Slug Đường Dẫn</label>
                <input
                  v-model="form.slug"
                  type="text"
                  class="form-control"
                  placeholder="nike"
                />
              </div>
            </div>

            <!-- Upload Logo tượng trưng -->
            <div class="form-group mb-3">
              <label class="form-label font-weight-bold">Logo Tượng Trưng Thương Hiệu (Upload Cloudinary)</label>
              <div class="d-flex align-items-center gap-3">
                <input
                  type="file"
                  accept="image/*"
                  class="form-control-file"
                  @change="handleImageSelect"
                />
                <div v-if="imagePreviewUrl" class="img-preview-box">
                  <img :src="imagePreviewUrl" alt="Preview Logo" class="img-preview" />
                </div>
              </div>
              <small class="text-muted">Logo sẽ được tự động upload lên thư mục Cloudinary: <code>FashionShop2/brands</code></small>
            </div>

            <!-- Trạng thái -->
            <div class="row mb-3">
              <div class="col-md-6">
                <label class="form-label font-weight-bold">Trạng Thái</label>
                <select v-model="form.status" class="form-control">
                  <option value="ACTIVE">HOẠT ĐỘNG (ACTIVE)</option>
                  <option value="INACTIVE">VÔ HIỆU (INACTIVE)</option>
                </select>
              </div>
            </div>

            <!-- Mô tả thương hiệu -->
            <div class="form-group mb-3">
              <label class="form-label font-weight-bold">Mô Tả Thương Hiệu</label>
              <textarea
                v-model="form.description"
                rows="3"
                class="form-control"
                placeholder="Giới thiệu ngắn về thương hiệu thời trang này..."
              ></textarea>
            </div>

          </div>

          <div class="modal-footer-custom">
            <button type="button" class="btn btn-secondary mr-2" @click="isModalOpen = false">Hủy Bỏ</button>
            <button type="submit" class="btn btn-primary d-flex align-items-center gap-2" :disabled="isSubmitting">
              <span v-if="isSubmitting" class="spinner-border spinner-border-sm"></span>
              <span>{{ isSubmitting ? 'ĐANG LƯU...' : 'LƯU THƯƠNG HIỆU' }}</span>
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

/* Brand logo style */
.brand-logo-box {
  width: 54px;
  height: 38px;
  border-radius: 6px;
  overflow: hidden;
  margin: 0 auto;
  border: 1px solid #e2e8f0;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.brand-logo-img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.brand-logo-placeholder {
  width: 100%;
  height: 100%;
  background: #f1f5f9;
  color: #94a3b8;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
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

.img-preview-box {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #ffffff;
}

.img-preview {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}
</style>
