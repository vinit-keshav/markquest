import { Link, NavLink, useNavigate } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import { logout } from "../features/auth/authSlice";

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || "http://localhost:8081/api";

const getInitials = (user) => {
  const value = user?.name || user?.email || user?.mobile || "MQ";
  return value
    .split(/\s+/)
    .map((part) => part[0])
    .join("")
    .slice(0, 2)
    .toUpperCase();
};

const getProfileImageUrl = (user) => {
  if (!user?.filename) return "";
  return `${apiBaseUrl}/auth/uploads/${encodeURIComponent(user.filename)}`;
};

function AppHeader() {
  const user = useSelector((state) => state.auth.user);
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const profileImage = getProfileImageUrl(user);

  const signOut = () => {
    dispatch(logout());
    navigate("/login");
  };

  return (
    <header className="app-header">
      <Link className="app-brand" to="/dashboard">
        <span>MQ</span>
        <strong>MarketQuest AI</strong>
      </Link>

      <nav className="app-nav" aria-label="Primary navigation">
        <NavLink to="/dashboard">Dashboard</NavLink>
        <NavLink to="/trading-lab">Trading</NavLink>
        <NavLink to="/watchlist">Watchlist</NavLink>
        <NavLink to="/settings">Settings</NavLink>
      </nav>

      <div className="header-profile">
        <Link className="profile-chip" to="/profile">
          {profileImage ? <img src={profileImage} alt="" /> : <span>{getInitials(user)}</span>}
          <small>{user?.name || "Profile"}</small>
        </Link>
        <button className="ghost-action" type="button" onClick={signOut}>
          Sign out
        </button>
      </div>
    </header>
  );
}

export default AppHeader;
