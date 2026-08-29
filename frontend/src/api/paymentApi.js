import axiosInstance from './axiosInstance'

// Admin Payment APIs
export const getAdminPayments = (params) => {
  return axiosInstance.get('/admin/payments', { params })
}

export const getAdminPaymentDetail = (id) => {
  return axiosInstance.get(`/admin/payments/${id}`)
}
