/**
 * api.js – Centralized API layer with Axios-like fetch wrapper
 * Handles JWT token injection and error normalization
 */

const API_BASE_URL = 'http://localhost:8080';

const API = {
  async request(method, endpoint, body = null, options = {}) {
    const headers = { 'Content-Type': 'application/json' };
    const token = localStorage.getItem('hotel_token');
    if (token) headers['Authorization'] = `Bearer ${token}`;

    const config = {
      method,
      headers: { ...headers, ...(options.headers || {}) },
    };
    if (body) config.body = JSON.stringify(body);

    const url = endpoint.startsWith('http') ? endpoint : `${API_BASE_URL}${endpoint}`;

    const response = await fetch(url, config);
    const contentType = response.headers.get('content-type');
    let data = null;
    if (contentType && contentType.includes('application/json')) {
      data = await response.json();
    }

    if (!response.ok) {
      const error = new Error(data?.message || `HTTP ${response.status}`);
      error.response = { status: response.status, data };
      throw error;
    }

    return { status: response.status, data };
  },

  get: (endpoint, options) => API.request('GET', endpoint, null, options),
  post: (endpoint, body, options) => API.request('POST', endpoint, body, options),
  put: (endpoint, body, options) => API.request('PUT', endpoint, body, options),
  delete: (endpoint, options) => API.request('DELETE', endpoint, null, options),
};
