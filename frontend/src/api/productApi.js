import axiosInstance from './axiosInstance'

/**
 * API calls cho Sản phẩm (Products).
 */

// Admin: Lấy danh sách sản phẩm (phân trang, tìm kiếm, lọc chuyên mục, thương hiệu)
export const getAdminProducts = (params) => {
  return axiosInstance.get('/admin/products', { params })
}

// Client: Lấy danh sách sản phẩm cửa hàng (phân trang, tìm kiếm realtime, lọc category, brand, sort)
export const getClientProducts = (params) => {
  return axiosInstance.get('/products', { params })
}

// Client: Lấy chi tiết sản phẩm theo ID
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
