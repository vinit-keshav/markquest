import { useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { resetPasswordUser } from "../features/auth/authSlice";

function Settings() {
  const dispatch = useDispatch();
  const user = useSelector((state) => state.auth.user);
  const { loading } = useSelector((state) => state.auth);
  const [form, setForm] = useState({ currentPassword: "", newPassword: "" });
  const [status, setStatus] = useState("");
  const [error, setError] = useState("");
  const [emailAlerts, setEmailAlerts] = useState(true);
  const [riskReview, setRiskReview] = useState(true);

  const identifier = user?.email || user?.mobile || "";

  const updatePassword = async (event) => {
    event.preventDefault();
    setStatus("");
    setError("");

    const result = await dispatch(resetPasswordUser({ identifier, ...form }));
    if (resetPasswordUser.fulfilled.match(result)) {
      setStatus(result.payload || "Password updated successfully");
      setForm({ currentPassword: "", newPassword: "" });
    } else {
      setError(result.payload || "Password update failed");
    }
  };

  return (
    <main className="settings-page">
      <header className="settings-hero">
        <p className="eyebrow">Settings</p>
        <h1>Account and Security Settings</h1>
        <p>Manage access, profile preferences, and workspace defaults.</p>
      </header>

      <section className="settings-grid">
        <form className="settings-card" onSubmit={updatePassword}>
          <h2>Password Reset</h2>
          <p>Use your current password to set a new account password.</p>
          {status && <p className="auth-success">{status}</p>}
          {error && <p className="auth-error">{error}</p>}
          <label className="field-group">
            <span>Account identifier</span>
            <input value={identifier} readOnly />
          </label>
          <label className="field-group">
            <span>Current password</span>
            <input
              type="password"
              value={form.currentPassword}
              onChange={(event) => setForm((current) => ({ ...current, currentPassword: event.target.value }))}
              required
            />
          </label>
          <label className="field-group">
            <span>New password</span>
            <input
              type="password"
              minLength={6}
              value={form.newPassword}
              onChange={(event) => setForm((current) => ({ ...current, newPassword: event.target.value }))}
              required
            />
          </label>
          <button className="primary-action" type="submit" disabled={loading || !identifier}>
            Update password
          </button>
        </form>

        <article className="settings-card">
          <h2>Workspace Preferences</h2>
          <p>These preferences are local to your current browser session.</p>
          <label className="toggle-row">
            <span>
              <strong>Email trade summaries</strong>
              <small>Receive key activity and account updates.</small>
            </span>
            <input type="checkbox" checked={emailAlerts} onChange={(event) => setEmailAlerts(event.target.checked)} />
          </label>
          <label className="toggle-row">
            <span>
              <strong>Show risk review prompts</strong>
              <small>Keep decision quality checks visible before orders.</small>
            </span>
            <input type="checkbox" checked={riskReview} onChange={(event) => setRiskReview(event.target.checked)} />
          </label>
        </article>
      </section>
    </main>
  );
}

export default Settings;
