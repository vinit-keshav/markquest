import apiClient from "./apiClient";

export const loginApi = (userData) => {
  return apiClient.post("/auth/login", userData);
};

export const signupApi = (userData) => {
  return apiClient.post("/auth/signup", userData, {
    headers: { "Content-Type": "multipart/form-data" },
  });
};

export const verifyOtpApi = (otpData) => {
  return apiClient.post("/auth/verify-otp", otpData);
};

export const resendOtpApi = (otpData) => {
  return apiClient.post("/auth/resend-otp", otpData);
};
