import api from './api';

const adminService = {
  getDashboardStats: async () => {
    const response = await api.get('/api/admin/dashboard');
    return response.data; // ApiResponse<DashboardStatsDTO>
  },

  getRevenueChart: async () => {
    const response = await api.get('/api/admin/dashboard/revenue');
    return response.data; // ApiResponse<List<RevenueChartDataDTO>>
  },

  getUsers: async (page = 0, size = 10) => {
    const response = await api.get(`/api/admin/users?page=${page}&size=${size}`);
    return response.data; // ApiResponse<Page<UserDTO>>
  },

  changeUserRole: async (userId, role) => {
    const response = await api.put(`/api/admin/users/${userId}/role?role=${role}`);
    return response.data; // ApiResponse<UserDTO>
  },

  getOrders: async (page = 0, size = 10) => {
    const response = await api.get(`/api/orders/all?page=${page}&size=${size}`);
    return response.data; // ApiResponse<Page<OrderDTO>>
  },

  updateOrderStatus: async (orderId, status) => {
    const response = await api.put(`/api/orders/${orderId}/status?status=${status}`);
    return response.data; // ApiResponse<OrderDTO>
  }
};

export default adminService;
