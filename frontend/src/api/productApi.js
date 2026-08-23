import axiosInstance from './axiosInstance'

/**
 * API calls cho Sản phẩm (Products).
 */

// Admin: Lấy danh sách sản phẩm (phân trang, tìm kiếm, lọc chuyên mục, thương hiệu)
export const getAdminProducts = (params) => {
  return axiosInstance.get('/admin/products', { params })
}

// Client: Lấy danh sách sản phẩm active
export const getClientProducts = () => {
  return axiosInstance.get('/products')
}

// Client: Lấy chi tiết sản phẩm
export const getProductById = (id) => {
  return axiosInstance.get(`/products/${id}`)
}

// Admin: Tạo sản phẩm mới (FormData hỗ trợ chọn file ảnh)
export const createProduct = (formData) => {
  return axiosInstance.post('/admin/products', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

// Admin: Cập nhật sản phẩm
export const updateProduct = (id, formData) => {
  return axiosInstance.put(`/admin/products/${id}`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

// Admin: Xóa mềm sản phẩm
export const deleteProduct = (id) => {
  return axiosInstance.delete(`/admin/products/${id}`)
}
