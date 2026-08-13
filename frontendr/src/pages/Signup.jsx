import { useState } from "react";
import { useDispatch } from "react-redux";
import { useSelector } from "react-redux";
import { Link } from "react-router-dom";
import { useNavigate } from "react-router-dom";
import { signupUser } from "../features/auth/authSlice";
import { getIdentifierPayload } from "../utils/authIdentifier";

const Signup = () => {
  const [form, setForm] = useState({ name: "", identifier: "", password: "" });
  const dispatch = useDispatch();
  const [file, setFile] = useState(null);
  const navigate = useNavigate();
  const { loading, error } = useSelector((state) => state.auth);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const identifier = form.identifier.trim();
    const signupPayload = {
      name: form.name.trim(),
      password: form.password,
      ...getIdentifierPayload(identifier),
    };
    const formData = new FormData();
    formData.append(
      "request",
      new Blob([JSON.stringify(signupPayload)], { type: "application/json" }),
    );
    formData.append("file", file);
    const result = await dispatch(signupUser(formData));
    if (signupUser.fulfilled.match(result)) {
      navigate("/verify-otp", { state: { identifier } });
    }
  };

  return (
    <main className="auth-page">
      <section className="auth-shell" aria-label="Signup">
        <div className="auth-intro">
          <p className="brand-mark">MarketQuest AI</p>
          <h1>Practice market decisions with confidence</h1>
          <p className="auth-copy">
            Create a secure workspace for paper trading, portfolio tracking, and structured market research.
          </p>
        </div>

        <form className="auth-form" onSubmit={handleSubmit}>
          <div className="auth-heading">
            <h2>Create account</h2>
            <p>Set up your MarketQuest profile.</p>
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
              placeholder="Create a password"
              autoComplete="new-password"
              onChange={handleChange}
              required
            />
          </label>
          <label className="field-group">
            <span>Profile image</span>
            <input
              name="file"
              type="file"
              accept="image/*"
              onChange={(e) => setFile(e.target.files?.[0] || null)}
              required
            />
          </label>
          <button className="primary-action" type="submit" disabled={loading}>
            {loading ? "Creating account..." : "Create account"}
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
