import api from './api';

const categoryService = {
  getAllCategories: async () => {
    const response = await api.get('/api/categories');
    return response.data; // ApiResponse<List<CategoryDTO>>
  },

  getCategoryTree: async () => {
    const response = await api.get('/api/categories/tree');
    return response.data; // ApiResponse<List<CategoryDTO>>
  },

  getCategoryById: async (id) => {
    const response = await api.get(`/api/categories/${id}`);
    return response.data; // ApiResponse<CategoryDTO>
  }
};

export default categoryService;
