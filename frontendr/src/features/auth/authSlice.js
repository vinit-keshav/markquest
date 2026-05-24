import { createSlice, createAsyncThunk } from "@reduxjs/toolkit";
import { loginApi, signupApi } from "../../api/authApi";

export const signupUser = createAsyncThunk("auth/signup", async (data) => {
  const response = await signupApi(data);
  return response.data;
});

export const loginUser = createAsyncThunk("auth/login", async (data) => {
  const response = await loginApi(data);
  return response.data;
});

const authSlice = createSlice({
  name: "auth",
  initialState: {
    token: localStorage.getItem("token") || null,
    user: null,
    loading: false,
    error: null,
  },
  reducers: {
    logout: (state) => {
      state.token = null;
      state.user = null;
      localStorage.removeItem("token");
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
        state.user = { name: authPayload?.name, email: authPayload?.email };

        if (authPayload?.token) {
          localStorage.setItem("token", authPayload.token);
        }
      })
      .addCase(loginUser.rejected, (state) => {
        state.loading = false;
        state.error = "Login failed";
      })
      .addCase(signupUser.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(signupUser.fulfilled, (state) => {
        state.loading = false;
      })
      .addCase(signupUser.rejected, (state) => {
        state.loading = false;
        state.error = "Signup failed";
      });
  },
});

export const { logout } = authSlice.actions;
export default authSlice.reducer;
