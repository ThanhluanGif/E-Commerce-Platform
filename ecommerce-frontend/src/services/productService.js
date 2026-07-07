import api from './api';

const productService = {
  getAllProducts: async (params) => {
    const response = await api.get('/api/products', { params });
    return response.data; // This returns ApiResponse<Page<ProductDTO>>
  },

  getProductById: async (id) => {
    const response = await api.get(`/api/products/${id}`);
    return response.data; // This returns ApiResponse<ProductDTO>
  },

  createProduct: async (productData) => {
    const response = await api.post('/api/products', productData);
    return response.data;
  },

  updateProduct: async (id, productData) => {
    const response = await api.put(`/api/products/${id}`, productData);
    return response.data;
  },

  deleteProduct: async (id) => {
    const response = await api.delete(`/api/products/${id}`);
    return response.data;
  }
};

export default productService;
