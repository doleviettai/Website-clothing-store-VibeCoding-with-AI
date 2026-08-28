<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  modelValue: {
    type: String,
    default: '',
  },
})

const emit = defineEmits(['update:modelValue', 'search'])

const internalKeyword = ref(props.modelValue)
let timer = null

watch(
  () => props.modelValue,
  (newVal) => {
    internalKeyword.value = newVal
  }
)

const onInput = () => {
  emit('update:modelValue', internalKeyword.value)
  clearTimeout(timer)
  // Debounce 400ms gửi AJAX tìm kiếm realtime
  timer = setTimeout(() => {
    emit('search', internalKeyword.value)
  }, 400)
}
</script>

<template>
  <div class="shop-sidebar-search mb-4">
    <h6 class="text-uppercase font-weight-bold text-dark mb-3">
      <i class="fa fa-search mr-2 text-danger"></i> Tìm Kiếm Sản Phẩm
    </h6>
    <div class="input-group shadow-sm rounded overflow-hidden">
      <span class="input-group-text bg-white border-end-0 text-muted">
        <i class="fa fa-search"></i>
      </span>
      <input
        v-model="internalKeyword"
        type="text"
        class="form-control border-start-0 py-2"
        placeholder="Nhập tên quần áo cần tìm..."
        @input="onInput"
      />
    </div>
  </div>
</template>

<style scoped>
.shop-sidebar-search input:focus {
  box-shadow: none;
}
</style>
