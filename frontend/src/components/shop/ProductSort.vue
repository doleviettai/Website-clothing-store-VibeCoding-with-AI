<script setup>
defineProps({
  modelValue: {
    type: String,
    default: 'newest',
  },
})

const emit = defineEmits(['update:modelValue', 'changeSort'])

const onChange = (event) => {
  const value = event.target.value
  emit('update:modelValue', value)
  emit('changeSort', value)
}
</script>

<template>
  <div class="shop-sort-box d-flex align-items-center gap-2">
    <label class="form-label font-weight-bold text-dark mb-0 whitespace-nowrap">Sắp xếp theo:</label>
    <select :value="modelValue" class="form-control form-control-sm border shadow-sm" @change="onChange">
      <option value="newest">Mới Nhất (Newest)</option>
      <option value="price_asc">Giá: Thấp → Cao (Price: Low to High)</option>
      <option value="price_desc">Giá: Cao → Thấp (Price: High to Low)</option>
    </select>
  </div>
</template>

<style scoped>
.whitespace-nowrap {
  white-space: nowrap;
}
.shop-sort-box select {
  min-width: 200px;
  cursor: pointer;
}
</style>
