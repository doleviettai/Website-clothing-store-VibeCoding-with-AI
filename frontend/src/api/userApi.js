import axiosInstance from './axiosInstance'

/**
 * API calls cho Tài khoản Người dùng (Users).
 */

// Admin: Lấy danh sách người dùng (phân trang, tìm kiếm, lọc status, roleName)
export const getAdminUsers = (params) => {
  return axiosInstance.get('/admin/users', { params })
}

// Admin: Lấy chi tiết người dùng
export const getUserById = (id) => {
  return axiosInstance.get(`/admin/users/${id}`)
}

// Admin: Tạo tài khoản người dùng mới (FormData chọn avatar)
export const createUser = (formData) => {
  return axiosInstance.post('/admin/users', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

// Admin: Cập nhật thông tin người dùng
export const updateUser = (id, formData) => {
  return axiosInstance.put(`/admin/users/${id}`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

// Admin: Xóa mềm người dùng
export const deleteUser = (id) => {
  return axiosInstance.delete(`/admin/users/${id}`)
}
