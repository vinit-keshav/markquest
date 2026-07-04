import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import {
  addWatchlistApi,
  getWatchlistApi,
  updateWatchlistNoteApi,
} from "../../api/watchlistApi";

const getApiError = (error, fallback) => {
  const data = error.response?.data;
  if (typeof data === "string" && data.trim()) return data;
  if (typeof data?.message === "string" && data.message.trim()) return data.message;
  return fallback;
};

export const fetchWatchlist = createAsyncThunk(
  "watchlist/fetch",
  async (_, { rejectWithValue }) => {
    try {
      const response = await getWatchlistApi();
      return response.data;
    } catch (error) {
      return rejectWithValue(getApiError(error, "Failed to load watchlist"));
    }
  },
);

export const addWatchlistStock = createAsyncThunk(
  "watchlist/add",
  async (data, { rejectWithValue }) => {
    try {
      const response = await addWatchlistApi(data);
      return response.data;
    } catch (error) {
      return rejectWithValue(getApiError(error, "Failed to add stock"));
    }
  },
);

export const updateWatchlistNote = createAsyncThunk(
  "watchlist/updateNote",
  async ({ id, note }, { rejectWithValue }) => {
    try {
      const response = await updateWatchlistNoteApi(id, { note });
      return response.data;
    } catch (error) {
      return rejectWithValue(getApiError(error, "Failed to update note"));
    }
  },
);

const watchlistSlice = createSlice({
  name: "watchlist",
  initialState: {
    items: [],
    loading: false,
    error: null,
  },
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchWatchlist.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchWatchlist.fulfilled, (state, action) => {
        state.loading = false;
        state.items = action.payload;
      })
      .addCase(fetchWatchlist.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload || "Failed to load watchlist";
      })
      .addCase(addWatchlistStock.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(addWatchlistStock.fulfilled, (state, action) => {
        state.loading = false;
        state.items.push(action.payload);
      })
      .addCase(addWatchlistStock.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload || "Failed to add stock";
      })
      .addCase(updateWatchlistNote.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(updateWatchlistNote.fulfilled, (state, action) => {
        state.loading = false;
        const index = state.items.findIndex((item) => item.id === action.payload.id);
        if (index !== -1) {
          state.items[index] = action.payload;
        }
      })
      .addCase(updateWatchlistNote.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload || "Failed to update note";
      });
  },
});

export default watchlistSlice.reducer;
