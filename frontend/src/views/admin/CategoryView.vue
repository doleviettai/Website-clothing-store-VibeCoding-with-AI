<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import * as categoryApi from '@/api/categoryApi'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'

// ─── State ────────────────────────────────────────────────────
const categories = ref([])
const dropdownCategories = ref([])
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
  parentId: null,
  description: '',
  imageUrl: '',
  status: 'ACTIVE',
  sortOrder: 0,
})

const formErrors = reactive({
  name: '',
})

// ─── Methods ──────────────────────────────────────────────────

// Tải danh sách chuyên mục cho Bảng (Table)
const fetchCategories = async () => {
  isLoading.value = true
  errorMessage.value = ''
  try {
    const res = await categoryApi.getAdminCategories({
      keyword: filters.keyword || undefined,
      status: filters.status || undefined,
      page: filters.page,
      size: filters.size,
    })
    const data = res.data.data
    categories.value = data.content
    pagination.totalPages = data.totalPages
    pagination.totalElements = data.totalElements
  } catch (err) {
    errorMessage.value = err.response?.data?.message || 'Không thể tải danh sách chuyên mục.'
  } finally {
    isLoading.value = false
  }
}

// Tải danh sách chuyên mục cho Dropdown chuyên mục cha
const fetchDropdownCategories = async () => {
  try {
    const res = await categoryApi.getCategoryDropdown()
    dropdownCategories.value = res.data.data
  } catch {
    //
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

// Xử lý chọn tệp ảnh
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
  form.parentId = null
  form.description = ''
  form.imageUrl = ''
  form.status = 'ACTIVE'
  form.sortOrder = 0
  formErrors.name = ''
  selectedImageFile.value = null
  imagePreviewUrl.value = ''
  isModalOpen.value = true
}

// Mở modal chỉnh sửa
const openEditModal = (cat) => {
  isEditing.value = true
  form.id = cat.id
  form.name = cat.name
  form.slug = cat.slug
  form.parentId = cat.parentId || null
  form.description = cat.description || ''
  form.imageUrl = cat.imageUrl || ''
  form.status = cat.status
  form.sortOrder = cat.sortOrder
  formErrors.name = ''
  selectedImageFile.value = null
  imagePreviewUrl.value = cat.imageUrl || ''
  isModalOpen.value = true
}

// Xử lý lưu chuyên mục (Tạo mới hoặc Cập nhật)
const handleSubmit = async () => {
  formErrors.name = ''
  if (!form.name.trim()) {
    formErrors.name = 'Tên chuyên mục không được để trống'
    return
  }

  isSubmitting.value = true
  errorMessage.value = ''

  try {
    const categoryData = {
      name: form.name.trim(),
      slug: form.slug.trim() || slugify(form.name),
      parentId: form.parentId ? Number(form.parentId) : null,
      description: form.description,
      imageUrl: form.imageUrl,
      status: form.status,
      sortOrder: Number(form.sortOrder) || 0,
    }

    const formData = new FormData()
    formData.append(
      'category',
      new Blob([JSON.stringify(categoryData)], { type: 'application/json' })
    )

    if (selectedImageFile.value) {
      formData.append('image', selectedImageFile.value)
    }

    if (isEditing.value) {
      await categoryApi.updateCategory(form.id, formData)
      successMessage.value = 'Cập nhật chuyên mục thành công!'
    } else {
      await categoryApi.createCategory(formData)
      successMessage.value = 'Tạo chuyên mục mới thành công!'
    }

    isModalOpen.value = false
    await fetchCategories()
    await fetchDropdownCategories()

    setTimeout(() => {
      successMessage.value = ''
    }, 3000)
  } catch (err) {
    errorMessage.value = err.response?.data?.message || 'Có lỗi xảy ra khi lưu chuyên mục.'
  } finally {
    isSubmitting.value = false
  }
}

// Xử lý xóa chuyên mục
const handleDelete = async (cat) => {
  if (confirm(`Bạn có chắc chắn muốn xóa chuyên mục "${cat.name}"?`)) {
    try {
      await categoryApi.deleteCategory(cat.id)
      successMessage.value = 'Đã xóa chuyên mục thành công!'
      await fetchCategories()
      await fetchDropdownCategories()
      setTimeout(() => {
        successMessage.value = ''
      }, 3000)
    } catch (err) {
      alert(err.response?.data?.message || 'Không thể xóa chuyên mục này.')
    }
  }
}

// Phân trang
const changePage = (newPage) => {
  if (newPage >= 0 && newPage < pagination.totalPages) {
    filters.page = newPage
    fetchCategories()
  }
}

// ─── AJAX Live Search (Tự động tải dữ liệu theo thời gian thực mỗi khi gõ) ───
let searchDebounceTimer = null

watch(
  () => filters.keyword,
  () => {
    clearTimeout(searchDebounceTimer)
    searchDebounceTimer = setTimeout(() => {
      filters.page = 0
      fetchCategories()
    }, 300)
  }
)

onMounted(() => {
  fetchCategories()
  fetchDropdownCategories()
})
</script>

<template>
  <div class="admin-category-page">
    <!-- Header Trực quan -->
    <div class="d-flex align-items-center justify-content-between mb-4 flex-wrap gap-3">
      <div>
        <h3 class="page-title">Quản Lý Chuyên Mục</h3>
        <p class="text-muted mb-0">Quản lý chuyên mục sản phẩm, phân cấp cha-con và ảnh hiển thị</p>
      </div>
      <button class="btn btn-primary d-flex align-items-center gap-2" @click="openAddModal">
        <i class="fa fa-plus"></i>
        <span>Thêm Chuyên Mục</span>
      </button>
    </div>

    <!-- Alert thông báo -->
    <div v-if="successMessage" class="alert alert-success alert-dismissible fade show mb-4" role="alert">
      <i class="fa fa-check-circle mr-2"></i> {{ successMessage }}
    </div>
    <div v-if="errorMessage" class="alert alert-danger alert-dismissible fade show mb-4" role="alert">
      <i class="fa fa-exclamation-circle mr-2"></i> {{ errorMessage }}
    </div>

    <!-- Thanh Tìm kiếm & Bộ lọc -->
    <div class="card border-0 shadow-sm mb-4">
      <div class="card-body">
        <div class="row g-3 align-items-center">
          <div class="col-md-5">
            <div class="input-group">
              <input
                v-model="filters.keyword"
                type="text"
                class="form-control"
                placeholder="Tìm kiếm theo tên chuyên mục hoặc slug..."
                @keyup.enter="fetchCategories"
              />
              <button class="btn btn-outline-secondary" @click="fetchCategories">
                <i class="fa fa-search"></i>
              </button>
            </div>
          </div>
          <div class="col-md-3">
            <select v-model="filters.status" class="form-control" @change="fetchCategories">
              <option value="">-- Tất cả trạng thái --</option>
              <option value="ACTIVE">HOẠT ĐỘNG (ACTIVE)</option>
              <option value="INACTIVE">VÔ HIỆU (INACTIVE)</option>
            </select>
          </div>
          <div class="col-md-2">
            <button class="btn btn-light border w-100" @click="filters.keyword = ''; filters.status = ''; fetchCategories()">
              <i class="fa fa-refresh mr-1"></i> Làm mới
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Bảng Chuyên Mục (Table) -->
    <div class="card border-0 shadow-sm">
      <div class="card-body p-0">
        <LoadingSpinner v-if="isLoading" text="Đang tải danh sách chuyên mục..." />

        <div v-else class="table-responsive">
          <table class="table table-hover align-middle mb-0 text-center">
            <thead class="bg-light text-secondary">
              <tr>
                <th style="width: 70px;">ID</th>
                <th style="width: 90px;">Ảnh</th>
                <th class="text-left">Tên Chuyên Mục</th>
                <th class="text-left">Slug</th>
                <th>Chuyên Mục Cha</th>
                <th>Thứ Tự</th>
                <th>Trạng Thái</th>
                <th style="width: 140px;">Thao Tác</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="cat in categories" :key="cat.id">
                <td class="font-weight-bold text-muted">#{{ cat.id }}</td>
                <!-- Ảnh tượng trưng chuyên mục -->
                <td>
                  <div class="cat-img-box">
                    <img
                      v-if="cat.imageUrl"
                      :src="cat.imageUrl"
                      :alt="cat.name"
                      class="cat-img"
                    />
                    <div v-else class="cat-img-placeholder">
                      <i class="fa fa-folder"></i>
                    </div>
                  </div>
                </td>
                <td class="text-left">
                  <span class="font-weight-bold text-dark d-block">{{ cat.name }}</span>
                  <small v-if="cat.description" class="text-muted text-truncate d-inline-block" style="max-width: 220px;">
                    {{ cat.description }}
                  </small>
                </td>
                <td class="text-left">
                  <code>{{ cat.slug }}</code>
                </td>
                <td>
                  <span v-if="cat.parentName" class="badge bg-light text-dark border">
                    <i class="fa fa-level-up mr-1 text-primary"></i>{{ cat.parentName }}
                  </span>
                  <span v-else class="text-muted small">Gốc (Top Level)</span>
                </td>
                <td>
                  <span class="badge bg-secondary text-white">{{ cat.sortOrder }}</span>
                </td>
                <td>
                  <span
                    class="badge"
                    :class="cat.status === 'ACTIVE' ? 'bg-success text-white' : 'bg-danger text-white'"
                  >
                    {{ cat.status === 'ACTIVE' ? 'HOẠT ĐỘNG' : 'VÔ HIỆU' }}
                  </span>
                </td>
                <td>
                  <button class="btn btn-sm btn-outline-info mr-2" @click="openEditModal(cat)" title="Sửa">
                    <i class="fa fa-pencil"></i>
                  </button>
                  <button class="btn btn-sm btn-outline-danger" @click="handleDelete(cat)" title="Xóa">
                    <i class="fa fa-trash"></i>
                  </button>
                </td>
              </tr>
              <tr v-if="categories.length === 0">
                <td colspan="8" class="text-center py-5 text-muted">
                  <i class="fa fa-folder-open-o fa-2x mb-2 d-block"></i>
                  Không tìm thấy chuyên mục nào phù hợp.
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Footer Phân Trang -->
      <div v-if="pagination.totalPages > 1" class="card-footer bg-white d-flex justify-content-between align-items-center py-3">
        <small class="text-muted">Trang {{ filters.page + 1 }} / {{ pagination.totalPages }} (Tổng {{ pagination.totalElements }} chuyên mục)</small>
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

    <!-- Modal Thêm / Chỉnh Sửa Chuyên Mục -->
    <div v-if="isModalOpen" class="modal-overlay" @click.self="isModalOpen = false">
      <div class="modal-box">
        <div class="modal-header-custom">
          <h5 class="m-0 font-weight-bold">
            <i class="fa mr-2" :class="isEditing ? 'fa-pencil' : 'fa-plus-circle'"></i>
            {{ isEditing ? 'Chỉnh Sửa Chuyên Mục' : 'Thêm Chuyên Mục Mới' }}
          </h5>
          <button class="close-btn" @click="isModalOpen = false">&times;</button>
        </div>

        <form @submit.prevent="handleSubmit">
          <div class="modal-body-custom">

            <!-- Tên chuyên mục & Slug -->
            <div class="row mb-3">
              <div class="col-md-6">
                <label class="form-label font-weight-bold">Tên Chuyên Mục <span class="text-danger">*</span></label>
                <input
                  v-model="form.name"
                  type="text"
                  class="form-control"
                  :class="{ 'is-invalid': formErrors.name }"
                  placeholder="Ví dụ: Áo Khoác Nam"
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
                  placeholder="ao-khoac-nam"
                />
              </div>
            </div>

            <!-- Chuyên mục cha & Thứ tự -->
            <div class="row mb-3">
              <div class="col-md-6">
                <label class="form-label font-weight-bold">Chuyên Mục Cha</label>
                <select v-model="form.parentId" class="form-control">
                  <option :value="null">-- Không chọn (Là danh mục Gốc) --</option>
                  <option
                    v-for="cat in dropdownCategories.filter(c => c.id !== form.id)"
                    :key="cat.id"
                    :value="cat.id"
                  >
                    {{ cat.name }} (ID: {{ cat.id }})
                  </option>
                </select>
              </div>
              <div class="col-md-6">
                <label class="form-label font-weight-bold">Thứ Tự Hiển Thị</label>
                <input
                  v-model="form.sortOrder"
                  type="number"
                  class="form-control"
                  placeholder="0"
                />
              </div>
            </div>

            <!-- Upload Ảnh tượng trưng -->
            <div class="form-group mb-3">
              <label class="form-label font-weight-bold">Ảnh Tượng Trưng Chuyên Mục (Upload Cloudinary)</label>
              <div class="d-flex align-items-center gap-3">
                <input
                  type="file"
                  accept="image/*"
                  class="form-control-file"
                  @change="handleImageSelect"
                />
                <div v-if="imagePreviewUrl" class="img-preview-box">
                  <img :src="imagePreviewUrl" alt="Preview" class="img-preview" />
                </div>
              </div>
              <small class="text-muted">Ảnh sẽ được lưu tự động vào thư mục Cloudinary: <code>FashionShop2/categories</code></small>
            </div>

            <!-- Trạng thái & Mô tả -->
            <div class="row mb-3">
              <div class="col-md-6">
                <label class="form-label font-weight-bold">Trạng Thái</label>
                <select v-model="form.status" class="form-control">
                  <option value="ACTIVE">HOẠT ĐỘNG (ACTIVE)</option>
                  <option value="INACTIVE">VÔ HIỆU (INACTIVE)</option>
                </select>
              </div>
            </div>

            <div class="form-group mb-3">
              <label class="form-label font-weight-bold">Mô Tả Chuyên Mục</label>
              <textarea
                v-model="form.description"
                rows="3"
                class="form-control"
                placeholder="Mô tả chi tiết về nhóm sản phẩm này..."
              ></textarea>
            </div>

          </div>

          <div class="modal-footer-custom">
            <button type="button" class="btn btn-secondary mr-2" @click="isModalOpen = false">Hủy Bỏ</button>
            <button type="submit" class="btn btn-primary d-flex align-items-center gap-2" :disabled="isSubmitting">
              <span v-if="isSubmitting" class="spinner-border spinner-border-sm"></span>
              <span>{{ isSubmitting ? 'ĐANG LƯU...' : 'LƯU CHUYÊN MỤC' }}</span>
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

/* Category image style */
.cat-img-box {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  overflow: hidden;
  margin: 0 auto;
  border: 1px solid #e2e8f0;
}

.cat-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cat-img-placeholder {
  width: 100%;
  height: 100%;
  background: #f1f5f9;
  color: #94a3b8;
  display: flex;
  align-items: center;
  justify-content: center;
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
  width: 60px;
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
