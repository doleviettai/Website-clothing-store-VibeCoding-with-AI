import axiosInstance from './axiosInstance'

// Admin Order APIs
export const getAdminOrders = (params) => {
  return axiosInstance.get('/admin/orders', { params })
}

export const getAdminOrderDetail = (id) => {
  return axiosInstance.get(`/admin/orders/${id}`)
}

export const updateOrderStatus = (id, status) => {
  return axiosInstance.patch(`/admin/orders/${id}/status`, { status })
}

export const updateOrderPaymentStatus = (id, paymentStatus) => {
  return axiosInstance.patch(`/admin/orders/${id}/payment-status`, null, { params: { paymentStatus } })
}
