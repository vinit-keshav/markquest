import { Link } from "react-router-dom";
import { useSelector } from "react-redux";

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || "http://localhost:8081/api";

const initials = (user) =>
  (user?.name || user?.email || "MQ")
    .split(/\s+/)
    .map((part) => part[0])
    .join("")
    .slice(0, 2)
    .toUpperCase();

function Profile() {
  const user = useSelector((state) => state.auth.user);
  const imageUrl = user?.filename ? `${apiBaseUrl}/auth/uploads/${encodeURIComponent(user.filename)}` : "";

  return (
    <main className="settings-page">
      <section className="profile-hero">
        <div className="profile-avatar-xl">
          {imageUrl ? <img src={imageUrl} alt="" /> : <span>{initials(user)}</span>}
        </div>
        <div>
          <p className="eyebrow">Profile</p>
          <h1>{user?.name || "MarketQuest User"}</h1>
          <p>Manage identity, account access, and your paper trading workspace preferences.</p>
        </div>
      </section>

      <section className="settings-grid">
        <article className="settings-card">
          <h2>Account Details</h2>
          <dl className="profile-list">
            <div>
              <dt>Name</dt>
              <dd>{user?.name || "Not available"}</dd>
            </div>
            <div>
              <dt>Email</dt>
              <dd>{user?.email || "Not provided"}</dd>
            </div>
            <div>
              <dt>Mobile</dt>
              <dd>{user?.mobile || "Not provided"}</dd>
            </div>
          </dl>
        </article>

        <article className="settings-card action-card">
          <h2>Security</h2>
          <p>Update your password and review account preferences from settings.</p>
          <Link className="primary-action link-action" to="/settings">Open settings</Link>
        </article>
      </section>
    </main>
  );
}

export default Profile;
