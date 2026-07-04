import { useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { resendOtpUser, verifyOtpUser } from "../features/auth/authSlice";

const VerifyOtp = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const { loading, error } = useSelector((state) => state.auth);
  const [form, setForm] = useState({
    identifier: location.state?.identifier || "",
    otp: "",
  });
  const [statusMessage, setStatusMessage] = useState(location.state?.message || "");

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleOtpChange = (e) => {
    const otp = e.target.value.replace(/\D/g, "").slice(0, 6);
    setForm({ ...form, otp });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setStatusMessage("");
    const result = await dispatch(
      verifyOtpUser({ identifier: form.identifier.trim(), otp: form.otp }),
    );

    if (verifyOtpUser.fulfilled.match(result)) {
      navigate("/login", {
        replace: true,
        state: { message: "Account verified successfully. You can log in now." },
      });
    }
  };

  const handleResendOtp = async () => {
    setStatusMessage("");
    const result = await dispatch(resendOtpUser({ identifier: form.identifier.trim() }));

    if (resendOtpUser.fulfilled.match(result)) {
      setForm({ ...form, otp: "" });
      setStatusMessage(result.payload || "OTP resent successfully");
    }
  };

  return (
    <main className="auth-page">
      <section className="auth-shell" aria-label="OTP verification">
        <div className="auth-intro">
          <p className="brand-mark">MarketQuest AI</p>
          <h1>Verify your account</h1>
          <p className="auth-copy">
            Enter the one-time code sent during signup to activate your account.
          </p>
        </div>

        <form className="auth-form" onSubmit={handleSubmit}>
          <div className="auth-heading">
            <h2>Enter OTP</h2>
            <p>Use the email or mobile number from signup.</p>
          </div>

          {statusMessage && <p className="auth-success">{statusMessage}</p>}
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
            <span>OTP</span>
            <input
              name="otp"
              type="text"
              inputMode="numeric"
              pattern="[0-9]{6}"
              maxLength={6}
              placeholder="6-digit code"
              autoComplete="one-time-code"
              value={form.otp}
              onChange={handleOtpChange}
              required
            />
          </label>

          <button className="primary-action" type="submit" disabled={loading}>
            {loading ? "Verifying..." : "Verify account"}
          </button>

          <p className="auth-switch">
            Didn't get the code?{" "}
            <button
              className="inline-action"
              type="button"
              onClick={handleResendOtp}
              disabled={loading || !form.identifier.trim()}
            >
              Resend OTP
            </button>
          </p>

          <p className="auth-switch">
            Already verified? <Link to="/login">Log in</Link>
          </p>
        </form>
      </section>
    </main>
  );
};

export default VerifyOtp;
