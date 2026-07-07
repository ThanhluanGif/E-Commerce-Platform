import api from './api';

const returnService = {
  createReturnRequest: async (orderId, returnData) => {
    const response = await api.post(`/api/returns/order/${orderId}`, returnData);
    return response.data; // ApiResponse<ReturnRequestDTO>
  },

  getMyReturns: async () => {
    const response = await api.get('/api/returns');
    return response.data; // ApiResponse<List<ReturnRequestDTO>>
  },

  getReturnDetail: async (id) => {
    const response = await api.get(`/api/returns/${id}`);
    return response.data; // ApiResponse<ReturnRequestDTO>
  },

  getShopReturns: async () => {
    const response = await api.get('/api/seller/returns');
    return response.data; // ApiResponse<List<ReturnRequestDTO>>
  },

  respondToReturn: async (id, approved, note) => {
    const response = await api.put(`/api/seller/returns/${id}/respond?approved=${approved}&note=${encodeURIComponent(note || '')}`);
    return response.data; // ApiResponse<ReturnRequestDTO>
  },

  getAllReturns: async () => {
    const response = await api.get('/api/admin/returns');
    return response.data; // ApiResponse<List<ReturnRequestDTO>>
  },

  resolveReturn: async (id, refund, note) => {
    const response = await api.put(`/api/admin/returns/${id}/resolve?refund=${refund}&note=${encodeURIComponent(note || '')}`);
    return response.data; // ApiResponse<ReturnRequestDTO>
  }
};

export default returnService;
