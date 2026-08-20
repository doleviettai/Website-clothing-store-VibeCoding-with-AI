import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import * as authApi from '@/api/authApi'

/**
 * Pinia store quản lý trạng thái xác thực.
 *
 * State lưu trong memory:
 * - user: thông tin người dùng (id, email, fullName, roles...)
 * - accessToken: JWT token (cũng lưu localStorage)
 * - refreshToken: lưu localStorage
 *
 * Computed:
 * - isAuthenticated: có đăng nhập không
 * - isAdmin: có phải ADMIN không
 * - isClient: có phải CLIENT không
 */
export const useAuthStore = defineStore('auth', () => {
  // ─── State ────────────────────────────────────────────────────────
  const user = ref(null)
  const accessToken = ref(localStorage.getItem('accessToken') || null)
  const refreshToken = ref(localStorage.getItem('refreshToken') || null)

  // ─── Computed ─────────────────────────────────────────────────────
  const isAuthenticated = computed(() => !!accessToken.value && !!user.value)

  const isAdmin = computed(() => user.value?.roles?.includes('ADMIN') ?? false)

  const isClient = computed(() => user.value?.roles?.includes('CLIENT') ?? false)

  // ─── Actions ──────────────────────────────────────────────────────

  /**
   * Đăng ký tài khoản mới.
   * Sau khi đăng ký thành công — không tự động đăng nhập, redirect sang login.
   */
  async function register(formData) {
    const response = await authApi.register(formData)
    return response.data
  }

  /**
   * Đăng nhập.
   * Lưu token vào localStorage và state.
   * Trả về user để component có thể redirect theo role.
   */
  async function login(credentials) {
    const response = await authApi.login(credentials)
    const { accessToken: newAccessToken, refreshToken: newRefreshToken, user: userData } = response.data.data

    // Lưu vào state
    accessToken.value = newAccessToken
    refreshToken.value = newRefreshToken
    user.value = userData

    // Lưu vào localStorage để tồn tại khi F5
    localStorage.setItem('accessToken', newAccessToken)
    localStorage.setItem('refreshToken', newRefreshToken)

    return userData
  }

  /**
   * Đăng xuất.
   * Gọi API thu hồi refresh token, sau đó xóa state.
   */
  async function logoutAction() {
    try {
      if (refreshToken.value) {
        await authApi.logout(refreshToken.value)
      }
    } catch {
      // Kể cả API lỗi vẫn phải xóa state local
    } finally {
      clearAuth()
    }
  }

  /**
   * Lấy thông tin user hiện tại từ server.
   * Gọi khi app khởi động để restore trạng thái đăng nhập.
   */
  async function fetchCurrentUser() {
    try {
      const response = await authApi.getMe()
      user.value = response.data.data
    } catch {
      // Token hết hạn hoặc không hợp lệ → xóa state
      clearAuth()
    }
  }

  /**
   * Xóa toàn bộ trạng thái auth.
   */
  function clearAuth() {
    user.value = null
    accessToken.value = null
    refreshToken.value = null
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
  }

  return {
    // State
    user,
    accessToken,
    refreshToken,
    // Computed
    isAuthenticated,
    isAdmin,
    isClient,
    // Actions
    register,
    login,
    logoutAction,
    fetchCurrentUser,
    clearAuth,
  }
})
