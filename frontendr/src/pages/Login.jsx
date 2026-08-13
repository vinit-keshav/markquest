import { useState } from "react";
import { useDispatch } from "react-redux";
import { useSelector } from "react-redux";
import { Link } from "react-router-dom";
import { useLocation } from "react-router-dom";
import { useNavigate } from "react-router-dom";
import { loginUser } from "../features/auth/authSlice";
import { getIdentifierPayload } from "../utils/authIdentifier";

function Login() {
  const [form, setForm] = useState({ identifier: "", password: "" });
  const dispatch = useDispatch();
  const location = useLocation();
  const navigate = useNavigate();
  const { loading, error } = useSelector((state) => state.auth);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const loginPayload = {
      password: form.password,
      ...getIdentifierPayload(form.identifier.trim()),
    };
    const result = await dispatch(loginUser(loginPayload));
    const authPayload = Array.isArray(result.payload) ? result.payload[0] : result.payload;

    if (authPayload?.token) {
      navigate("/dashboard");
    }
  };

  return (
    <main className="auth-page">
      <section className="auth-shell" aria-label="Login">
        <div className="auth-intro">
          <p className="brand-mark">MarketQuest AI</p>
          <h1>Access your trading workspace</h1>
          <p className="auth-copy">
            Review watchlists, manage paper positions, and evaluate decisions with real market context.
          </p>
        </div>

        <form className="auth-form" onSubmit={handleSubmit}>
          <div className="auth-heading">
            <h2>Sign in</h2>
            <p>Use your registered email or mobile number.</p>
          </div>

          {location.state?.message && <p className="auth-success">{location.state.message}</p>}
          {error && <p className="auth-error">{error}</p>}

          <label className="field-group">
            <span>Email or mobile</span>
            <input
              name="identifier"
              type="text"
              placeholder="you@example.com or 9876543210"
              autoComplete="username"
              value={form.identifier}
              onChange={handleChange}
              required
            />
          </label>

          <label className="field-group">
            <span>Password</span>
            <input
              name="password"
              type="password"
              placeholder="Enter your password"
              autoComplete="current-password"
              onChange={handleChange}
              required
            />
          </label>

          <button className="primary-action" type="submit" disabled={loading}>
            {loading ? "Signing in..." : "Sign in"}
          </button>

          <p className="auth-switch">
            New to MarketQuest? <Link to="/signup">Create an account</Link>
          </p>
        </form>
      </section>
    </main>
  );
}

export default Login;
