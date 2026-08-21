import axiosInstance from './axiosInstance'

/**
 * API calls cho Chuyên mục (Categories).
 */

// Lấy danh sách chuyên mục cho Admin (phân trang, tìm kiếm)
export const getAdminCategories = (params) => {
  return axiosInstance.get('/admin/categories', { params })
}

// Lấy toàn bộ danh sách cho dropdown chọn chuyên mục cha
export const getCategoryDropdown = () => {
  return axiosInstance.get('/admin/categories/dropdown')
}

// Lấy danh sách active cho Client
export const getClientCategories = () => {
  return axiosInstance.get('/categories')
}

// Tạo chuyên mục mới (FormData hỗ trợ file)
export const createCategory = (formData) => {
  return axiosInstance.post('/admin/categories', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

// Cập nhật chuyên mục
export const updateCategory = (id, formData) => {
  return axiosInstance.put(`/admin/categories/${id}`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

// Xóa mềm chuyên mục
export const deleteCategory = (id) => {
  return axiosInstance.delete(`/admin/categories/${id}`)
}
