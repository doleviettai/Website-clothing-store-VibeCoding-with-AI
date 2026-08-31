import axiosInstance from './axiosInstance'

export const createZaloPayPayment = (orderId) => {
  return axiosInstance.post('/payments/zalopay/create', { orderId })
}

export const getZaloPayStatus = (orderId) => {
  return axiosInstance.get(`/payments/zalopay/status/${orderId}`)
}

export const confirmMockZaloPay = (orderId, appTransId, status) => {
  return axiosInstance.post('/payments/zalopay/confirm-mock', null, {
    params: { orderId, appTransId, status }
  })
}
