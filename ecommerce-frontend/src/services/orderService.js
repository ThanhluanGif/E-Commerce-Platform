import api from './api';

const orderService = {
  createOrder: async (orderData) => {
    const response = await api.post('/api/orders', orderData);
    return response.data; // ApiResponse<OrderDTO>
  },

  getMyOrders: async () => {
    const response = await api.get('/api/orders');
    return response.data; // ApiResponse<List<OrderDTO>>
  },

  getOrderDetails: async (orderId) => {
    const response = await api.get(`/api/orders/${orderId}`);
    return response.data; // ApiResponse<OrderDTO>
  },

  cancelOrder: async (orderId) => {
    const response = await api.put(`/api/orders/${orderId}/cancel`);
    return response.data; // ApiResponse<OrderDTO>
  },

  payOrder: async (orderId) => {
    const response = await api.post(`/api/orders/${orderId}/pay`);
    return response.data; // ApiResponse<OrderDTO>
  }
};

export default orderService;
