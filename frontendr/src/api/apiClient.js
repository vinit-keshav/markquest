import { attachSession } from "./session";
import axios from "axios";

const apiClient = attachSession(axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8081/api",
}));

export default apiClient;
