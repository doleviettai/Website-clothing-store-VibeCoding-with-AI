import axiosInstance from './axiosInstance'

/**
 * API calls liên quan đến xác thực.
 * Mỗi function gọi một endpoint của AuthController.
 */

/**
 * Đăng ký tài khoản mới.
 * POST /api/v1/auth/register
 */
export const register = (data) => {
  return axiosInstance.post('/auth/register', data)
}

/**
 * Đăng nhập.
 * POST /api/v1/auth/login
 * Response: { accessToken, refreshToken, user }
 */
export const login = (data) => {
  return axiosInstance.post('/auth/login', data)
}

/**
 * Làm mới access token.
 * POST /api/v1/auth/refresh
 */
export const refreshToken = (refreshToken) => {
  return axiosInstance.post('/auth/refresh', { refreshToken })
}

/**
 * Đăng xuất.
 * POST /api/v1/auth/logout
 */
export const logout = (refreshToken) => {
  return axiosInstance.post('/auth/logout', { refreshToken })
}

/**
 * Lấy thông tin user hiện tại.
 * GET /api/v1/auth/me
 * Yêu cầu: đã đăng nhập (axiosInstance tự gắn token)
 */
export const getMe = () => {
  return axiosInstance.get('/auth/me')
}
