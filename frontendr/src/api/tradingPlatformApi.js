import axios from "axios";

const tradingClient = axios.create({
  baseURL: import.meta.env.VITE_TRADING_API_BASE_URL || "http://localhost:8084/api",
});

const portfolioClient = axios.create({
  baseURL: import.meta.env.VITE_PORTFOLIO_API_BASE_URL || "http://localhost:8085/api",
});

export const executeTradeApi = (data) => tradingClient.post("/trades", data);

export const getInstrumentsApi = (params) => tradingClient.get("/instruments", { params });

export const getLivePriceApi = (symbol) => tradingClient.get(`/instruments/${symbol}/price`);

export const getPortfolioApi = (userId) => portfolioClient.get(`/portfolio/${userId}`);

export const getPortfolioSummaryApi = (userId) => portfolioClient.get(`/portfolio/${userId}/summary`);

export const depositDemoCashApi = (userId, data) =>
  portfolioClient.post(`/portfolio/${userId}/deposit`, data);

export const updateMarketPriceApi = (userId, data) =>
  portfolioClient.post(`/portfolio/${userId}/prices`, data);
