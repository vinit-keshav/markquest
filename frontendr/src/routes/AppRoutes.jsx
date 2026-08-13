import { Routes, Route } from "react-router-dom";
import Login from "../pages/Login";
import Signup from "../pages/Signup";
import VerifyOtp from "../pages/VerifyOtp";
import Dashboard from "../pages/Dashboard";
import Watchlist from "../pages/Watchlist";
import TradingLab from "../pages/TradingLab";
import ProtectedRoute from "../components/ProtectedRoute";

function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<Signup />} />
      <Route path="/signup" element={<Signup />} />
      <Route path="/verify-otp" element={<VerifyOtp />} />
      <Route path="/login" element={<Login />} />
      <Route path="/dashboard" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
      <Route path="/watchlist" element={<ProtectedRoute><Watchlist /></ProtectedRoute>} />
      <Route path="/trading-lab" element={<ProtectedRoute><TradingLab /></ProtectedRoute>} />
    </Routes>
  );
}

export default AppRoutes;
