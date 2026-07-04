import { Link } from "react-router-dom";

function Dashboard() {
  return (
    <main className="watchlist-page">
      <h1>Welcome to MarketQuest Dashboard</h1>
      <Link className="secondary-action" to="/watchlist">Open watchlist</Link>
    </main>
  );
}

export default Dashboard;
