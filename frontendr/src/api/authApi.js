import apiClient from "./apiClient";

export const loginApi = (userData) => {
  return apiClient.post("/auth/login", userData, { skipSession: true });
};

export const signupApi = (userData) => {
  return apiClient.post("/auth/signup", userData, {
    skipSession: true,
    headers: { "Content-Type": "multipart/form-data" },
  });
};

export const verifyOtpApi = (otpData) => {
  return apiClient.post("/auth/verify-otp", otpData, { skipSession: true });
};

export const resendOtpApi = (otpData) => {
  return apiClient.post("/auth/resend-otp", otpData, { skipSession: true });
};

export const resetPasswordApi = (data) => {
  return apiClient.post("/auth/reset-password", data);
};
