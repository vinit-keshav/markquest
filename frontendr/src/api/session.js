export function attachSession(client) {
  client.defaults.timeout = 20000;
  client.interceptors.request.use((config) => {
    const token = localStorage.getItem("token");
    if (config.skipSession) {
      // Public auth requests must work even when a previous login has expired.
      delete config.headers.Authorization;
    } else if (token) {
      config.headers.Authorization = "Bearer " + token;
    }
    return config;
  });
  client.interceptors.response.use((response) => response, (error) => {
    if (error.response?.status === 401 && !error.config?.skipSession) {
      localStorage.removeItem("token");
      localStorage.removeItem("user");
      window.location.assign("/login");
    }
    return Promise.reject(error);
  });
  return client;
}
export function apiError(error, fallback) {
  const data = error.response?.data;
  return typeof data === "string" ? data : data?.message || data?.detail || fallback;
}
