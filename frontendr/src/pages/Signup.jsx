import { useState } from "react";
import { useDispatch } from "react-redux";
import { useSelector } from "react-redux";
import { Link } from "react-router-dom";
import { useNavigate } from "react-router-dom";
import { signupUser } from "../features/auth/authSlice";

const Signup = () => {
  const [form, setForm] = useState({ name: "", email: "", password: "" });
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { loading, error } = useSelector((state) => state.auth);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const result = await dispatch(signupUser(form));
    if (!result.error) {
      navigate("/login");
    }
  };

  return (
    <main className="auth-page">
      <section className="auth-shell" aria-label="Signup">
        <div className="auth-intro">
          <p className="brand-mark">MarketQuest AI</p>
          <h1>Start learning by trading virtually</h1>
          <p className="auth-copy">
            Create your account and begin with a practice portfolio built for beginner investors.
          </p>
        </div>

        <form className="auth-form" onSubmit={handleSubmit}>
          <div className="auth-heading">
            <h2>Create account</h2>
            <p>Enter your details to set up your workspace.</p>
          </div>

          {error && <p className="auth-error">{error}</p>}

          <label className="field-group">
            <span>Name</span>
            <input
              name="name"
              type="text"
              placeholder="Your full name"
              autoComplete="name"
              onChange={handleChange}
              required
            />
          </label>

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
              placeholder="Create a password"
              autoComplete="new-password"
              onChange={handleChange}
              required
            />
          </label>

          <button className="primary-action" type="submit" disabled={loading}>
            {loading ? "Creating..." : "Create account"}
          </button>

          <p className="auth-switch">
            Already have an account? <Link to="/login">Log in</Link>
          </p>
        </form>
      </section>
    </main>
  );
};

export default Signup;
