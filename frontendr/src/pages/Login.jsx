import { useState } from "react";
import { useDispatch } from "react-redux";
import { useSelector } from "react-redux";
import { Link } from "react-router-dom";
import { useNavigate } from "react-router-dom";
import { loginUser } from "../features/auth/authSlice";

function Login() {
  const [form, setForm] = useState({ email: "", password: "" });
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { loading, error } = useSelector((state) => state.auth);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const result = await dispatch(loginUser(form));
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
          <h1>Welcome back</h1>
          <p className="auth-copy">
            Continue your virtual investing practice, track portfolio progress, and build market confidence.
          </p>
        </div>

        <form className="auth-form" onSubmit={handleSubmit}>
          <div className="auth-heading">
            <h2>Log in</h2>
            <p>Use your account email and password.</p>
          </div>

          {error && <p className="auth-error">{error}</p>}

          <label className="field-group">
            <span>Email</span>
            <input
              name="email"
              type="email"
              placeholder="you@example.com"
              autoComplete="email"
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
            {loading ? "Logging in..." : "Log in"}
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
