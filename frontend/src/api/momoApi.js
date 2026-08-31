import axiosInstance from './axiosInstance'

export const createMoMoPayment = (orderId) => {
  return axiosInstance.post('/payments/momo/create', { orderId })
}

export const getMoMoStatus = (orderId) => {
  return axiosInstance.get(`/payments/momo/status/${orderId}`)
}

export const confirmMockMoMo = (orderId, requestId, status) => {
  return axiosInstance.post('/payments/momo/confirm-mock', null, {
    params: { orderId, requestId, status }
  })
}
