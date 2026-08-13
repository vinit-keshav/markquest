import { Link } from "react-router-dom";

function Dashboard() {
  return (
    <main className="dashboard-page">
      <header className="dashboard-hero">
        <p className="eyebrow">MarketQuest AI</p>
        <h1>Investment Practice Dashboard</h1>
        <p>
          Manage paper trades, monitor buying power, and keep research notes in one focused workspace.
        </p>
      </header>

      <section className="dashboard-grid" aria-label="Dashboard actions">
        <article className="dashboard-card">
          <div>
            <span className="card-kicker">Trading</span>
            <h2>Paper Trading Desk</h2>
            <p>Place paper buy and sell orders using the latest available stock prices.</p>
          </div>
          <Link className="primary-action link-action" to="/trading-lab">Open trading desk</Link>
        </article>

        <article className="dashboard-card">
          <div>
            <span className="card-kicker">Research</span>
            <h2>Watchlist</h2>
            <p>Track companies, save observations, and prepare ideas before placing paper trades.</p>
          </div>
          <Link className="secondary-action" to="/watchlist">Open watchlist</Link>
        </article>
      </section>
    </main>
  );
}

export default Dashboard;
