import api from './api';

const aiService = {
  generateDescription: async (name, category, keywords) => {
    try {
      const response = await api.post('/api/ai/copywriter', { name, category, keywords });
      return response.data;
    } catch (error) {
      console.error('Error generating AI description', error);
      return { success: false, message: error.response?.data?.message || 'Có lỗi xảy ra khi gọi AI!' };
    }
  },

  searchByImage: async (imageUrl) => {
    try {
      const response = await api.post('/api/ai/visual-search', { imageUrl });
      return response.data;
    } catch (error) {
      console.error('Error in visual search', error);
      return { success: false, message: 'Có lỗi xảy ra khi tìm kiếm bằng hình ảnh!' };
    }
  },

  askChatbot: async (message) => {
    try {
      const response = await api.post('/api/ai/chatbot', { message });
      return response.data;
    } catch (error) {
      console.error('Error querying AI chatbot', error);
      return { success: false, message: 'Trợ lý ảo bận, vui lòng thử lại sau!' };
    }
  }
};

export default aiService;
