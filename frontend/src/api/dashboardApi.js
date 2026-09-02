import axiosInstance from './axiosInstance'

export const dashboardApi = {
  getSummary: (days = 7) => {
    return axiosInstance.get(`/admin/dashboard/summary?days=${days}`)
  }
}

export default dashboardApi
