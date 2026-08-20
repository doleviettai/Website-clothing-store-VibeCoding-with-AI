<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

const router = useRouter()
const authStore = useAuthStore()

const form = reactive({
  fullName: '',
  email: '',
  phone: '',
  password: '',
  confirmPassword: '',
})

const isLoading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')
const showPassword = ref(false)

const errors = reactive({
  fullName: '',
  email: '',
  phone: '',
  password: '',
  confirmPassword: '',
})

function validateForm() {
  let isValid = true
  Object.keys(errors).forEach((k) => (errors[k] = ''))

  if (!form.fullName.trim()) {
    errors.fullName = 'Họ tên không được để trống'
    isValid = false
  }

  if (!form.email.trim()) {
    errors.email = 'Email không được để trống'
    isValid = false
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
    errors.email = 'Email không đúng định dạng'
    isValid = false
  }

  if (form.phone && !/^(\+84|0)[0-9]{9,10}$/.test(form.phone)) {
    errors.phone = 'Số điện thoại không đúng định dạng'
    isValid = false
  }

  if (!form.password) {
    errors.password = 'Mật khẩu không được để trống'
    isValid = false
  } else if (form.password.length < 8) {
    errors.password = 'Mật khẩu phải có ít nhất 8 ký tự'
    isValid = false
  }

  if (!form.confirmPassword) {
    errors.confirmPassword = 'Vui lòng xác nhận mật khẩu'
    isValid = false
  } else if (form.password !== form.confirmPassword) {
    errors.confirmPassword = 'Mật khẩu xác nhận không khớp'
    isValid = false
  }

  return isValid
}

async function handleRegister() {
  errorMessage.value = ''
  successMessage.value = ''

  if (!validateForm()) return

  isLoading.value = true

  try {
    await authStore.register({
      fullName: form.fullName.trim(),
      email: form.email.trim(),
      phone: form.phone.trim() || undefined,
      password: form.password,
      confirmPassword: form.confirmPassword,
    })

    successMessage.value = 'Đăng ký thành công! Đang chuyển đến trang đăng nhập...'
    setTimeout(() => {
      router.push('/login')
    }, 1500)
  } catch (err) {
    const serverErrors = err.response?.data?.errors
    if (serverErrors) {
      Object.keys(serverErrors).forEach((field) => {
        if (field in errors) errors[field] = serverErrors[field]
      })
    }
    errorMessage.value = err.response?.data?.message || 'Đăng ký thất bại. Vui lòng thử lại.'
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <div class="register-view">
    <div class="form-header">
      <h2 class="form-title">Tạo tài khoản mới</h2>
      <p class="form-subtitle">Đăng ký để trải nghiệm các ưu đãi từ Male Fashion</p>
    </div>

    <!-- Alert -->
    <div v-if="successMessage" class="alert-success">
      <i class="fa fa-check-circle"></i>
      <span>{{ successMessage }}</span>
    </div>

    <div v-if="errorMessage && !successMessage" class="alert-error">
      <i class="fa fa-exclamation-circle"></i>
      <span>{{ errorMessage }}</span>
    </div>

    <form @submit.prevent="handleRegister" class="auth-form" novalidate>
      <!-- Họ tên -->
      <div class="form-group">
        <label for="reg-fullname" class="form-label">Họ và tên <span class="req">*</span></label>
        <div class="input-wrapper" :class="{ 'input-error': errors.fullName }">
          <i class="fa fa-user input-icon"></i>
          <input
            id="reg-fullname"
            v-model="form.fullName"
            type="text"
            class="form-input"
            placeholder="Nguyễn Văn A"
            :disabled="isLoading"
          />
        </div>
        <p v-if="errors.fullName" class="field-error">{{ errors.fullName }}</p>
      </div>

      <!-- Email -->
      <div class="form-group">
        <label for="reg-email" class="form-label">Email <span class="req">*</span></label>
        <div class="input-wrapper" :class="{ 'input-error': errors.email }">
          <i class="fa fa-envelope input-icon"></i>
          <input
            id="reg-email"
            v-model="form.email"
            type="email"
            class="form-input"
            placeholder="email@example.com"
            :disabled="isLoading"
          />
        </div>
        <p v-if="errors.email" class="field-error">{{ errors.email }}</p>
      </div>

      <!-- SĐT -->
      <div class="form-group">
        <label for="reg-phone" class="form-label">Số điện thoại <span class="opt">(tùy chọn)</span></label>
        <div class="input-wrapper" :class="{ 'input-error': errors.phone }">
          <i class="fa fa-phone input-icon"></i>
          <input
            id="reg-phone"
            v-model="form.phone"
            type="tel"
            class="form-input"
            placeholder="0901234567"
            :disabled="isLoading"
          />
        </div>
        <p v-if="errors.phone" class="field-error">{{ errors.phone }}</p>
      </div>

      <!-- Mật khẩu -->
      <div class="form-group">
        <label for="reg-password" class="form-label">Mật khẩu <span class="req">*</span></label>
        <div class="input-wrapper" :class="{ 'input-error': errors.password }">
          <i class="fa fa-lock input-icon"></i>
          <input
            id="reg-password"
            v-model="form.password"
            :type="showPassword ? 'text' : 'password'"
            class="form-input"
            placeholder="Ít nhất 8 ký tự"
            :disabled="isLoading"
          />
          <button type="button" class="toggle-password" @click="showPassword = !showPassword">
            <i class="fa" :class="showPassword ? 'fa-eye-slash' : 'fa-eye'"></i>
          </button>
        </div>
        <p v-if="errors.password" class="field-error">{{ errors.password }}</p>
      </div>

      <!-- Xác nhận mật khẩu -->
      <div class="form-group">
        <label for="reg-confirm" class="form-label">Xác nhận mật khẩu <span class="req">*</span></label>
        <div class="input-wrapper" :class="{ 'input-error': errors.confirmPassword }">
          <i class="fa fa-shield input-icon"></i>
          <input
            id="reg-confirm"
            v-model="form.confirmPassword"
            :type="showPassword ? 'text' : 'password'"
            class="form-input"
            placeholder="Nhập lại mật khẩu"
            :disabled="isLoading"
          />
        </div>
        <p v-if="errors.confirmPassword" class="field-error">{{ errors.confirmPassword }}</p>
      </div>

      <!-- Submit Button -->
      <button type="submit" class="btn-submit" :disabled="isLoading">
        <span v-if="isLoading" class="spinner"></span>
        <span>{{ isLoading ? 'ĐANG TẠO TÀI KHOẢN...' : 'TẠO TÀI KHOẢN' }}</span>
      </button>
    </form>

    <div class="switch-auth">
      <span>Đã có tài khoản?</span>
      <RouterLink to="/login" class="switch-link">Đăng nhập</RouterLink>
    </div>
  </div>
</template>

<style scoped>
.form-header {
  text-align: center;
  margin-bottom: 20px;
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
  margin-bottom: 16px;
}

.alert-success {
  display: flex;
  align-items: center;
  gap: 10px;
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  color: #16a34a;
  padding: 12px 16px;
  border-radius: 8px;
  font-size: 0.875rem;
  margin-bottom: 16px;
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.form-label {
  font-size: 0.875rem;
  font-weight: 600;
  color: #334155;
}

.req { color: #dc2626; }
.opt { color: #94a3b8; font-weight: 400; font-size: 0.8rem; }

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
  padding: 10px 42px;
  background: #ffffff;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  color: #0f172a;
  font-size: 0.9375rem;
  outline: none;
  transition: all 0.2s ease;
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
}

.field-error {
  font-size: 0.8rem;
  color: #dc2626;
  margin: 0;
}

.btn-submit {
  width: 100%;
  padding: 13px;
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

@keyframes spin { to { transform: rotate(360deg); } }

.switch-auth {
  text-align: center;
  font-size: 0.875rem;
  color: #64748b;
  margin-top: 18px;
}

.switch-link {
  color: #e53637;
  font-weight: 700;
  text-decoration: none;
  margin-left: 6px;
}

.switch-link:hover { text-decoration: underline; }
</style>
