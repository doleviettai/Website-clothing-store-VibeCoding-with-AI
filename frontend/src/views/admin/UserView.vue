<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import * as userApi from '@/api/userApi'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'

// ─── State ────────────────────────────────────────────────────
const users = ref([])
const isLoading = ref(false)
const isSubmitting = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

// Phân trang & Lọc
const filters = reactive({
  keyword: '',
  status: '',
  roleName: '',
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
  fullName: '',
  email: '',
  phone: '',
  password: '',
  avatarUrl: '',
  status: 'ACTIVE',
  roleName: 'ROLE_USER',
})

const formErrors = reactive({
  fullName: '',
  email: '',
  password: '',
})

// ─── Methods ──────────────────────────────────────────────────

// Tải danh sách Người dùng cho Bảng (Table)
const fetchUsers = async () => {
  isLoading.value = true
  errorMessage.value = ''
  try {
    const res = await userApi.getAdminUsers({
      keyword: filters.keyword || undefined,
      status: filters.status || undefined,
      roleName: filters.roleName || undefined,
      page: filters.page,
      size: filters.size,
    })
    const data = res.data.data
    users.value = data.content
    pagination.totalPages = data.totalPages
    pagination.totalElements = data.totalElements
  } catch (err) {
    errorMessage.value = err.response?.data?.message || 'Không thể tải danh sách người dùng.'
  } finally {
    isLoading.value = false
  }
}

// Xử lý chọn tệp ảnh avatar
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
  form.fullName = ''
  form.email = ''
  form.phone = ''
  form.password = ''
  form.avatarUrl = ''
  form.status = 'ACTIVE'
  form.roleName = 'ROLE_USER'
  formErrors.fullName = ''
  formErrors.email = ''
  formErrors.password = ''
  selectedImageFile.value = null
  imagePreviewUrl.value = ''
  isModalOpen.value = true
}

// Mở modal chỉnh sửa
const openEditModal = (user) => {
  isEditing.value = true
  form.id = user.id
  form.fullName = user.fullName
  form.email = user.email
  form.phone = user.phone || ''
  form.password = '' // Để trống = giữ nguyên mật khẩu cũ
  form.avatarUrl = user.avatarUrl || ''
  form.status = user.status
  form.roleName = user.roles && user.roles.length > 0 ? Array.from(user.roles)[0] : 'ROLE_USER'
  formErrors.fullName = ''
  formErrors.email = ''
  formErrors.password = ''
  selectedImageFile.value = null
  imagePreviewUrl.value = user.avatarUrl || ''
  isModalOpen.value = true
}

// Xử lý lưu người dùng
const handleSubmit = async () => {
  formErrors.fullName = ''
  formErrors.email = ''
  formErrors.password = ''

  if (!form.fullName.trim()) {
    formErrors.fullName = 'Tên người dùng không được để trống'
    return
  }
  if (!form.email.trim()) {
    formErrors.email = 'Email không được để trống'
    return
  }
  if (!isEditing.value && (!form.password || form.password.length < 6)) {
    formErrors.password = 'Mật khẩu phải từ 6 ký tự trở lên'
    return
  }

  isSubmitting.value = true
  errorMessage.value = ''

  try {
    const userData = {
      fullName: form.fullName.trim(),
      email: form.email.trim().toLowerCase(),
      phone: form.phone,
      password: form.password ? form.password : undefined,
      avatarUrl: form.avatarUrl,
      status: form.status,
      roleName: form.roleName,
    }

    const formData = new FormData()
    formData.append(
      'user',
      new Blob([JSON.stringify(userData)], { type: 'application/json' })
    )

    if (selectedImageFile.value) {
      formData.append('image', selectedImageFile.value)
    }

    if (isEditing.value) {
      await userApi.updateUser(form.id, formData)
      successMessage.value = 'Cập nhật tài khoản người dùng thành công!'
    } else {
      await userApi.createUser(formData)
      successMessage.value = 'Tạo tài khoản người dùng mới thành công!'
    }

    isModalOpen.value = false
    await fetchUsers()

    setTimeout(() => {
      successMessage.value = ''
    }, 3000)
  } catch (err) {
    errorMessage.value = err.response?.data?.message || 'Có lỗi xảy ra khi lưu thông tin người dùng.'
  } finally {
    isSubmitting.value = false
  }
}

// Xử lý xóa người dùng
const handleDelete = async (user) => {
  if (confirm(`Bạn có chắc chắn muốn xóa tài khoản "${user.fullName}" (${user.email})?`)) {
    try {
      await userApi.deleteUser(user.id)
      successMessage.value = 'Đã xóa tài khoản người dùng thành công!'
      await fetchUsers()
      setTimeout(() => {
        successMessage.value = ''
      }, 3000)
    } catch (err) {
      alert(err.response?.data?.message || 'Không thể xóa tài khoản này.')
    }
  }
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

// Helper nhận diện vai trò người dùng (cho dù backend trả về ADMIN, ROLE_ADMIN hay Quản trị viên)
const getRoleLabel = (role) => {
  if (!role) return 'Khách hàng (CLIENT)'
  const r = role.toString().toUpperCase()
  if (r.includes('ADMIN') || r.includes('QUẢN TRỊ')) {
    return 'Quản trị viên (ADMIN)'
  }
  if (r.includes('STAFF') || r.includes('NHÂN VIÊN')) {
    return 'Nhân viên (STAFF)'
  }
  return 'Khách hàng (CLIENT)'
}

const getRoleBadgeClass = (role) => {
  if (!role) return 'bg-primary text-white'
  const r = role.toString().toUpperCase()
  if (r.includes('ADMIN') || r.includes('QUẢN TRỊ')) return 'bg-danger text-white'
  if (r.includes('STAFF') || r.includes('NHÂN VIÊN')) return 'bg-warning text-dark'
  return 'bg-primary text-white'
}

// Phân trang
const changePage = (newPage) => {
  if (newPage >= 0 && newPage < pagination.totalPages) {
    filters.page = newPage
    fetchUsers()
  }
}

// ─── AJAX Live Search (Tự động nạp dữ liệu khi nhập tên, email, sđt) ───
let searchDebounceTimer = null

watch(
  () => filters.keyword,
  () => {
    clearTimeout(searchDebounceTimer)
    searchDebounceTimer = setTimeout(() => {
      filters.page = 0
      fetchUsers()
    }, 300)
  }
)

onMounted(() => {
  fetchUsers()
})
</script>

<template>
  <div class="admin-user-page">
    <!-- Header Trực quan -->
    <div class="d-flex align-items-center justify-content-between mb-4 flex-wrap gap-3">
      <div>
        <h3 class="page-title">Quản Lý Tài Khoản Người Dùng</h3>
        <p class="text-muted mb-0">Quản lý tài khoản khách hàng, nhân viên, phân quyền vai trò, nhật ký đăng nhập và trạng thái</p>
      </div>
      <button class="btn btn-primary d-flex align-items-center gap-2" @click="openAddModal">
        <i class="fa fa-user-plus"></i>
        <span>Thêm Người Dùng Mới</span>
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
                placeholder="Nhập tên, email hoặc số điện thoại để tìm kiếm tức thì (Live Search AJAX)..."
              />
            </div>
          </div>
          <div class="col-md-3">
            <select v-model="filters.roleName" class="form-control" @change="fetchUsers">
              <option value="">-- Tất cả vai trò --</option>
              <option value="ROLE_ADMIN">QUẢN TRỊ VIÊN (ROLE_ADMIN)</option>
              <option value="ROLE_STAFF">NHÂN VIÊN (ROLE_STAFF)</option>
              <option value="ROLE_USER">KHÁCH HÀNG (ROLE_USER)</option>
            </select>
          </div>
          <div class="col-md-2">
            <select v-model="filters.status" class="form-control" @change="fetchUsers">
              <option value="">-- Trạng thái --</option>
              <option value="ACTIVE">ACTIVE (Hoạt động)</option>
              <option value="LOCKED">LOCKED (Bị khóa)</option>
              <option value="INACTIVE">INACTIVE (Vô hiệu)</option>
            </select>
          </div>
          <div class="col-md-2">
            <button class="btn btn-light border w-100" @click="filters.keyword = ''; filters.roleName = ''; filters.status = ''; fetchUsers()">
              <i class="fa fa-refresh mr-1"></i> Làm mới
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Bảng Người Dùng (Table) -->
    <div class="card border-0 shadow-sm">
      <div class="card-body p-0">
        <LoadingSpinner v-if="isLoading" text="Đang tải danh sách tài khoản người dùng..." />

        <div v-else class="table-responsive">
          <table class="table table-hover align-middle mb-0 text-center">
            <thead class="bg-light text-secondary">
              <tr>
                <th style="width: 60px;">ID</th>
                <th style="width: 70px;">Avatar</th>
                <th class="text-left">Tên Người Dùng</th>
                <th class="text-left">Email</th>
                <th>Số Điện Thoại</th>
                <th>Vai Trò</th>
                <th>Đăng Nhập Lần Cuối</th>
                <th>Đăng Xuất Lần Cuối</th>
                <th>Trạng Thái</th>
                <th style="width: 130px;">Thao Tác</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="user in users" :key="user.id">
                <td class="font-weight-bold text-muted">#{{ user.id }}</td>
                <!-- Avatar tượng trưng người dùng -->
                <td>
                  <div class="user-avatar-box">
                    <img
                      v-if="user.avatarUrl"
                      :src="user.avatarUrl"
                      :alt="user.fullName"
                      class="user-avatar-img"
                    />
                    <div v-else class="user-avatar-placeholder">
                      <i class="fa fa-user"></i>
                    </div>
                  </div>
                </td>
                <td class="text-left">
                  <span class="font-weight-bold text-dark d-block">{{ user.fullName }}</span>
                </td>
                <td class="text-left">
                  <code>{{ user.email }}</code>
                </td>
                <td>
                  <span v-if="user.phone" class="text-dark font-weight-bold">{{ user.phone }}</span>
                  <span v-else class="text-muted small">-</span>
                </td>
                <td>
                  <span
                    v-for="role in user.roles"
                    :key="role"
                    class="badge mr-1 px-2 py-1"
                    :class="getRoleBadgeClass(role)"
                  >
                    {{ getRoleLabel(role) }}
                  </span>
                </td>
                <!-- Thời gian Login / Logout -->
                <td>
                  <span class="small text-muted d-block">{{ formatDate(user.lastLoginAt) }}</span>
                </td>
                <td>
                  <span class="small text-muted d-block">{{ formatDate(user.lastLogoutAt) }}</span>
                </td>
                <td>
                  <span
                    class="badge"
                    :class="{
                      'bg-success text-white': user.status === 'ACTIVE',
                      'bg-warning text-dark': user.status === 'LOCKED',
                      'bg-danger text-white': user.status === 'INACTIVE'
                    }"
                  >
                    {{ user.status === 'ACTIVE' ? 'HOẠT ĐỘNG' : (user.status === 'LOCKED' ? 'BỊ KHÓA' : 'VÔ HIỆU') }}
                  </span>
                </td>
                <td>
                  <button class="btn btn-sm btn-outline-info mr-2" @click="openEditModal(user)" title="Sửa">
                    <i class="fa fa-pencil"></i>
                  </button>
                  <button class="btn btn-sm btn-outline-danger" @click="handleDelete(user)" title="Xóa">
                    <i class="fa fa-trash"></i>
                  </button>
                </td>
              </tr>
              <tr v-if="users.length === 0">
                <td colspan="10" class="text-center py-5 text-muted">
                  <i class="fa fa-users fa-2x mb-2 d-block"></i>
                  Không tìm thấy tài khoản người dùng nào phù hợp.
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Footer Phân Trang -->
      <div v-if="pagination.totalPages > 1" class="card-footer bg-white d-flex justify-content-between align-items-center py-3">
        <small class="text-muted">Trang {{ filters.page + 1 }} / {{ pagination.totalPages }} (Tổng {{ pagination.totalElements }} người dùng)</small>
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

    <!-- Modal Thêm / Chỉnh Sửa Người Dùng -->
    <div v-if="isModalOpen" class="modal-overlay" @click.self="isModalOpen = false">
      <div class="modal-box">
        <div class="modal-header-custom">
          <h5 class="m-0 font-weight-bold">
            <i class="fa mr-2" :class="isEditing ? 'fa-pencil' : 'fa-user-plus'"></i>
            {{ isEditing ? 'Chỉnh Sửa Tài Khoản Người Dùng' : 'Thêm Người Dùng Mới' }}
          </h5>
          <button class="close-btn" @click="isModalOpen = false">&times;</button>
        </div>

        <form @submit.prevent="handleSubmit">
          <div class="modal-body-custom">

            <!-- Tên & Email -->
            <div class="row mb-3">
              <div class="col-md-6">
                <label class="form-label font-weight-bold">Họ Và Tên <span class="text-danger">*</span></label>
                <input
                  v-model="form.fullName"
                  type="text"
                  class="form-control"
                  :class="{ 'is-invalid': formErrors.fullName }"
                  placeholder="Ví dụ: Nguyễn Văn A"
                />
                <div v-if="formErrors.fullName" class="invalid-feedback">{{ formErrors.fullName }}</div>
              </div>
              <div class="col-md-6">
                <label class="form-label font-weight-bold">Email Đăng Nhập <span class="text-danger">*</span></label>
                <input
                  v-model="form.email"
                  type="email"
                  class="form-control"
                  :class="{ 'is-invalid': formErrors.email }"
                  placeholder="nguyenvana@gmail.com"
                />
                <div v-if="formErrors.email" class="invalid-feedback">{{ formErrors.email }}</div>
              </div>
            </div>

            <!-- Số điện thoại & Mật khẩu -->
            <div class="row mb-3">
              <div class="col-md-6">
                <label class="form-label font-weight-bold">Số Điện Thoại</label>
                <input
                  v-model="form.phone"
                  type="text"
                  class="form-control"
                  placeholder="0912345678"
                />
              </div>
              <div class="col-md-6">
                <label class="form-label font-weight-bold">
                  Mật Khẩu
                  <span v-if="!isEditing" class="text-danger">*</span>
                  <span v-else class="text-muted font-weight-normal small">(để trống = giữ nguyên)</span>
                </label>
                <input
                  v-model="form.password"
                  type="password"
                  class="form-control"
                  :class="{ 'is-invalid': formErrors.password }"
                  placeholder="••••••••"
                />
                <div v-if="formErrors.password" class="invalid-feedback">{{ formErrors.password }}</div>
              </div>
            </div>

            <!-- Upload Avatar người dùng -->
            <div class="form-group mb-3">
              <label class="form-label font-weight-bold">Ảnh Đại Diện / Avatar (Upload Cloudinary)</label>
              <div class="d-flex align-items-center gap-3">
                <input
                  type="file"
                  accept="image/*"
                  class="form-control-file"
                  @change="handleImageSelect"
                />
                <div v-if="imagePreviewUrl" class="img-preview-box">
                  <img :src="imagePreviewUrl" alt="Preview Avatar" class="img-preview" />
                </div>
              </div>
              <small class="text-muted">Ảnh avatar sẽ được upload tự động lên thư mục Cloudinary: <code>FashionShop2/users</code></small>
            </div>

            <!-- Vai Trò & Trạng Thái -->
            <div class="row mb-3">
              <div class="col-md-6">
                <label class="form-label font-weight-bold">Vai Trò Hệ Thống <span class="text-danger">*</span></label>
                <select v-model="form.roleName" class="form-control" required>
                  <option value="CLIENT">KHÁCH HÀNG (CLIENT)</option>
                  <option value="ADMIN">QUẢN TRỊ VIÊN (ADMIN)</option>
                  <option value="STAFF">NHÂN VIÊN (STAFF)</option>
                  <option value="ROLE_USER">KHÁCH HÀNG (ROLE_USER)</option>
                  <option value="ROLE_ADMIN">QUẢN TRỊ VIÊN (ROLE_ADMIN)</option>
                </select>
              </div>
              <div class="col-md-6">
                <label class="form-label font-weight-bold">Trạng Thái Tài Khoản</label>
                <select v-model="form.status" class="form-control">
                  <option value="ACTIVE">ACTIVE (Hoạt động)</option>
                  <option value="LOCKED">LOCKED (Khóa tài khoản)</option>
                  <option value="INACTIVE">INACTIVE (Vô hiệu hóa)</option>
                </select>
              </div>
            </div>

          </div>

          <div class="modal-footer-custom">
            <button type="button" class="btn btn-secondary mr-2" @click="isModalOpen = false">Hủy Bỏ</button>
            <button type="submit" class="btn btn-primary d-flex align-items-center gap-2" :disabled="isSubmitting">
              <span v-if="isSubmitting" class="spinner-border spinner-border-sm"></span>
              <span>{{ isSubmitting ? 'ĐANG LƯU...' : 'LƯU TÀI KHOẢN' }}</span>
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

/* User avatar style */
.user-avatar-box {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  overflow: hidden;
  margin: 0 auto;
  border: 1px solid #e2e8f0;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-avatar-placeholder {
  width: 100%;
  height: 100%;
  background: #f1f5f9;
  color: #94a3b8;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
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
  width: 50px;
  height: 50px;
  border-radius: 50%;
  overflow: hidden;
  border: 1px solid #e2e8f0;
}

.img-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
</style>
