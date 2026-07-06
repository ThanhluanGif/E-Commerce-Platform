import api from './api';

const referralService = {
  getMyReferrals: async () => {
    const response = await api.get('/api/referrals');
    return response.data;
  },

  getReferralCode: async () => {
    const response = await api.get('/api/referrals/code');
    return response.data;
  }
};

export default referralService;
