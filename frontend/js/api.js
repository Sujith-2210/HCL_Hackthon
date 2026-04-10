/**
 * api.js – Centralized API layer with Axios-like fetch wrapper
 * Handles JWT token injection and error normalization
 */

const API_BASE_URL =
  window.HOTEL_API_BASE_URL ||
  localStorage.getItem('hotel_api_base_url') ||
  'http://localhost:8080';

const API = {
  async request(method, endpoint, body = null, options = {}) {
    const headers = {};
    const token = localStorage.getItem('hotel_token');
    if (token) headers['Authorization'] = `Bearer ${token}`;

    const config = {
      method,
      headers: { ...headers, ...(options.headers || {}) },
    };
    if (body !== null && body !== undefined) {
      config.headers['Content-Type'] = 'application/json';
      config.body = JSON.stringify(body);
    }

    const url = endpoint.startsWith('http') ? endpoint : `${API_BASE_URL}${endpoint}`;

    const response = await fetch(url, config);
    const contentType = response.headers.get('content-type');
    const raw = await response.text();
    let data = null;
    if (raw) {
      if (contentType && contentType.includes('application/json')) {
        try {
          data = JSON.parse(raw);
        } catch {
          data = null;
        }
      } else {
        data = raw;
      }
    }

    if (!response.ok) {
      const errorMessage =
        (typeof data === 'string' ? data : data?.message) || `HTTP ${response.status}`;
      const error = new Error(errorMessage);
      error.response = { status: response.status, data };
      throw error;
    }

    return { status: response.status, data };
  },

  get: (endpoint, options) => API.request('GET', endpoint, null, options),
  post: (endpoint, body, options) => API.request('POST', endpoint, body, options),
  patch: (endpoint, body, options) => API.request('PATCH', endpoint, body, options),
  put: (endpoint, body, options) => API.request('PUT', endpoint, body, options),
  delete: (endpoint, options) => API.request('DELETE', endpoint, null, options),
};
