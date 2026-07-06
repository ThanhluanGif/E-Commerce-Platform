import api from './api';

const promotionService = {
  getActiveFlashSale: async () => {
    const response = await api.get('/api/flash-sales/active');
    return response.data;
  },
  getMyVouchers: async () => {
    const response = await api.get('/api/vouchers/mine');
    return response.data;
  },
  applyVoucher: async (voucherCode) => {
    const response = await api.post('/api/checkout/apply-voucher', null, {
      params: { voucherCode }
    });
    return response.data;
  },
  subscribeFlashSale: async (id) => {
    const response = await api.post(`/api/flash-sales/${id}/subscribe`);
    return response.data;
  },
  unsubscribeFlashSale: async (id) => {
    const response = await api.delete(`/api/flash-sales/${id}/unsubscribe`);
    return response.data;
  },
  isSubscribedFlashSale: async (id) => {
    const response = await api.get(`/api/flash-sales/${id}/is-subscribed`);
    return response.data;
  }
};

export default promotionService;
