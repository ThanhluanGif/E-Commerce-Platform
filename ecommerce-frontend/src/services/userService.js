import api from './api';

const userService = {
  getProfile: async () => {
    const response = await api.get('/api/users/profile');
    return response.data; // ApiResponse<UserDTO>
  },

  updateProfile: async (profileData) => {
    const response = await api.put('/api/users/profile', profileData);
    return response.data; // ApiResponse<UserDTO>
  },

  changePassword: async (passwordData) => {
    const response = await api.put('/api/users/change-password', passwordData);
    return response.data; // ApiResponse<Void>
  }
};

export default userService;
