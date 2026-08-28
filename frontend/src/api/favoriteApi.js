import axiosInstance from './axiosInstance'

export const addFavorite = (productId) => {
  return axiosInstance.post('/favorites', { productId })
}

export const removeFavorite = (productId) => {
  return axiosInstance.delete(`/favorites/${productId}`)
}

export const checkFavorite = (productId) => {
  return axiosInstance.get(`/favorites/check/${productId}`)
}

export const getUserFavorites = () => {
  return axiosInstance.get('/favorites')
}
