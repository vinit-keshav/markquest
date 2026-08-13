function AppFooter() {
  return (
    <footer className="app-footer">
      <div className="footer-brand">
        <span>MQ</span>
        <div>
          <strong>MarketQuest AI</strong>
          <p>Paper trading and portfolio analytics workspace.</p>
        </div>
      </div>
      <div className="footer-disclaimer">
        <strong>Educational platform</strong>
        <p>No real orders are placed. Portfolio values and buying power are simulated for practice.</p>
      </div>
      <div className="footer-meta">
        <span>Demo environment</span>
        <span>Not investment advice</span>
      </div>
    </footer>
  );
}

export default AppFooter;
