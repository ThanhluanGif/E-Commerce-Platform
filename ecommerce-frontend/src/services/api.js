import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request Interceptor: attach JWT token if it exists
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('jwtToken');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response Interceptor: handle unauthorized errors globally
api.interceptors.response.use(
  (response) => {
    return response;
  },
  async (error) => {
    const originalRequest = error.config;
    
    // Check if error is 401 and we haven't retried yet and it's not a login/refresh request
    if (error.response && error.response.status === 401 && !originalRequest._retry && !originalRequest.url.includes('/auth/refresh-token')) {
      originalRequest._retry = true;
      try {
        const rToken = localStorage.getItem('refreshToken');
        if (!rToken) {
          throw new Error('No refresh token found');
        }

        const res = await axios.post(`${API_BASE_URL}/api/auth/refresh-token`, {
          refreshToken: rToken
        });

        if (res.data && res.data.success) {
          const { accessToken, refreshToken: newRefreshToken } = res.data.data;
          
          localStorage.setItem('jwtToken', accessToken);
          localStorage.setItem('refreshToken', newRefreshToken);
          window.dispatchEvent(new Event('storage'));

          originalRequest.headers['Authorization'] = `Bearer ${accessToken}`;
          return api(originalRequest);
        }
      } catch (refreshError) {
        // Clear token and user details on unauthorized refresh failure
        localStorage.removeItem('jwtToken');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('userId');
        localStorage.removeItem('username');
        localStorage.removeItem('userRole');
        window.dispatchEvent(new Event('storage'));
        
        // Redirect to login if not already there
        if (window.location.pathname !== '/login') {
          window.location.href = '/login';
        }
        return Promise.reject(refreshError);
      }
    }
    return Promise.reject(error);
  }
);

export default api;
