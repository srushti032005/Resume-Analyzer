import api from './client.js';

export const authApi = {
  register: (payload) => api.post('/auth/register', payload),
  login: (payload) => api.post('/auth/login', payload),
  logout: (refreshToken) => api.post('/auth/logout', { refreshToken }),
  forgotPassword: (email) => api.post('/auth/forgot-password', { email }),
  resetPassword: (token, newPassword) =>
    api.post('/auth/reset-password', { token, newPassword }),
  me: () => api.get('/users/me'),
};

export const resumeApi = {
  upload: (file, onProgress) => {
    const form = new FormData();
    form.append('file', file);
    return api.post('/resumes/upload', form, {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress: (e) => {
        if (onProgress && e.total) {
          onProgress(Math.round((e.loaded * 100) / e.total));
        }
      },
    });
  },
  list: () => api.get('/resumes'),
  get: (id) => api.get(`/resumes/${id}`),
  reanalyze: (id) => api.post(`/resumes/${id}/reanalyze`),
  remove: (id) => api.delete(`/resumes/${id}`),
};

export const dashboardApi = {
  stats: () => api.get('/dashboard'),
};

export const adminApi = {
  users: () => api.get('/admin/users'),
  resumes: () => api.get('/admin/resumes'),
  stats: () => api.get('/admin/stats'),
  deleteUser: (id) => api.delete(`/admin/users/${id}`),
  toggleUser: (id) => api.patch(`/admin/users/${id}/toggle`),
};
