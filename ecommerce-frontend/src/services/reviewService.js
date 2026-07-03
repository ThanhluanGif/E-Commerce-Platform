import api from './api';

const reviewService = {
  getReviews: async (productId) => {
    const response = await api.get(`/api/products/${productId}/reviews`);
    return response.data; // ApiResponse<List<ReviewDTO>>
  },

  addReview: async (productId, reviewData) => {
    const response = await api.post(`/api/products/${productId}/reviews`, reviewData);
    return response.data; // ApiResponse<ReviewDTO>
  },

  canReview: async (productId) => {
    const response = await api.get(`/api/products/${productId}/reviews/can-review`);
    return response.data; // ApiResponse<Boolean>
  }
};

export default reviewService;
