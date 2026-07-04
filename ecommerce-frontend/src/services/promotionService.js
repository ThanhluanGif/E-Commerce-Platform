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
  }
};

export default promotionService;
