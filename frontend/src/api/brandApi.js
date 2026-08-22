import axiosInstance from './axiosInstance'

/**
 * API calls cho Thương hiệu (Brands).
 */

// Lấy danh sách thương hiệu cho Admin (phân trang, tìm kiếm)
export const getAdminBrands = (params) => {
  return axiosInstance.get('/admin/brands', { params })
}

// Lấy toàn bộ danh sách cho dropdown
export const getBrandDropdown = () => {
  return axiosInstance.get('/admin/brands/dropdown')
}

// Lấy danh sách active cho Client
export const getClientBrands = () => {
  return axiosInstance.get('/brands')
}

// Tạo thương hiệu mới (FormData hỗ trợ file logo)
export const createBrand = (formData) => {
  return axiosInstance.post('/admin/brands', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

// Cập nhật thương hiệu
export const updateBrand = (id, formData) => {
  return axiosInstance.put(`/admin/brands/${id}`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

// Xóa mềm thương hiệu
export const deleteBrand = (id) => {
  return axiosInstance.delete(`/admin/brands/${id}`)
}
