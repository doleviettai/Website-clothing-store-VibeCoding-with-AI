<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

const router = useRouter()
const authStore = useAuthStore()

const form = reactive({
  email: '',
  password: '',
})

const isLoading = ref(false)
const errorMessage = ref('')
const showPassword = ref(false)

const errors = reactive({
  email: '',
  password: '',
})

function validateForm() {
  let isValid = true
  errors.email = ''
  errors.password = ''

  if (!form.email.trim()) {
    errors.email = 'Email không được để trống'
    isValid = false
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
    errors.email = 'Email không đúng định dạng'
    isValid = false
  }

  if (!form.password) {
    errors.password = 'Mật khẩu không được để trống'
    isValid = false
  }

  return isValid
}

async function handleLogin() {
  errorMessage.value = ''
  if (!validateForm()) return

  isLoading.value = true

  try {
    const user = await authStore.login({
      email: form.email,
      password: form.password,
    })

    if (user.roles?.includes('ADMIN')) {
      router.push('/admin/dashboard')
    } else {
      router.push('/')
    }
  } catch (err) {
    errorMessage.value = err.response?.data?.message || 'Đăng nhập thất bại. Vui lòng kiểm tra lại thông tin.'
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <div class="login-view">
    <div class="form-header">
      <h2 class="form-title">Đăng nhập tài khoản</h2>
      <p class="form-subtitle">Nhập thông tin tài khoản của bạn để tiếp tục</p>
    </div>

    <!-- Alert lỗi -->
    <div v-if="errorMessage" class="alert-error" role="alert">
      <i class="fa fa-exclamation-circle"></i>
      <span>{{ errorMessage }}</span>
    </div>

    <form @submit.prevent="handleLogin" class="auth-form" novalidate>
      <!-- Email -->
      <div class="form-group">
        <label for="login-email" class="form-label">Địa chỉ Email</label>
        <div class="input-wrapper" :class="{ 'input-error': errors.email }">
          <i class="fa fa-envelope input-icon"></i>
          <input
            id="login-email"
            v-model="form.email"
            type="email"
            class="form-input"
            placeholder="nhapemail@example.com"
            :disabled="isLoading"
          />
        </div>
        <p v-if="errors.email" class="field-error">{{ errors.email }}</p>
      </div>

      <!-- Password -->
      <div class="form-group">
        <div class="label-row">
          <label for="login-password" class="form-label">Mật khẩu</label>
        </div>
        <div class="input-wrapper" :class="{ 'input-error': errors.password }">
          <i class="fa fa-lock input-icon"></i>
          <input
            id="login-password"
            v-model="form.password"
            :type="showPassword ? 'text' : 'password'"
            class="form-input"
            placeholder="Mật khẩu của bạn"
            :disabled="isLoading"
          />
          <button
            type="button"
            class="toggle-password"
            @click="showPassword = !showPassword"
          >
            <i class="fa" :class="showPassword ? 'fa-eye-slash' : 'fa-eye'"></i>
          </button>
        </div>
        <p v-if="errors.password" class="field-error">{{ errors.password }}</p>
      </div>

      <!-- Button Submit -->
      <button
        type="submit"
        class="btn-submit"
        :disabled="isLoading"
      >
        <span v-if="isLoading" class="spinner"></span>
        <span>{{ isLoading ? 'ĐANG ĐĂNG NHẬP...' : 'ĐĂNG NHẬP' }}</span>
      </button>
    </form>

    <div class="switch-auth">
      <span>Chưa có tài khoản?</span>
      <RouterLink to="/register" class="switch-link">Đăng ký ngay</RouterLink>
    </div>
  </div>
</template>

<style scoped>
.form-header {
  text-align: center;
  margin-bottom: 24px;
}

.form-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: #111111;
  margin: 0 0 6px;
}

.form-subtitle {
  font-size: 0.875rem;
  color: #64748b;
  margin: 0;
}

.alert-error {
  display: flex;
  align-items: center;
  gap: 10px;
  background: #fef2f2;
  border: 1px solid #fecaca;
  color: #dc2626;
  padding: 12px 16px;
  border-radius: 8px;
  font-size: 0.875rem;
  margin-bottom: 20px;
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.label-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.form-label {
  font-size: 0.875rem;
  font-weight: 600;
  color: #334155;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 14px;
  color: #94a3b8;
  font-size: 16px;
  pointer-events: none;
}

.form-input {
  width: 100%;
  padding: 12px 42px;
  background: #ffffff;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  color: #0f172a;
  font-size: 0.9375rem;
  outline: none;
  transition: all 0.2s ease;
}

.form-input::placeholder {
  color: #94a3b8;
}

.form-input:focus {
  border-color: #e53637;
  box-shadow: 0 0 0 3px rgba(229, 54, 55, 0.12);
}

.input-wrapper.input-error .form-input {
  border-color: #ef4444;
}

.toggle-password {
  position: absolute;
  right: 14px;
  background: none;
  border: none;
  cursor: pointer;
  color: #94a3b8;
  padding: 0;
}

.toggle-password:hover {
  color: #475569;
}

.field-error {
  font-size: 0.8rem;
  color: #dc2626;
  margin: 0;
}

.btn-submit {
  width: 100%;
  padding: 14px;
  background: #111111;
  border: none;
  border-radius: 8px;
  color: #ffffff;
  font-size: 0.9375rem;
  font-weight: 700;
  letter-spacing: 1px;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-top: 6px;
}

.btn-submit:hover:not(:disabled) {
  background: #e53637;
}

.btn-submit:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #ffffff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.switch-auth {
  text-align: center;
  font-size: 0.875rem;
  color: #64748b;
  margin-top: 24px;
}

.switch-link {
  color: #e53637;
  font-weight: 700;
  text-decoration: none;
  margin-left: 6px;
}

.switch-link:hover {
  text-decoration: underline;
}
</style>
