import api from './api';

const addressService = {
  getAllAddresses: async () => {
    const response = await api.get('/api/users/addresses');
    return response.data;
  },

  createAddress: async (addressData) => {
    const response = await api.post('/api/users/addresses', addressData);
    return response.data;
  },

  updateAddress: async (id, addressData) => {
    const response = await api.put(`/api/users/addresses/${id}`, addressData);
    return response.data;
  },

  deleteAddress: async (id) => {
    const response = await api.delete(`/api/users/addresses/${id}`);
    return response.data;
  },

  setDefaultAddress: async (id) => {
    const response = await api.put(`/api/users/addresses/${id}/default`);
    return response.data;
  }
};

export default addressService;
