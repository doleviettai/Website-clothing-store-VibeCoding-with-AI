import axios from 'axios'

/**
 * Axios instance dùng chung cho toàn bộ frontend.
 *
 * Cấu hình:
 * - baseURL: API backend Spring Boot
 * - Content-Type: JSON
 *
 * Request interceptor:
 * - Tự động gắn Authorization: Bearer <accessToken> vào mọi request
 *
 * Response interceptor:
 * - Nếu nhận 401 → thử refresh token → gửi lại request gốc
 * - Nếu refresh thất bại → logout và redirect về /login
 */

const axiosInstance = axios.create({
  baseURL: 'http://localhost:8080/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
})

// ─── Request Interceptor ───────────────────────────────────────────
// Gắn access token vào mọi request trước khi gửi
axiosInstance.interceptors.request.use(
  (config) => {
    const accessToken = localStorage.getItem('accessToken')
    if (accessToken) {
      config.headers.Authorization = `Bearer ${accessToken}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

// ─── Response Interceptor ──────────────────────────────────────────
// Xử lý khi nhận được response lỗi

// Flag để tránh vòng lặp refresh (nhiều request 401 cùng lúc)
let isRefreshing = false
let failedQueue = []

const processQueue = (error, token = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error)
    } else {
      prom.resolve(token)
    }
  })
  failedQueue = []
}

axiosInstance.interceptors.response.use(
  // Response thành công → trả về bình thường
  (response) => response,

  // Response lỗi
  async (error) => {
    const originalRequest = error.config

    // Nếu lỗi 401 và chưa retry (tránh vòng lặp vô tận)
    if (error.response?.status === 401 && !originalRequest._retry) {
      // Nếu đang trong quá trình refresh → xếp vào hàng chờ
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject })
        })
          .then((token) => {
            originalRequest.headers.Authorization = `Bearer ${token}`
            return axiosInstance(originalRequest)
          })
          .catch((err) => Promise.reject(err))
      }

      // Đánh dấu đang retry và đang refresh
      originalRequest._retry = true
      isRefreshing = true

      const refreshToken = localStorage.getItem('refreshToken')

      if (!refreshToken) {
        // Không có refresh token → logout
        handleLogout()
        return Promise.reject(error)
      }

      try {
        // Gọi API refresh token (dùng axios gốc, không dùng instance để tránh vòng lặp)
        const response = await axios.post('http://localhost:8080/api/v1/auth/refresh', {
          refreshToken,
        })

        const { accessToken: newAccessToken, refreshToken: newRefreshToken } = response.data.data

        // Lưu token mới
        localStorage.setItem('accessToken', newAccessToken)
        localStorage.setItem('refreshToken', newRefreshToken)

        // Cập nhật header mặc định
        axiosInstance.defaults.headers.common.Authorization = `Bearer ${newAccessToken}`

        // Gửi lại các request đang chờ
        processQueue(null, newAccessToken)

        // Gửi lại request gốc với token mới
        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`
        return axiosInstance(originalRequest)
      } catch (refreshError) {
        // Refresh thất bại → logout
        processQueue(refreshError, null)
        handleLogout()
        return Promise.reject(refreshError)
      } finally {
        isRefreshing = false
      }
    }

    return Promise.reject(error)
  },
)

/**
 * Xóa token và redirect về trang login.
 */
function handleLogout() {
  localStorage.removeItem('accessToken')
  localStorage.removeItem('refreshToken')
  window.location.href = '/login'
}

export default axiosInstance
