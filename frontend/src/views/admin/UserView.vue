<script setup>
import { ref } from 'vue'

const users = ref([
  { id: 1, fullName: 'Quản Trị Viên Administrator', email: 'admin@clothingstore.com', phone: '0900000000', roles: ['ADMIN'], status: 'ACTIVE' },
  { id: 2, fullName: 'Nguyễn Văn A', email: 'client@example.com', phone: '0901234567', roles: ['CLIENT'], status: 'ACTIVE' },
  { id: 3, fullName: 'Trần Thị B', email: 'tranthib@example.com', phone: '0912345678', roles: ['CLIENT'], status: 'LOCKED' },
])

const toggleLock = (user) => {
  if (user.roles.includes('ADMIN')) {
    alert('Không thể khóa tài khoản Administrator!')
    return
  }
  user.status = user.status === 'ACTIVE' ? 'LOCKED' : 'ACTIVE'
  alert(`Đã ${user.status === 'LOCKED' ? 'KHÓA' : 'MỞ KHÓA'} tài khoản ${user.email}`)
}
</script>

<template>
  <div class="admin-page">
    <div class="d-flex align-items-center justify-content-between mb-4">
      <div>
        <h3 class="page-title">Quản Lý Người Dùng</h3>
        <p class="text-muted mb-0">Quản lý danh sách tài khoản khách hàng và quản trị viên</p>
      </div>
    </div>

    <div class="card border-0 shadow-sm">
      <div class="card-body p-0">
        <div class="table-responsive">
          <table class="table table-hover align-middle mb-0 text-center">
            <thead class="bg-light">
              <tr>
                <th>ID</th>
                <th>Họ và Tên</th>
                <th>Email</th>
                <th>Số Điện Thoại</th>
                <th>Vai Trò</th>
                <th>Trạng Thái</th>
                <th>Hành Động</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="u in users" :key="u.id">
                <td>#{{ u.id }}</td>
                <td class="font-weight-bold text-dark">{{ u.fullName }}</td>
                <td>{{ u.email }}</td>
                <td>{{ u.phone || 'Chưa cập nhật' }}</td>
                <td>
                  <span class="badge mr-1" :class="r === 'ADMIN' ? 'bg-danger text-white' : 'bg-info text-white'" v-for="r in u.roles" :key="r">
                    {{ r }}
                  </span>
                </td>
                <td>
                  <span class="badge" :class="u.status === 'ACTIVE' ? 'bg-success text-white' : 'bg-danger text-white'">
                    {{ u.status === 'ACTIVE' ? 'HOẠT ĐỘNG' : 'BỊ KHÓA' }}
                  </span>
                </td>
                <td>
                  <button
                    v-if="!u.roles.includes('ADMIN')"
                    class="btn btn-sm"
                    :class="u.status === 'ACTIVE' ? 'btn-outline-danger' : 'btn-outline-success'"
                    @click="toggleLock(u)"
                  >
                    {{ u.status === 'ACTIVE' ? 'Khóa tài khoản' : 'Mở khóa' }}
                  </button>
                  <span v-else class="text-muted small">Hệ thống</span>
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
