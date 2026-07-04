import axios from "axios";

const watchlistClient = axios.create({
  baseURL:
    import.meta.env.VITE_WATCHLIST_API_BASE_URL || "http://localhost:8082/api",
});

watchlistClient.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

export const getWatchlistApi = () => watchlistClient.get("/watchlist");

export const addWatchlistApi = (data) => watchlistClient.post("/watchlist", data);

export const updateWatchlistNoteApi = (id, data) =>
  watchlistClient.put(`/watchlist/${id}`, data);
