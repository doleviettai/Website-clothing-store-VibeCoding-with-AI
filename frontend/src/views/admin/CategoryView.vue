<script setup>
import { ref } from 'vue'

const categories = ref([
  { id: 1, name: 'Thời Trang Nam', slug: 'thoi-trang-nam', parent: 'Gốc', status: 'ACTIVE', sortOrder: 1 },
  { id: 2, name: 'Áo Nam', slug: 'ao-nam', parent: 'Thời Trang Nam', status: 'ACTIVE', sortOrder: 2 },
  { id: 3, name: 'Áo Thun & Polo', slug: 'ao-thun-polo', parent: 'Áo Nam', status: 'ACTIVE', sortOrder: 3 },
  { id: 4, name: 'Áo Sơ Mi', slug: 'ao-so-mi', parent: 'Áo Nam', status: 'ACTIVE', sortOrder: 4 },
  { id: 5, name: 'Quần Nam', slug: 'quan-nam', parent: 'Thời Trang Nam', status: 'ACTIVE', sortOrder: 5 },
])

const isModalOpen = ref(false)
const editingCategory = ref(null)

const openAddModal = () => {
  editingCategory.value = { name: '', slug: '', parentId: null, status: 'ACTIVE', sortOrder: 1 }
  isModalOpen.value = true
}

const openEditModal = (cat) => {
  editingCategory.value = { ...cat }
  isModalOpen.value = true
}

const saveCategory = () => {
  alert('Đã lưu thông tin chuyên mục thành công!')
  isModalOpen.value = false
}
</script>

<template>
  <div class="admin-page">
    <div class="d-flex align-items-center justify-content-between mb-4">
      <div>
        <h3 class="page-title">Quản Lý Chuyên Mục</h3>
        <p class="text-muted mb-0">Danh sách chuyên mục sản phẩm phân cấp trong cửa hàng</p>
      </div>
      <button class="btn btn-primary" @click="openAddModal">
        <i class="fa fa-plus mr-1"></i> Thêm Chuyên Mục
      </button>
    </div>

    <div class="card border-0 shadow-sm">
      <div class="card-body p-0">
        <div class="table-responsive">
          <table class="table table-hover align-middle mb-0 text-center">
            <thead class="bg-light">
              <tr>
                <th>ID</th>
                <th>Tên Chuyên Mục</th>
                <th>Slug</th>
                <th>Chuyên Mục Cha</th>
                <th>Thứ Tự</th>
                <th>Trạng Thái</th>
                <th>Hành Động</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="cat in categories" :key="cat.id">
                <td>#{{ cat.id }}</td>
                <td class="font-weight-bold text-dark">{{ cat.name }}</td>
                <td><code>{{ cat.slug }}</code></td>
                <td>{{ cat.parent }}</td>
                <td>{{ cat.sortOrder }}</td>
                <td>
                  <span class="badge bg-success text-white">ACTIVE</span>
                </td>
                <td>
                  <button class="btn btn-sm btn-outline-info mr-2" @click="openEditModal(cat)">Sửa</button>
                  <button class="btn btn-sm btn-outline-danger">Xóa</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Modal Mockup -->
    <div v-if="isModalOpen" class="modal-backdrop-custom">
      <div class="modal-dialog-custom">
        <div class="modal-header-custom">
          <h5>{{ editingCategory?.id ? 'Chỉnh Sửa Chuyên Mục' : 'Thêm Chuyên Mục Mới' }}</h5>
          <button class="close-btn" @click="isModalOpen = false">&times;</button>
        </div>
        <div class="modal-body-custom">
          <div class="form-group mb-3">
            <label>Tên chuyên mục</label>
            <input v-model="editingCategory.name" type="text" class="form-control" placeholder="Ví dụ: Áo Khoác Nam">
          </div>
          <div class="form-group mb-3">
            <label>Slug đường dẫn</label>
            <input v-model="editingCategory.slug" type="text" class="form-control" placeholder="ao-khoac-nam">
          </div>
        </div>
        <div class="modal-footer-custom">
          <button class="btn btn-secondary mr-2" @click="isModalOpen = false">Hủy</button>
          <button class="btn btn-primary" @click="saveCategory">Lưu Dữ Liệu</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-title { font-size: 1.5rem; font-weight: 700; color: #111827; }
.modal-backdrop-custom {
  position: fixed; inset: 0; background: rgba(0,0,0,0.5); z-index: 9999;
  display: flex; align-items: center; justify-content: center;
}
.modal-dialog-custom { background: white; border-radius: 12px; width: 450px; padding: 24px; }
.modal-header-custom { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.close-btn { border: none; background: none; font-size: 24px; cursor: pointer; }
.modal-footer-custom { display: flex; justify-content: flex-end; margin-top: 20px; }
</style>
