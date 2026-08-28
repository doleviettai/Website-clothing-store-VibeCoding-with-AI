<script setup>
defineProps({
  categories: {
    type: Array,
    default: () => [],
  },
  selectedCategoryId: {
    type: [Number, String, null],
    default: null,
  },
})

const emit = defineEmits(['selectCategory'])
</script>

<template>
  <div class="shop-sidebar-category mb-4">
    <h6 class="text-uppercase font-weight-bold text-dark mb-3">
      <i class="fa fa-th-list mr-2 text-danger"></i> Chuyên Mục (Category)
    </h6>
    <ul class="list-group list-group-flush border rounded shadow-sm overflow-hidden">
      <!-- Nút Chọn Tất Cả -->
      <li
        class="list-group-item list-group-item-action d-flex justify-content-between align-items-center cursor-pointer"
        :class="{ active: selectedCategoryId === null || selectedCategoryId === '' }"
        @click="emit('selectCategory', null)"
      >
        <span>Tất Cả Chuyên Mục (All)</span>
        <i class="fa fa-check text-success" v-if="selectedCategoryId === null || selectedCategoryId === ''"></i>
      </li>

      <!-- Danh sách Chuyên mục nạp động từ Database API -->
      <li
        v-for="cat in categories"
        :key="cat.id"
        class="list-group-item list-group-item-action d-flex justify-content-between align-items-center cursor-pointer"
        :class="{ active: Number(selectedCategoryId) === Number(cat.id) }"
        @click="emit('selectCategory', cat.id)"
      >
        <span>{{ cat.name }}</span>
        <i class="fa fa-check text-success" v-if="Number(selectedCategoryId) === Number(cat.id)"></i>
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
