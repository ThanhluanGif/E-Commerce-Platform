import api from './api';

const authService = {
  login: async (username, password) => {
    const response = await api.post('/api/auth/login', { username, password });
    return response.data; // This returns ApiResponse<LoginResponse>
  },

  register: async (registerData) => {
    const response = await api.post('/api/auth/register', registerData);
    return response.data; // This returns ApiResponse<UserDTO>
  }
};

export default authService;
