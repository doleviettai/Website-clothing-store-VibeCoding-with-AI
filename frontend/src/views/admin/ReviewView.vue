<script setup>
import { ref } from 'vue'

const reviews = ref([
  { id: 1, product: 'Áo Khoác Biker Piqué', user: 'Nguyễn Văn A', rating: 5, content: 'Chất vải rất tốt, giao hàng nhanh!', status: 'VISIBLE', date: '19/08/2026' },
  { id: 2, product: 'Áo Thun Basic Cotton Unisex', user: 'Trần Thị B', rating: 4, content: 'Mặc thoải mái, đúng kích thước.', status: 'VISIBLE', date: '18/08/2026' },
  { id: 3, product: 'Quần Jean Nam Skinny Stretch', user: 'Lê Hoàng C', rating: 1, content: 'Quần bị chật so với mô tả.', status: 'HIDDEN', date: '17/08/2026' },
])

const toggleStatus = (rev) => {
  rev.status = rev.status === 'VISIBLE' ? 'HIDDEN' : 'VISIBLE'
}
</script>

<template>
  <div class="admin-page">
    <div class="d-flex align-items-center justify-content-between mb-4">
      <div>
        <h3 class="page-title">Quản Lý Đánh Giá</h3>
        <p class="text-muted mb-0">Quản lý nhận xét và đánh giá sản phẩm của khách hàng</p>
      </div>
    </div>

    <div class="card border-0 shadow-sm">
      <div class="card-body p-0">
        <div class="table-responsive">
          <table class="table table-hover align-middle mb-0 text-center">
            <thead class="bg-light">
              <tr>
                <th>ID</th>
                <th>Sản Phẩm</th>
                <th>Người Đánh Giá</th>
                <th>Điểm Số</th>
                <th>Nội Dung</th>
                <th>Ngày Đánh Giá</th>
                <th>Trạng Thái</th>
                <th>Hành Động</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="rev in reviews" :key="rev.id">
                <td>#{{ rev.id }}</td>
                <td class="font-weight-bold">{{ rev.product }}</td>
                <td>{{ rev.user }}</td>
                <td>
                  <span class="text-warning font-weight-bold"><i class="fa fa-star"></i> {{ rev.rating }}/5</span>
                </td>
                <td class="text-left" style="max-width: 300px;">{{ rev.content }}</td>
                <td>{{ rev.date }}</td>
                <td>
                  <span class="badge" :class="rev.status === 'VISIBLE' ? 'bg-success text-white' : 'bg-secondary text-white'">
                    {{ rev.status === 'VISIBLE' ? 'HIỂN THỊ' : 'ĐÃ ẨN' }}
                  </span>
                </td>
                <td>
                  <button
                    class="btn btn-sm"
                    :class="rev.status === 'VISIBLE' ? 'btn-outline-warning' : 'btn-outline-success'"
                    @click="toggleStatus(rev)"
                  >
                    {{ rev.status === 'VISIBLE' ? 'Ẩn đánh giá' : 'Hiển thị lại' }}
                  </button>
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
