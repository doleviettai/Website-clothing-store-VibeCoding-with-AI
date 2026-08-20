<script setup>
import { ref } from 'vue'

const products = ref([
  { id: 1, name: 'Áo Khoác Biker Piqué', category: 'Áo Nam', brand: 'Zara', price: 1550000, stock: 45, rating: 5.0, favorites: 12, status: 'ACTIVE' },
  { id: 2, name: 'Áo Thun Basic Cotton Unisex', category: 'Áo Nam', brand: 'Uniqlo', price: 350000, stock: 120, rating: 4.8, favorites: 40, status: 'ACTIVE' },
  { id: 3, name: 'Áo Sơ Mi Nam Tay Dài Oxford', category: 'Áo Nam', brand: 'Zara', price: 620000, stock: 28, rating: 5.0, favorites: 18, status: 'ACTIVE' },
  { id: 4, name: 'Quần Jean Nam Skinny Stretch', category: 'Quần Nam', brand: 'Nike', price: 890000, stock: 15, rating: 4.5, favorites: 9, status: 'ACTIVE' },
])

const formatPrice = (val) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)
</script>

<template>
  <div class="admin-page">
    <div class="d-flex align-items-center justify-content-between mb-4">
      <div>
        <h3 class="page-title">Quản Lý Sản Phẩm</h3>
        <p class="text-muted mb-0">Quản lý danh sách sản phẩm, biến thể màu sắc, kích thước và tồn kho</p>
      </div>
      <button class="btn btn-primary">
        <i class="fa fa-plus mr-1"></i> Thêm Sản Phẩm Mới
      </button>
    </div>

    <div class="card border-0 shadow-sm">
      <div class="card-body p-0">
        <div class="table-responsive">
          <table class="table table-hover align-middle mb-0 text-center">
            <thead class="bg-light">
              <tr>
                <th>ID</th>
                <th>Tên Sản Phẩm</th>
                <th>Chuyên Mục</th>
                <th>Thương Hiệu</th>
                <th>Giá Bán</th>
                <th>Tồn Kho</th>
                <th>Đánh Giá</th>
                <th>Yêu Thích</th>
                <th>Trạng Thái</th>
                <th>Hành Động</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="p in products" :key="p.id">
                <td>#{{ p.id }}</td>
                <td class="font-weight-bold text-dark text-left">{{ p.name }}</td>
                <td>{{ p.category }}</td>
                <td>{{ p.brand }}</td>
                <td class="font-weight-bold text-danger">{{ formatPrice(p.price) }}</td>
                <td>
                  <span :class="p.stock <= 20 ? 'text-danger font-weight-bold' : ''">{{ p.stock }}</span>
                </td>
                <td><i class="fa fa-star text-warning"></i> {{ p.rating }}</td>
                <td><i class="fa fa-heart text-danger"></i> {{ p.favorites }}</td>
                <td><span class="badge bg-success text-white">ACTIVE</span></td>
                <td>
                  <button class="btn btn-sm btn-outline-info mr-1">Sửa</button>
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
