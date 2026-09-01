import axiosInstance from './axiosInstance'

export const createVNPayPayment = (orderId, bankCode = '') => {
  return axiosInstance.post('/payments/vnpay/create', { orderId, bankCode })
}

export const getVNPayStatus = (orderId) => {
  return axiosInstance.get(`/payments/vnpay/status/${orderId}`)
}

export const confirmMockVNPay = (orderId, txnRef, status) => {
  return axiosInstance.post('/payments/vnpay/confirm-mock', null, {
    params: { orderId, txnRef, status }
  })
}
