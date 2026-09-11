import { attachSession } from "./session";
import axios from "axios";

const watchlistClient = attachSession(axios.create({
  baseURL:
    import.meta.env.VITE_WATCHLIST_API_BASE_URL || "http://localhost:8082/api",
}));


export const getWatchlistApi = () => watchlistClient.get("/watchlist");

export const addWatchlistApi = (data) => watchlistClient.post("/watchlist", data);

export const updateWatchlistNoteApi = (id, data) =>
  watchlistClient.put(`/watchlist/${id}`, data);
