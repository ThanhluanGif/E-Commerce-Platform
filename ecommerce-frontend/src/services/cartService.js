import api from './api';

const cartService = {
  getCart: async () => {
    const response = await api.get('/api/cart');
    return response.data; // ApiResponse<List<CartItemDTO>>
  },

  addItem: async (productId, quantity) => {
    const response = await api.post('/api/cart/items', { productId, quantity });
    return response.data; // ApiResponse<CartItemDTO>
  },

  updateQuantity: async (cartItemId, quantity) => {
    const response = await api.put(`/api/cart/items/${cartItemId}?quantity=${quantity}`);
    return response.data; // ApiResponse<CartItemDTO>
  },

  removeItem: async (cartItemId) => {
    const response = await api.delete(`/api/cart/items/${cartItemId}`);
    return response.data; // ApiResponse<Void>
  },

  clearCart: async () => {
    const response = await api.delete('/api/cart');
    return response.data; // ApiResponse<Void>
  },

  mergeCart: async (guestItems) => {
    const response = await api.post('/api/cart/merge', guestItems);
    return response.data; // ApiResponse<Void>
  }
};

export default cartService;
