import axiosInstance from './axiosInstance'

/**
 * API calls cho Đánh Giá & Bình Luận Sản Phẩm (Reviews).
 */

// Admin: Lấy danh sách đánh giá (phân trang, tìm kiếm AJAX, lọc status, rating)
export const getAdminReviews = (params) => {
  return axiosInstance.get('/admin/reviews', { params })
}

// Admin: Ẩn / Hiện (Toggle) bình luận đánh giá
export const toggleReviewStatus = (id, status) => {
  return axiosInstance.patch(`/admin/reviews/${id}/status`, null, {
    params: { status },
  })
}

// Admin: Xóa bình luận đánh giá
export const deleteReview = (id) => {
  return axiosInstance.delete(`/admin/reviews/${id}`)
}
