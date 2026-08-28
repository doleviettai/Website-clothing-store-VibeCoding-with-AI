import axiosInstance from './axiosInstance'

export const getProductReviews = (productId) => {
  return axiosInstance.get(`/products/${productId}/reviews`)
}

export const createReview = (productId, data) => {
  return axiosInstance.post(`/products/${productId}/reviews`, data)
}

export const deleteUserReview = (reviewId) => {
  return axiosInstance.delete(`/reviews/${reviewId}`)
}

// Admin Review APIs
export const getAdminReviews = (params) => {
  return axiosInstance.get('/admin/reviews', { params })
}

export const toggleReviewStatus = (id, status) => {
  return axiosInstance.patch(`/admin/reviews/${id}/status`, null, { params: { status } })
}

export const deleteAdminReview = (id) => {
  return axiosInstance.delete(`/admin/reviews/${id}`)
}

export const deleteReview = (id) => {
  return axiosInstance.delete(`/admin/reviews/${id}`)
}
