<script setup>
defineProps({
  brands: {
    type: Array,
    default: () => [],
  },
  selectedBrandId: {
    type: [Number, String, null],
    default: null,
  },
})

const emit = defineEmits(['selectBrand'])
</script>

<template>
  <div class="shop-sidebar-brand mb-4">
    <h6 class="text-uppercase font-weight-bold text-dark mb-3">
      <i class="fa fa-tags mr-2 text-danger"></i> Thương Hiệu (Brand)
    </h6>
    <ul class="list-group list-group-flush border rounded shadow-sm overflow-hidden">
      <!-- Nút Chọn Tất Cả -->
      <li
        class="list-group-item list-group-item-action d-flex justify-content-between align-items-center cursor-pointer"
        :class="{ active: selectedBrandId === null || selectedBrandId === '' }"
        @click="emit('selectBrand', null)"
      >
        <span>Tất Cả Thương Hiệu (All)</span>
        <i class="fa fa-check text-success" v-if="selectedBrandId === null || selectedBrandId === ''"></i>
      </li>

      <!-- Danh sách Thương hiệu nạp động từ Database API -->
      <li
        v-for="brand in brands"
        :key="brand.id"
        class="list-group-item list-group-item-action d-flex justify-content-between align-items-center cursor-pointer"
        :class="{ active: Number(selectedBrandId) === Number(brand.id) }"
        @click="emit('selectBrand', brand.id)"
      >
        <span>{{ brand.name }}</span>
        <i class="fa fa-check text-success" v-if="Number(selectedBrandId) === Number(brand.id)"></i>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.cursor-pointer {
  cursor: pointer;
}
.list-group-item.active {
  background-color: #111111 !important;
  border-color: #111111 !important;
  color: #ffffff !important;
  font-weight: bold;
}
</style>
