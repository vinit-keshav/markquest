import { createSlice, createAsyncThunk } from "@reduxjs/toolkit";
import { loginApi, resendOtpApi, resetPasswordApi, signupApi, verifyOtpApi } from "../../api/authApi";

const getApiError = (error, fallback) => {
  const data = error.response?.data;
  if (typeof data === "string" && data.trim()) return data;
  if (typeof data?.message === "string" && data.message.trim()) return data.message;
  return fallback;
};

export const signupUser = createAsyncThunk("auth/signup", async (data, { rejectWithValue }) => {
  try {
    const response = await signupApi(data);
    return response.data;
  } catch (error) {
    return rejectWithValue(getApiError(error, "Signup failed"));
  }
});

export const loginUser = createAsyncThunk("auth/login", async (data, { rejectWithValue }) => {
  try {
    const response = await loginApi(data);
    return response.data;
  } catch (error) {
    return rejectWithValue(getApiError(error, "Login failed"));
  }
});

export const verifyOtpUser = createAsyncThunk("auth/verifyOtp", async (data, { rejectWithValue }) => {
  try {
    const response = await verifyOtpApi(data);
    return response.data;
  } catch (error) {
    return rejectWithValue(getApiError(error, "OTP verification failed"));
  }
});

export const resendOtpUser = createAsyncThunk("auth/resendOtp", async (data, { rejectWithValue }) => {
  try {
    const response = await resendOtpApi(data);
    return response.data;
  } catch (error) {
    return rejectWithValue(getApiError(error, "OTP resend failed"));
  }
});

export const resetPasswordUser = createAsyncThunk("auth/resetPassword", async (data, { rejectWithValue }) => {
  try {
    const response = await resetPasswordApi(data);
    return response.data;
  } catch (error) {
    return rejectWithValue(getApiError(error, "Password update failed"));
  }
});

const authSlice = createSlice({
  name: "auth",
  initialState: {
    token: localStorage.getItem("token") || null,
    user: JSON.parse(localStorage.getItem("user") || "null"),
    loading: false,
    error: null,
  },
  reducers: {
    logout: (state) => {
      state.token = null;
      state.user = null;
      localStorage.removeItem("token");
      localStorage.removeItem("user");
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(loginUser.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(loginUser.fulfilled, (state, action) => {
        const authPayload = Array.isArray(action.payload) ? action.payload[0] : action.payload;

        state.loading = false;
        state.token = authPayload?.token || null;
        state.user = {
          name: authPayload?.name,
          email: authPayload?.email,
          mobile: authPayload?.mobile,
          filename: authPayload?.filename,
        };

        if (authPayload?.token) {
          localStorage.setItem("token", authPayload.token);
          localStorage.setItem("user", JSON.stringify(state.user));
        }
      })
      .addCase(loginUser.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload || "Login failed";
      })
      .addCase(signupUser.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(signupUser.fulfilled, (state) => {
        state.loading = false;
      })
      .addCase(signupUser.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload || "Signup failed";
      })
      .addCase(verifyOtpUser.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(verifyOtpUser.fulfilled, (state) => {
        state.loading = false;
      })
      .addCase(verifyOtpUser.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload || "OTP verification failed";
      })
      .addCase(resendOtpUser.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(resendOtpUser.fulfilled, (state) => {
        state.loading = false;
      })
      .addCase(resendOtpUser.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload || "OTP resend failed";
      });
  },
});

export const { logout } = authSlice.actions;
export default authSlice.reducer;
