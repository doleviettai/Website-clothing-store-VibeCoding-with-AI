<script setup>
import { ref } from 'vue'

const brands = ref([
  { id: 1, name: 'Nike', slug: 'nike', logo: '/img/clients/client-1.png', status: 'ACTIVE' },
  { id: 2, name: 'Adidas', slug: 'adidas', logo: '/img/clients/client-2.png', status: 'ACTIVE' },
  { id: 3, name: 'Uniqlo', slug: 'uniqlo', logo: '/img/clients/client-3.png', status: 'ACTIVE' },
  { id: 4, name: 'Zara', slug: 'zara', logo: '/img/clients/client-4.png', status: 'ACTIVE' },
])

const isModalOpen = ref(false)

const openModal = () => { isModalOpen.value = true }
const closeModal = () => { isModalOpen.value = false }
</script>

<template>
  <div class="admin-page">
    <div class="d-flex align-items-center justify-content-between mb-4">
      <div>
        <h3 class="page-title">Quản Lý Thương Hiệu</h3>
        <p class="text-muted mb-0">Quản lý đối tác và thương hiệu sản phẩm</p>
      </div>
      <button class="btn btn-primary" @click="openModal">
        <i class="fa fa-plus mr-1"></i> Thêm Thương Hiệu
      </button>
    </div>

    <div class="card border-0 shadow-sm">
      <div class="card-body p-0">
        <div class="table-responsive">
          <table class="table table-hover align-middle mb-0 text-center">
            <thead class="bg-light">
              <tr>
                <th>ID</th>
                <th>Tên Thương Hiệu</th>
                <th>Slug</th>
                <th>Trạng Thái</th>
                <th>Hành Động</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="brand in brands" :key="brand.id">
                <td>#{{ brand.id }}</td>
                <td class="font-weight-bold text-dark">{{ brand.name }}</td>
                <td><code>{{ brand.slug }}</code></td>
                <td><span class="badge bg-success text-white">ACTIVE</span></td>
                <td>
                  <button class="btn btn-sm btn-outline-info mr-2">Sửa</button>
                  <button class="btn btn-sm btn-outline-danger">Xóa</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-title { font-size: 1.5rem; font-weight: 700; color: #111827; }
</style>
