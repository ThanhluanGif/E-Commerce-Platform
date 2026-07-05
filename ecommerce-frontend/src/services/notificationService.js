import api from './api';

const notificationService = {
  getMyNotifications: async () => {
    const response = await api.get('/api/notifications');
    return response.data;
  },

  getUnreadCount: async () => {
    const response = await api.get('/api/notifications/unread-count');
    return response.data;
  },

  readAll: async () => {
    const response = await api.put('/api/notifications/read-all');
    return response.data;
  },

  readOne: async (id) => {
    const response = await api.put(`/api/notifications/${id}/read`);
    return response.data;
  }
};

export default notificationService;
