import api from './api';

const chatService = {
  getConversations: async (isSeller) => {
    const response = await api.get(`/api/chat/conversations?isSeller=${isSeller}`);
    return response.data;
  },
  getMessages: async (conversationId) => {
    const response = await api.get(`/api/chat/conversations/${conversationId}/messages`);
    return response.data;
  },
  sendMessage: async (conversationId, content) => {
    const response = await api.post(`/api/chat/conversations/${conversationId}/messages`, { content });
    return response.data;
  },
  createConversation: async (sellerId) => {
    const response = await api.post(`/api/chat/conversations/${sellerId}`);
    return response.data;
  }
};

export default chatService;
