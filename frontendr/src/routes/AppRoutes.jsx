import { Routes, Route } from "react-router-dom";
import Login from "../pages/Login";
import Signup from "../pages/Signup";
import VerifyOtp from "../pages/VerifyOtp";
import Dashboard from "../pages/Dashboard";
import Watchlist from "../pages/Watchlist";
import TradingLab from "../pages/TradingLab";
import Profile from "../pages/Profile";
import Settings from "../pages/Settings";
import AppShell from "../components/AppShell";
import ProtectedRoute from "../components/ProtectedRoute";

const protectedPage = (page) => (
  <ProtectedRoute>
    <AppShell>{page}</AppShell>
  </ProtectedRoute>
);

function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<Signup />} />
      <Route path="/signup" element={<Signup />} />
      <Route path="/verify-otp" element={<VerifyOtp />} />
      <Route path="/login" element={<Login />} />
      <Route path="/dashboard" element={protectedPage(<Dashboard />)} />
      <Route path="/watchlist" element={protectedPage(<Watchlist />)} />
      <Route path="/trading-lab" element={protectedPage(<TradingLab />)} />
      <Route path="/profile" element={protectedPage(<Profile />)} />
      <Route path="/settings" element={protectedPage(<Settings />)} />
    </Routes>
  );
}

export default AppRoutes;
