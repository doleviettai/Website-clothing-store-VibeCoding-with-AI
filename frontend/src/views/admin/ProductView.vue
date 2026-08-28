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
  stockQuantity: 10,
  availableSizes: 'S, M, L, XL, XXL',
  availableColors: 'Đen, Trắng, Xanh Navy, Xám',
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

// Danh sách gợi ý Kích cỡ & Màu sắc nhanh
const presetSizes = ['S', 'M', 'L', 'XL', 'XXL', '28', '29', '30', '31', '32', '33', '34']
const presetColors = ['Đen', 'Trắng', 'Xanh Navy', 'Xám', 'Đỏ', 'Vàng', 'Beige', 'Xanh Lá']

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
  form.availableSizes = 'S, M, L, XL, XXL'
  form.availableColors = 'Đen, Trắng, Xanh Navy, Xám'
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
  form.availableSizes = product.availableSizes || 'S, M, L, XL, XXL'
  form.availableColors = product.availableColors || 'Đen, Trắng, Xanh Navy, Xám'
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

// Thêm/Xóa Nhanh Kích cỡ
const togglePresetSize = (size) => {
  let sizes = form.availableSizes ? form.availableSizes.split(',').map(s => s.trim()).filter(Boolean) : []
  if (sizes.includes(size)) {
    sizes = sizes.filter(s => s !== size)
  } else {
    sizes.push(size)
  }
  form.availableSizes = sizes.join(', ')
}

// Thêm/Xóa Nhanh Màu sắc
const togglePresetColor = (color) => {
  let colors = form.availableColors ? form.availableColors.split(',').map(c => c.trim()).filter(Boolean) : []
  if (colors.includes(color)) {
    colors = colors.filter(c => c !== color)
  } else {
    colors.push(color)
  }
  form.availableColors = colors.join(', ')
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
      availableSizes: form.availableSizes,
      availableColors: form.availableColors,
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

// Xử lý Xóa sản phẩm
const handleDelete = async (product) => {
  if (!confirm(`Bạn có chắc chắn muốn xóa sản phẩm "${product.name}"?`)) return

  try {
    await productApi.deleteProduct(product.id)
    successMessage.value = 'Xóa sản phẩm thành công!'
    await fetchProducts()
    setTimeout(() => {
      successMessage.value = ''
    }, 3000)
  } catch (err) {
    errorMessage.value = err.response?.data?.message || 'Không thể xóa sản phẩm.'
  }
}

// Live Search AJAX Debounce
let searchTimer = null
watch(
  () => filters.keyword,
  () => {
    clearTimeout(searchTimer)
    searchTimer = setTimeout(() => {
      filters.page = 0
      fetchProducts()
    }, 300)
  }
)

watch([() => filters.categoryId, () => filters.brandId, () => filters.status], () => {
  filters.page = 0
  fetchProducts()
})

const changePage = (newPage) => {
  if (newPage >= 0 && newPage < pagination.totalPages) {
    filters.page = newPage
    fetchProducts()
  }
}

const formatPrice = (val) => {
  if (val === null || val === undefined) return '0 đ'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)
}

onMounted(() => {
  fetchProducts()
  fetchDropdownData()
})
</script>

<template>
  <div class="product-admin-view p-4">
    <!-- Header -->
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h3 class="font-weight-bold text-dark m-0">
          <i class="fa fa-cubes text-danger mr-2"></i> QUẢN LÝ SẢN PHẨM & BIẾN THỂ (PRODUCT ADMIN)
        </h3>
        <p class="text-muted small m-0">Quản lý thêm, xem, sửa, xóa sản phẩm, giá bán, tồn kho, kích cỡ và màu sắc.</p>
      </div>
      <button class="btn btn-danger font-weight-bold shadow-sm" @click="openAddModal">
        <i class="fa fa-plus-circle mr-1"></i> Thêm Sản Phẩm Mới
      </button>
    </div>

    <!-- Alert Messages -->
    <div v-if="successMessage" class="alert alert-success alert-dismissible fade show" role="alert">
      <i class="fa fa-check-circle mr-2"></i> {{ successMessage }}
    </div>
    <div v-if="errorMessage" class="alert alert-danger alert-dismissible fade show" role="alert">
      <i class="fa fa-exclamation-triangle mr-2"></i> {{ errorMessage }}
    </div>

    <!-- Thanh Tìm Kiếm & Bộ Lọc -->
    <div class="card border-0 shadow-sm rounded-lg mb-4">
      <div class="card-body bg-light rounded-lg">
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
                placeholder="Nhập tên sản phẩm để tìm kiếm..."
              />
            </div>
          </div>
          <div class="col-md-3">
            <select v-model="filters.categoryId" class="form-control">
              <option value="">-- Tất cả Chuyên Mục --</option>
              <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
            </select>
          </div>
          <div class="col-md-3">
            <select v-model="filters.brandId" class="form-control">
              <option value="">-- Tất cả Thương Hiệu --</option>
              <option v-for="brand in brands" :key="brand.id" :value="brand.id">{{ brand.name }}</option>
            </select>
          </div>
          <div class="col-md-2">
            <select v-model="filters.status" class="form-control">
              <option value="">-- Trạng Thái --</option>
              <option value="ACTIVE">HOẠT ĐỘNG</option>
              <option value="INACTIVE">VÔ HIỆU</option>
            </select>
          </div>
        </div>
      </div>
    </div>

    <!-- Bảng Danh Sách Sản Phẩm -->
    <div class="card border-0 shadow-sm rounded-lg overflow-hidden">
      <div class="card-body p-0">
        <div v-if="isLoading" class="py-5 text-center">
          <LoadingSpinner text="Đang tải dữ liệu sản phẩm..." />
        </div>

        <div v-else class="table-responsive">
          <table class="table table-hover align-middle m-0 text-center">
            <thead class="table-dark text-uppercase small">
              <tr>
                <th style="width: 60px;">ID</th>
                <th style="width: 80px;">Hình Ảnh</th>
                <th class="text-left">Tên Sản Phẩm</th>
                <th>Chuyên Mục</th>
                <th>Thương Hiệu</th>
                <th>Giá Bán</th>
                <th>Tồn Kho</th>
                <th>Kích Cỡ</th>
                <th>Màu Sắc</th>
                <th>Đánh Giá</th>
                <th>Trạng Thái</th>
                <th style="width: 120px;">Thao Tác</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="product in products" :key="product.id">
                <td class="font-weight-bold text-muted">#{{ product.id }}</td>
                <td>
                  <div class="product-thumb-box">
                    <img v-if="product.thumbnailUrl" :src="product.thumbnailUrl" :alt="product.name" class="product-thumb-img" />
                    <div v-else class="product-thumb-placeholder"><i class="fa fa-shopping-bag"></i></div>
                  </div>
                </td>
                <td class="text-left">
                  <span class="font-weight-bold text-dark d-block">{{ product.name }}</span>
                  <code class="small text-muted">{{ product.slug }}</code>
                </td>
                <td><span class="badge bg-light text-dark border">{{ product.categoryName || '-' }}</span></td>
                <td><span class="badge bg-light text-dark border">{{ product.brandName || '-' }}</span></td>
                <td>
                  <span class="font-weight-bold text-danger d-block">{{ formatPrice(product.price) }}</span>
                  <small v-if="product.salePrice" class="text-muted text-decoration-line-through">{{ formatPrice(product.salePrice) }}</small>
                </td>
                <td>
                  <span class="badge" :class="product.stockQuantity > 0 ? 'bg-info text-white' : 'bg-secondary text-white'">
                    {{ product.stockQuantity }} cái
                  </span>
                </td>
                <!-- Kích cỡ -->
                <td>
                  <div class="d-flex flex-wrap gap-1 justify-content-center">
                    <span v-for="sz in (product.availableSizes || 'S, M, L, XL').split(',')" :key="sz" class="badge bg-dark text-white border">
                      {{ sz.trim() }}
                    </span>
                  </div>
                </td>
                <!-- Màu sắc -->
                <td>
                  <div class="d-flex flex-wrap gap-1 justify-content-center">
                    <span v-for="cl in (product.availableColors || 'Đen, Trắng').split(',')" :key="cl" class="badge bg-secondary text-white">
                      {{ cl.trim() }}
                    </span>
                  </div>
                </td>
                <td>
                  <div class="small text-warning font-weight-bold"><i class="fa fa-star"></i> {{ product.averageRating || '5.0' }}</div>
                </td>
                <td>
                  <span class="badge" :class="product.status === 'ACTIVE' ? 'bg-success text-white' : 'bg-danger text-white'">
                    {{ product.status === 'ACTIVE' ? 'HOẠT ĐỘNG' : 'VÔ HIỆU' }}
                  </span>
                </td>
                <td>
                  <button class="btn btn-sm btn-outline-info mr-1" @click="openEditModal(product)" title="Sửa">
                    <i class="fa fa-pencil"></i>
                  </button>
                  <button class="btn btn-sm btn-outline-danger" @click="handleDelete(product)" title="Xóa">
                    <i class="fa fa-trash"></i>
                  </button>
                </td>
              </tr>
              <tr v-if="products.length === 0">
                <td colspan="12" class="text-center py-5 text-muted">
                  <i class="fa fa-shopping-bag fa-2x mb-2 d-block"></i> Không tìm thấy sản phẩm nào.
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Phân Trang -->
      <div v-if="pagination.totalPages > 1" class="card-footer bg-white d-flex justify-content-between align-items-center py-3">
        <small class="text-muted">Trang {{ filters.page + 1 }} / {{ pagination.totalPages }} (Tổng {{ pagination.totalElements }} sản phẩm)</small>
        <ul class="pagination pagination-sm m-0">
          <li class="page-item" :class="{ disabled: filters.page === 0 }">
            <button class="page-link" @click="changePage(filters.page - 1)">Trước</button>
          </li>
          <li v-for="p in pagination.totalPages" :key="p" class="page-item" :class="{ active: filters.page === p - 1 }">
            <button class="page-link" @click="changePage(p - 1)">{{ p }}</button>
          </li>
          <li class="page-item" :class="{ disabled: filters.page === pagination.totalPages - 1 }">
            <button class="page-link" @click="changePage(filters.page + 1)">Sau</button>
          </li>
        </ul>
      </div>
    </div>

    <!-- MODAL THÊM / SỬA SẢN PHẨM -->
    <div v-if="isModalOpen" class="custom-modal-backdrop">
      <div class="custom-modal-dialog">
        <div class="custom-modal-content">
          <div class="modal-header bg-dark text-white">
            <h5 class="modal-title font-weight-bold">
              <i class="fa" :class="isEditing ? 'fa-pencil' : 'fa-plus-circle'"></i>
              {{ isEditing ? 'Chỉnh Sửa Sản Phẩm' : 'Thêm Sản Phẩm Mới' }}
            </h5>
            <button type="button" class="btn-close btn-close-white" @click="isModalOpen = false"></button>
          </div>

          <div class="modal-body p-4" style="max-height: 75vh; overflow-y: auto;">
            <form @submit.prevent="handleSubmit">
              <div class="row g-3">
                <!-- Tên Sản Phẩm -->
                <div class="col-md-6">
                  <label class="form-label font-weight-bold">Tên Sản Phẩm <span class="text-danger">*</span></label>
                  <input v-model="form.name" type="text" class="form-control" placeholder="Ví dụ: Áo Sơ Mi Nam Oxford" @input="onNameChange" />
                  <small v-if="formErrors.name" class="text-danger">{{ formErrors.name }}</small>
                </div>

                <!-- Slug -->
                <div class="col-md-6">
                  <label class="form-label font-weight-bold">Slug URL</label>
                  <input v-model="form.slug" type="text" class="form-control bg-light" placeholder="ao-so-mi-nam-oxford" />
                </div>

                <!-- Chuyên Mục -->
                <div class="col-md-6">
                  <label class="form-label font-weight-bold">Chuyên Mục (Category)</label>
                  <select v-model="form.categoryId" class="form-control">
                    <option value="">-- Chọn Chuyên Mục --</option>
                    <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
                  </select>
                </div>

                <!-- Thương Hiệu -->
                <div class="col-md-6">
                  <label class="form-label font-weight-bold">Thương Hiệu (Brand)</label>
                  <select v-model="form.brandId" class="form-control">
                    <option value="">-- Chọn Thương Hiệu --</option>
                    <option v-for="brand in brands" :key="brand.id" :value="brand.id">{{ brand.name }}</option>
                  </select>
                </div>

                <!-- Giá Bán Niêm Yết -->
                <div class="col-md-4">
                  <label class="form-label font-weight-bold">Giá Bán Niêm Yết (VND) <span class="text-danger">*</span></label>
                  <input v-model.number="form.price" type="number" class="form-control" placeholder="500000" min="0" />
                  <small v-if="formErrors.price" class="text-danger">{{ formErrors.price }}</small>
                </div>

                <!-- Giá Khuyến Mãi (Sale Price) -->
                <div class="col-md-4">
                  <label class="form-label font-weight-bold">Giá Khuyến Mãi (Khuyết)</label>
                  <input v-model.number="form.salePrice" type="number" class="form-control" placeholder="Để trống nếu không giảm giá" min="0" />
                </div>

                <!-- Số Lượng Tồn Kho -->
                <div class="col-md-4">
                  <label class="form-label font-weight-bold">Số Lượng Tồn Kho <span class="text-danger">*</span></label>
                  <input v-model.number="form.stockQuantity" type="number" class="form-control" placeholder="10" min="0" />
                </div>

                <!-- NÂNG CẤP: KÍCH CỠ SẢN PHẨM (SIZES) -->
                <div class="col-md-6">
                  <label class="form-label font-weight-bold">
                    <i class="fa fa-ruler-combined text-primary mr-1"></i> Các Kích Cỡ Sản Phẩm (Sizes)
                  </label>
                  <input v-model="form.availableSizes" type="text" class="form-control mb-2" placeholder="Ví dụ: S, M, L, XL, XXL" />
                  <div class="d-flex flex-wrap gap-1">
                    <button
                      v-for="sz in presetSizes"
                      :key="sz"
                      type="button"
                      class="btn btn-xs btn-outline-dark"
                      :class="{ active: form.availableSizes && form.availableSizes.includes(sz) }"
                      @click="togglePresetSize(sz)"
                    >
                      + {{ sz }}
                    </button>
                  </div>
                </div>

                <!-- NÂNG CẤP: MÀU SẮC SẢN PHẨM (COLORS) -->
                <div class="col-md-6">
                  <label class="form-label font-weight-bold">
                    <i class="fa fa-paint-brush text-warning mr-1"></i> Các Màu Sắc Sản Phẩm (Colors)
                  </label>
                  <input v-model="form.availableColors" type="text" class="form-control mb-2" placeholder="Ví dụ: Đen, Trắng, Xanh Navy, Xám" />
                  <div class="d-flex flex-wrap gap-1">
                    <button
                      v-for="cl in presetColors"
                      :key="cl"
                      type="button"
                      class="btn btn-xs btn-outline-secondary"
                      :class="{ active: form.availableColors && form.availableColors.includes(cl) }"
                      @click="togglePresetColor(cl)"
                    >
                      + {{ cl }}
                    </button>
                  </div>
                </div>

                <!-- Chọn Tệp Ảnh Sản Phẩm -->
                <div class="col-md-12">
                  <label class="form-label font-weight-bold">Ảnh Tượng Trưng Sản Phẩm (Cloudinary)</label>
                  <input type="file" accept="image/*" class="form-control mb-2" @change="handleImageSelect" />
                  <div v-if="imagePreviewUrl" class="mt-2 text-center p-2 border rounded bg-light">
                    <img :src="imagePreviewUrl" alt="Preview" style="max-height: 120px; object-fit: contain;" />
                  </div>
                </div>

                <!-- Mô Tả Ngắn -->
                <div class="col-md-12">
                  <label class="form-label font-weight-bold">Mô Tả Ngắn</label>
                  <input v-model="form.shortDescription" type="text" class="form-control" placeholder="Ví dụ: Áo thun nam chất liệu 100% cotton thoáng mát" />
                </div>

                <!-- Mô Tả Chi Tiết -->
                <div class="col-md-12">
                  <label class="form-label font-weight-bold">Mô Tả Chi Tiết</label>
                  <textarea v-model="form.description" class="form-control" rows="3" placeholder="Chi tiết chất liệu, hướng dẫn giặt là..."></textarea>
                </div>

                <!-- Trạng Thái -->
                <div class="col-md-6">
                  <label class="form-label font-weight-bold">Trạng Thái</label>
                  <select v-model="form.status" class="form-control">
                    <option value="ACTIVE">HOẠT ĐỘNG (ACTIVE)</option>
                    <option value="INACTIVE">VÔ HIỆU (INACTIVE)</option>
                  </select>
                </div>

                <!-- Sản phẩm Nổi bật -->
                <div class="col-md-6 d-flex align-items-center mt-4">
                  <div class="form-check">
                    <input id="isFeatured" v-model="form.isFeatured" type="checkbox" class="form-check-input" />
                    <label for="isFeatured" class="form-check-label font-weight-bold cursor-pointer">Sản Phẩm Nổi Bật (Featured Product)</label>
                  </div>
                </div>
              </div>

              <!-- Action Buttons -->
              <div class="d-flex justify-content-end gap-2 mt-4 pt-3 border-top">
                <button type="button" class="btn btn-secondary" @click="isModalOpen = false">Hủy Bỏ</button>
                <button type="submit" class="btn btn-danger font-weight-bold" :disabled="isSubmitting">
                  <i class="fa fa-save mr-1"></i> {{ isSubmitting ? 'Đang Lưu...' : 'Lưu Sản Phẩm' }}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.product-thumb-box {
  width: 48px;
  height: 48px;
  margin: 0 auto;
  border-radius: 6px;
  overflow: hidden;
  border: 1px solid #dee2e6;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8f9fa;
}
.product-thumb-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.custom-modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1050;
}
.custom-modal-dialog {
  width: 100%;
  max-width: 800px;
  padding: 15px;
}
.custom-modal-content {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
}
.cursor-pointer {
  cursor: pointer;
}
.btn-xs {
  padding: 2px 8px;
  font-size: 0.75rem;
  border-radius: 4px;
}
</style>
