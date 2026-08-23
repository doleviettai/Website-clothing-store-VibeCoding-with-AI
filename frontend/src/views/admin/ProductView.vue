<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import * as productApi from '@/api/productApi'
import * as categoryApi from '@/api/categoryApi'
import * as brandApi from '@/api/brandApi'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'

// ─── State ────────────────────────────────────────────────────
const products = ref([])
const categories = ref([])
const brands = ref([])
const isLoading = ref(false)
const isSubmitting = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

// Phân trang & Lọc
const filters = reactive({
  keyword: '',
  categoryId: '',
  brandId: '',
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
  categoryId: '',
  brandId: '',
  price: 0,
  salePrice: null,
  stockQuantity: 0,
  shortDescription: '',
  description: '',
  thumbnailUrl: '',
  status: 'ACTIVE',
  isFeatured: false,
  favoriteCount: 0,
  averageRating: 5.0,
})

const formErrors = reactive({
  name: '',
  price: '',
  stockQuantity: '',
})

// ─── Methods ──────────────────────────────────────────────────

// Tải danh sách Sản phẩm cho Bảng (Table)
const fetchProducts = async () => {
  isLoading.value = true
  errorMessage.value = ''
  try {
    const res = await productApi.getAdminProducts({
      keyword: filters.keyword || undefined,
      categoryId: filters.categoryId || undefined,
      brandId: filters.brandId || undefined,
      status: filters.status || undefined,
      page: filters.page,
      size: filters.size,
    })
    const data = res.data.data
    products.value = data.content
    pagination.totalPages = data.totalPages
    pagination.totalElements = data.totalElements
  } catch (err) {
    errorMessage.value = err.response?.data?.message || 'Không thể tải danh sách sản phẩm.'
  } finally {
    isLoading.value = false
  }
}

// Tải danh sách Chuyên mục & Thương hiệu cho Dropdown
const fetchDropdownData = async () => {
  try {
    const [resCat, resBrand] = await Promise.all([
      categoryApi.getCategoryDropdown(),
      brandApi.getBrandDropdown()
    ])
    categories.value = resCat.data.data || []
    brands.value = resBrand.data.data || []
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

// Xử lý chọn tệp ảnh sản phẩm
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
  form.categoryId = ''
  form.brandId = ''
  form.price = 0
  form.salePrice = null
  form.stockQuantity = 10
  form.shortDescription = ''
  form.description = ''
  form.thumbnailUrl = ''
  form.status = 'ACTIVE'
  form.isFeatured = false
  form.favoriteCount = 0
  form.averageRating = 5.0
  formErrors.name = ''
  formErrors.price = ''
  formErrors.stockQuantity = ''
  selectedImageFile.value = null
  imagePreviewUrl.value = ''
  isModalOpen.value = true
}

// Mở modal chỉnh sửa
const openEditModal = (product) => {
  isEditing.value = true
  form.id = product.id
  form.name = product.name
  form.slug = product.slug
  form.categoryId = product.categoryId || ''
  form.brandId = product.brandId || ''
  form.price = product.price
  form.salePrice = product.salePrice || null
  form.stockQuantity = product.stockQuantity
  form.shortDescription = product.shortDescription || ''
  form.description = product.description || ''
  form.thumbnailUrl = product.thumbnailUrl || ''
  form.status = product.status
  form.isFeatured = product.isFeatured || false
  form.favoriteCount = product.favoriteCount || 0
  form.averageRating = product.averageRating || 5.0
  formErrors.name = ''
  formErrors.price = ''
  formErrors.stockQuantity = ''
  selectedImageFile.value = null
  imagePreviewUrl.value = product.thumbnailUrl || ''
  isModalOpen.value = true
}

// Xử lý lưu sản phẩm
const handleSubmit = async () => {
  formErrors.name = ''
  formErrors.price = ''
  formErrors.stockQuantity = ''

  if (!form.name.trim()) {
    formErrors.name = 'Tên sản phẩm không được để trống'
    return
  }
  if (form.price === null || form.price < 0) {
    formErrors.price = 'Giá bán phải lớn hơn hoặc bằng 0'
    return
  }

  isSubmitting.value = true
  errorMessage.value = ''

  try {
    const productData = {
      name: form.name.trim(),
      slug: form.slug.trim() || slugify(form.name),
      categoryId: form.categoryId ? Number(form.categoryId) : null,
      brandId: form.brandId ? Number(form.brandId) : null,
      price: Number(form.price),
      salePrice: form.salePrice ? Number(form.salePrice) : null,
      stockQuantity: Number(form.stockQuantity) || 0,
      shortDescription: form.shortDescription,
      description: form.description,
      thumbnailUrl: form.thumbnailUrl,
      status: form.status,
      isFeatured: form.isFeatured,
      favoriteCount: Number(form.favoriteCount) || 0,
      averageRating: Number(form.averageRating) || 5.0,
    }

    const formData = new FormData()
    formData.append(
      'product',
      new Blob([JSON.stringify(productData)], { type: 'application/json' })
    )

    if (selectedImageFile.value) {
      formData.append('image', selectedImageFile.value)
    }

    if (isEditing.value) {
      await productApi.updateProduct(form.id, formData)
      successMessage.value = 'Cập nhật sản phẩm thành công!'
    } else {
      await productApi.createProduct(formData)
      successMessage.value = 'Tạo sản phẩm mới thành công!'
    }

    isModalOpen.value = false
    await fetchProducts()

    setTimeout(() => {
      successMessage.value = ''
    }, 3000)
  } catch (err) {
    errorMessage.value = err.response?.data?.message || 'Có lỗi xảy ra khi lưu sản phẩm.'
  } finally {
    isSubmitting.value = false
  }
}

// Xử lý xóa sản phẩm
const handleDelete = async (product) => {
  if (confirm(`Bạn có chắc chắn muốn xóa sản phẩm "${product.name}"?`)) {
    try {
      await productApi.deleteProduct(product.id)
      successMessage.value = 'Đã xóa sản phẩm thành công!'
      await fetchProducts()
      setTimeout(() => {
        successMessage.value = ''
      }, 3000)
    } catch (err) {
      alert(err.response?.data?.message || 'Không thể xóa sản phẩm này.')
    }
  }
}

const formatPrice = (value) => {
  if (!value) return '0 đ'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value)
}

// Phân trang
const changePage = (newPage) => {
  if (newPage >= 0 && newPage < pagination.totalPages) {
    filters.page = newPage
    fetchProducts()
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
      fetchProducts()
    }, 300)
  }
)

onMounted(() => {
  fetchProducts()
  fetchDropdownData()
})
</script>

<template>
  <div class="admin-product-page">
    <!-- Header Trực quan -->
    <div class="d-flex align-items-center justify-content-between mb-4 flex-wrap gap-3">
      <div>
        <h3 class="page-title">Quản Lý Sản Phẩm</h3>
        <p class="text-muted mb-0">Quản lý kho hàng, giá bán, chuyên mục, thương hiệu, lượt yêu thích và đánh giá</p>
      </div>
      <button class="btn btn-primary d-flex align-items-center gap-2" @click="openAddModal">
        <i class="fa fa-plus"></i>
        <span>Thêm Sản Phẩm Mới</span>
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
          <div class="col-md-4">
            <div class="input-group">
              <span class="input-group-text bg-white border-end-0">
                <i class="fa fa-search text-muted"></i>
              </span>
              <input
                v-model="filters.keyword"
                type="text"
                class="form-control border-start-0"
                placeholder="Nhập tên sản phẩm hoặc slug để tìm kiếm tức thì (Live Search)..."
              />
            </div>
          </div>
          <div class="col-md-3">
            <select v-model="filters.categoryId" class="form-control" @change="fetchProducts">
              <option value="">-- Tất cả chuyên mục --</option>
              <option v-for="cat in categories" :key="cat.id" :value="cat.id">
                {{ cat.name }}
              </option>
            </select>
          </div>
          <div class="col-md-3">
            <select v-model="filters.brandId" class="form-control" @change="fetchProducts">
              <option value="">-- Tất cả thương hiệu --</option>
              <option v-for="brand in brands" :key="brand.id" :value="brand.id">
                {{ brand.name }}
              </option>
            </select>
          </div>
          <div class="col-md-2">
            <button class="btn btn-light border w-100" @click="filters.keyword = ''; filters.categoryId = ''; filters.brandId = ''; filters.status = ''; fetchProducts()">
              <i class="fa fa-refresh mr-1"></i> Làm mới
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Bảng Sản Phẩm (Table) -->
    <div class="card border-0 shadow-sm">
      <div class="card-body p-0">
        <LoadingSpinner v-if="isLoading" text="Đang tải danh sách sản phẩm..." />

        <div v-else class="table-responsive">
          <table class="table table-hover align-middle mb-0 text-center">
            <thead class="bg-light text-secondary">
              <tr>
                <th style="width: 60px;">ID</th>
                <th style="width: 80px;">Ảnh</th>
                <th class="text-left">Tên Sản Phẩm</th>
                <th>Chuyên Mục</th>
                <th>Thương Hiệu</th>
                <th>Giá Bán</th>
                <th>Tồn Kho</th>
                <th>Đánh Giá</th>
                <th>Yêu Thích</th>
                <th>Trạng Thái</th>
                <th style="width: 130px;">Thao Tác</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="product in products" :key="product.id">
                <td class="font-weight-bold text-muted">#{{ product.id }}</td>
                <!-- Ảnh tượng trưng sản phẩm -->
                <td>
                  <div class="product-thumb-box">
                    <img
                      v-if="product.thumbnailUrl"
                      :src="product.thumbnailUrl"
                      :alt="product.name"
                      class="product-thumb-img"
                    />
                    <div v-else class="product-thumb-placeholder">
                      <i class="fa fa-shopping-bag"></i>
                    </div>
                  </div>
                </td>
                <td class="text-left">
                  <span class="font-weight-bold text-dark d-block">{{ product.name }}</span>
                  <code class="small text-muted">{{ product.slug }}</code>
                </td>
                <td>
                  <span class="badge bg-light text-dark border">{{ product.categoryName || '-' }}</span>
                </td>
                <td>
                  <span class="badge bg-light text-dark border">{{ product.brandName || '-' }}</span>
                </td>
                <td>
                  <span class="font-weight-bold text-danger d-block">{{ formatPrice(product.price) }}</span>
                  <small v-if="product.salePrice" class="text-muted text-decoration-line-through">{{ formatPrice(product.salePrice) }}</small>
                </td>
                <td>
                  <span
                    class="badge"
                    :class="product.stockQuantity > 0 ? 'bg-info text-white' : 'bg-secondary text-white'"
                  >
                    {{ product.stockQuantity }} cái
                  </span>
                </td>
                <!-- Đánh giá Rating -->
                <td>
                  <div class="small text-warning font-weight-bold">
                    <i class="fa fa-star"></i> {{ product.averageRating || '5.0' }}
                  </div>
                </td>
                <!-- Yêu thích Favorite Count -->
                <td>
                  <div class="small text-danger font-weight-bold">
                    <i class="fa fa-heart"></i> {{ product.favoriteCount || 0 }}
                  </div>
                </td>
                <td>
                  <span
                    class="badge"
                    :class="product.status === 'ACTIVE' ? 'bg-success text-white' : 'bg-danger text-white'"
                  >
                    {{ product.status === 'ACTIVE' ? 'HOẠT ĐỘNG' : 'VÔ HIỆU' }}
                  </span>
                </td>
                <td>
                  <button class="btn btn-sm btn-outline-info mr-2" @click="openEditModal(product)" title="Sửa">
                    <i class="fa fa-pencil"></i>
                  </button>
                  <button class="btn btn-sm btn-outline-danger" @click="handleDelete(product)" title="Xóa">
                    <i class="fa fa-trash"></i>
                  </button>
                </td>
              </tr>
              <tr v-if="products.length === 0">
                <td colspan="11" class="text-center py-5 text-muted">
                  <i class="fa fa-shopping-bag fa-2x mb-2 d-block"></i>
                  Không tìm thấy sản phẩm nào phù hợp.
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Footer Phân Trang -->
      <div v-if="pagination.totalPages > 1" class="card-footer bg-white d-flex justify-content-between align-items-center py-3">
        <small class="text-muted">Trang {{ filters.page + 1 }} / {{ pagination.totalPages }} (Tổng {{ pagination.totalElements }} sản phẩm)</small>
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

    <!-- Modal Thêm / Chỉnh Sửa Sản Phẩm -->
    <div v-if="isModalOpen" class="modal-overlay" @click.self="isModalOpen = false">
      <div class="modal-box">
        <div class="modal-header-custom">
          <h5 class="m-0 font-weight-bold">
            <i class="fa mr-2" :class="isEditing ? 'fa-pencil' : 'fa-plus-circle'"></i>
            {{ isEditing ? 'Chỉnh Sửa Sản Phẩm' : 'Thêm Sản Phẩm Mới' }}
          </h5>
          <button class="close-btn" @click="isModalOpen = false">&times;</button>
        </div>

        <form @submit.prevent="handleSubmit">
          <div class="modal-body-custom">

            <!-- Tên sản phẩm & Slug -->
            <div class="row mb-3">
              <div class="col-md-6">
                <label class="form-label font-weight-bold">Tên Sản Phẩm <span class="text-danger">*</span></label>
                <input
                  v-model="form.name"
                  type="text"
                  class="form-control"
                  :class="{ 'is-invalid': formErrors.name }"
                  placeholder="Ví dụ: Áo Khoác Biker Piqué Nam"
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
                  placeholder="ao-khoac-biker-pique-nam"
                />
              </div>
            </div>

            <!-- Chuyên mục & Thương hiệu -->
            <div class="row mb-3">
              <div class="col-md-6">
                <label class="form-label font-weight-bold">Chuyên Mục Liên Kết</label>
                <select v-model="form.categoryId" class="form-control">
                  <option value="">-- Chọn Chuyên Mục --</option>
                  <option v-for="cat in categories" :key="cat.id" :value="cat.id">
                    {{ cat.name }}
                  </option>
                </select>
              </div>
              <div class="col-md-6">
                <label class="form-label font-weight-bold">Thương Hiệu Liên Kết</label>
                <select v-model="form.brandId" class="form-control">
                  <option value="">-- Chọn Thương Hiệu --</option>
                  <option v-for="brand in brands" :key="brand.id" :value="brand.id">
                    {{ brand.name }}
                  </option>
                </select>
              </div>
            </div>

            <!-- Giá bán, Giá khuyến mãi & Tồn kho -->
            <div class="row mb-3">
              <div class="col-md-4">
                <label class="form-label font-weight-bold">Giá Bán (VND) <span class="text-danger">*</span></label>
                <input
                  v-model="form.price"
                  type="number"
                  class="form-control"
                  :class="{ 'is-invalid': formErrors.price }"
                  placeholder="1550000"
                  min="0"
                />
                <div v-if="formErrors.price" class="invalid-feedback">{{ formErrors.price }}</div>
              </div>
              <div class="col-md-4">
                <label class="form-label font-weight-bold">Giá Khuyến Mãi (VND)</label>
                <input
                  v-model="form.salePrice"
                  type="number"
                  class="form-control"
                  placeholder="1250000"
                  min="0"
                />
              </div>
              <div class="col-md-4">
                <label class="form-label font-weight-bold">Tồn Kho (Cái) <span class="text-danger">*</span></label>
                <input
                  v-model="form.stockQuantity"
                  type="number"
                  class="form-control"
                  placeholder="50"
                  min="0"
                />
              </div>
            </div>

            <!-- Upload Ảnh đại diện sản phẩm -->
            <div class="form-group mb-3">
              <label class="form-label font-weight-bold">Ảnh Đại Diện Sản Phẩm (Upload Cloudinary)</label>
              <div class="d-flex align-items-center gap-3">
                <input
                  type="file"
                  accept="image/*"
                  class="form-control-file"
                  @change="handleImageSelect"
                />
                <div v-if="imagePreviewUrl" class="img-preview-box">
                  <img :src="imagePreviewUrl" alt="Preview Thumbnail" class="img-preview" />
                </div>
              </div>
              <small class="text-muted">Ảnh sẽ được upload tự động lên thư mục Cloudinary: <code>FashionShop2/products</code></small>
            </div>

            <!-- Đánh giá & Số lượt yêu thích & Trạng thái -->
            <div class="row mb-3">
              <div class="col-md-4">
                <label class="form-label font-weight-bold">Đánh Giá (Rating 1.0 - 5.0)</label>
                <input
                  v-model="form.averageRating"
                  type="number"
                  step="0.1"
                  min="1"
                  max="5"
                  class="form-control"
                />
              </div>
              <div class="col-md-4">
                <label class="form-label font-weight-bold">Lượt Yêu Thích</label>
                <input
                  v-model="form.favoriteCount"
                  type="number"
                  min="0"
                  class="form-control"
                />
              </div>
              <div class="col-md-4">
                <label class="form-label font-weight-bold">Trạng Thái</label>
                <select v-model="form.status" class="form-control">
                  <option value="ACTIVE">HOẠT ĐỘNG (ACTIVE)</option>
                  <option value="INACTIVE">VÔ HIỆU (INACTIVE)</option>
                </select>
              </div>
            </div>

            <!-- Mô tả ngắn -->
            <div class="form-group mb-3">
              <label class="form-label font-weight-bold">Mô Tả Ngắn</label>
              <input
                v-model="form.shortDescription"
                type="text"
                class="form-control"
                placeholder="Ví dụ: Áo khoác da chất liệu piqué nam tính cao cấp"
              />
            </div>

            <!-- Mô tả chi tiết -->
            <div class="form-group mb-3">
              <label class="form-label font-weight-bold">Mô Tả Chi Tiết Sản Phẩm</label>
              <textarea
                v-model="form.description"
                rows="4"
                class="form-control"
                placeholder="Nhập thông số kỹ thuật, chất liệu vải, hướng dẫn giặt ủi và chi tiết sản phẩm..."
              ></textarea>
            </div>

          </div>

          <div class="modal-footer-custom">
            <button type="button" class="btn btn-secondary mr-2" @click="isModalOpen = false">Hủy Bỏ</button>
            <button type="submit" class="btn btn-primary d-flex align-items-center gap-2" :disabled="isSubmitting">
              <span v-if="isSubmitting" class="spinner-border spinner-border-sm"></span>
              <span>{{ isSubmitting ? 'ĐANG LƯU...' : 'LƯU SẢN PHẨM' }}</span>
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

/* Product thumbnail style */
.product-thumb-box {
  width: 50px;
  height: 50px;
  border-radius: 6px;
  overflow: hidden;
  margin: 0 auto;
  border: 1px solid #e2e8f0;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.product-thumb-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-thumb-placeholder {
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
  max-width: 720px;
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
