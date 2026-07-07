import api from './api';

const sellerService = {
  registerShop: async (shopData) => {
    const response = await api.post('/api/seller/register', shopData);
    return response.data;
  },
  getShopInfo: async () => {
    const response = await api.get('/api/seller/shop');
    return response.data;
  },
  getAnalytics: async () => {
    const response = await api.get('/api/seller/analytics');
    return response.data;
  },
  getProducts: async () => {
    const response = await api.get('/api/seller/products');
    return response.data;
  },
  createProduct: async (productData) => {
    const response = await api.post('/api/seller/products', productData);
    return response.data;
  },
  updateProduct: async (id, productData) => {
    const response = await api.put(`/api/seller/products/${id}`, productData);
    return response.data;
  },
  deleteProduct: async (id) => {
    const response = await api.delete(`/api/seller/products/${id}`);
    return response.data;
  },
  getOrders: async () => {
    const response = await api.get('/api/seller/orders');
    return response.data;
  },
  confirmOrder: async (orderId) => {
    const response = await api.put(`/api/seller/orders/${orderId}/confirm`);
    return response.data;
  }
};

export default sellerService;
