<script setup>
import { ref } from 'vue'

const banners = ref([
  { id: 1, title: 'Summer Collection 2030', position: 'HOME_TOP', image: '/img/hero/hero-1.jpg', status: 'ACTIVE', sortOrder: 1 },
  { id: 2, title: 'Clothing Collections 2030', position: 'HOME_MIDDLE', image: '/img/banner/banner-1.jpg', status: 'ACTIVE', sortOrder: 2 },
  { id: 3, title: 'Accessories Sale', position: 'CATEGORY_TOP', image: '/img/banner/banner-2.jpg', status: 'ACTIVE', sortOrder: 3 },
])
</script>

<template>
  <div class="admin-page">
    <div class="d-flex align-items-center justify-content-between mb-4">
      <div>
        <h3 class="page-title">Quản Lý Banner Quảng Cáo</h3>
        <p class="text-muted mb-0">Quản lý banner slider và quảng cáo trên các vị trí trang chủ</p>
      </div>
      <button class="btn btn-primary">
        <i class="fa fa-plus mr-1"></i> Thêm Banner Mới
      </button>
    </div>

    <div class="card border-0 shadow-sm">
      <div class="card-body p-0">
        <div class="table-responsive">
          <table class="table table-hover align-middle mb-0 text-center">
            <thead class="bg-light">
              <tr>
                <th>ID</th>
                <th>Hình Ảnh</th>
                <th>Tiêu Đề Banner</th>
                <th>Vị Trí</th>
                <th>Thứ Tự</th>
                <th>Trạng Thái</th>
                <th>Hành Động</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="b in banners" :key="b.id">
                <td>#{{ b.id }}</td>
                <td>
                  <img :src="b.image" style="width: 80px; height: 45px; object-fit: cover; border-radius: 4px;">
                </td>
                <td class="font-weight-bold text-dark">{{ b.title }}</td>
                <td><span class="badge bg-secondary text-white">{{ b.position }}</span></td>
                <td>{{ b.sortOrder }}</td>
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
