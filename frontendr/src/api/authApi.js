import apiClient from "./apiClient";

export const loginApi = (userData) => {
  return apiClient.post("/auth/login", userData);
};

export const signupApi = (userData) => {
  return apiClient.post("/auth/signup", userData);
};
