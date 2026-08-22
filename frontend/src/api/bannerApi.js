import axiosInstance from './axiosInstance'

/**
 * API calls cho Banner quảng cáo & Slide.
 */

// Admin: Lấy danh sách banner (phân trang, tìm kiếm, lọc position)
export const getAdminBanners = (params) => {
  return axiosInstance.get('/admin/banners', { params })
}

// Client: Lấy danh sách banner ACTIVE theo position (sắp xếp theo sortOrder)
export const getClientBanners = (position) => {
  return axiosInstance.get('/banners', { params: { position } })
}

// Admin: Tạo banner mới (FormData hỗ trợ file)
export const createBanner = (formData) => {
  return axiosInstance.post('/admin/banners', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

// Admin: Cập nhật banner
export const updateBanner = (id, formData) => {
  return axiosInstance.put(`/admin/banners/${id}`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

// Admin: Xóa mềm banner
export const deleteBanner = (id) => {
  return axiosInstance.delete(`/admin/banners/${id}`)
}
