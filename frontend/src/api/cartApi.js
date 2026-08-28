import axiosInstance from './axiosInstance'

export const getCart = () => {
  return axiosInstance.get('/cart')
}

export const addToCart = (data) => {
  return axiosInstance.post('/cart/items', data)
}

export const updateCartItemQuantity = (itemId, quantity) => {
  return axiosInstance.put(`/cart/items/${itemId}`, { quantity })
}

export const removeCartItem = (itemId) => {
  return axiosInstance.delete(`/cart/items/${itemId}`)
}
