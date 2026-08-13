import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import {
  depositDemoCashApi,
  executeTradeApi,
  getInstrumentsApi,
  getLivePriceApi,
  getPortfolioSummaryApi,
  updateMarketPriceApi,
} from "../api/tradingPlatformApi";

const defaultUserId = "user1";
const markets = [
  { label: "All", value: "" },
  { label: "NSE", value: "NSE" },
  { label: "BSE", value: "BSE" },
  { label: "US", value: "US" },
];

const chartWidth = 640;
const chartHeight = 220;

const marketPulse = [18, 38, 28, 54, 43, 70, 62, 88, 74, 96, 86, 112];

const formatMoney = (value, currency = "INR") =>
  Number(value || 0).toLocaleString(currency === "INR" ? "en-IN" : "en-US", {
    style: "currency",
    currency,
    maximumFractionDigits: 2,
  });

const ProfitLossChart = ({ data, currency }) => {
  const filtered = data.filter((item) => item.currency === currency);
  if (filtered.length === 0) {
    return <p className="empty-state compact-empty">No realized profit/loss history for {currency} yet.</p>;
  }

  const values = filtered.map((item) => Number(item.profitLoss || 0));
  const max = Math.max(...values, 0);
  const min = Math.min(...values, 0);
  const range = max - min || 1;
  const xStep = filtered.length > 1 ? chartWidth / (filtered.length - 1) : chartWidth;
  const yFor = (value) => chartHeight - ((value - min) / range) * chartHeight;
  const zeroY = yFor(0);
  const points = filtered.map((item, index) => `${index * xStep},${yFor(Number(item.profitLoss || 0))}`).join(" ");

  return (
    <div className="pl-chart">
      <svg viewBox={`0 0 ${chartWidth} ${chartHeight}`} role="img" aria-label={`${currency} daily realized profit and loss`}>
        <line className="chart-zero" x1="0" y1={zeroY} x2={chartWidth} y2={zeroY} />
        <polyline className="chart-line" points={points} />
        {filtered.map((item, index) => {
          const value = Number(item.profitLoss || 0);
          return (
            <circle
              className={value >= 0 ? "chart-dot gain" : "chart-dot loss-dot"}
              cx={index * xStep}
              cy={yFor(value)}
              r="5"
              key={`${item.date}-${item.currency}`}
            />
          );
        })}
      </svg>
      <div className="chart-labels">
        {filtered.map((item) => (
          <span key={`${item.date}-${item.currency}`}>{item.date.slice(5)}</span>
        ))}
      </div>
    </div>
  );
};

const TradingLab = () => {
  const [userId, setUserId] = useState(defaultUserId);
  const [market, setMarket] = useState("");
  const [query, setQuery] = useState("");
  const [instruments, setInstruments] = useState([]);
  const [selected, setSelected] = useState(null);
  const [liveQuote, setLiveQuote] = useState(null);
  const [side, setSide] = useState("BUY");
  const [quantity, setQuantity] = useState("1");
  const [tradePrice, setTradePrice] = useState("");
  const [depositCurrency, setDepositCurrency] = useState("INR");
  const [depositAmount, setDepositAmount] = useState("10000");
  const [accounts, setAccounts] = useState([]);
  const [holdings, setHoldings] = useState([]);
  const [trades, setTrades] = useState([]);
  const [dailyProfitLoss, setDailyProfitLoss] = useState([]);
  const [chartCurrency, setChartCurrency] = useState("INR");
  const [loading, setLoading] = useState(false);
  const [priceLoading, setPriceLoading] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const totals = useMemo(() => {
    const byCurrency = new Map();
    holdings.forEach((holding) => {
      const currency = holding.currency || "INR";
      const current = byCurrency.get(currency) || {
        currency,
        investedValue: 0,
        currentValue: 0,
        totalProfitLoss: 0,
      };
      current.investedValue += Number(holding.investedValue || 0);
      current.currentValue += Number(holding.currentValue || 0);
      current.totalProfitLoss += Number(holding.totalProfitLoss || 0);
      byCurrency.set(currency, current);
    });
    return Array.from(byCurrency.values());
  }, [holdings]);

  const realizedTotals = useMemo(() => {
    const byCurrency = new Map();
    trades.forEach((trade) => {
      if (trade.status !== "EXECUTED" || trade.side !== "SELL") return;
      const currency = trade.currency || "INR";
      byCurrency.set(currency, (byCurrency.get(currency) || 0) + Number(trade.profitLoss || 0));
    });
    return Array.from(byCurrency, ([currency, realizedProfitLoss]) => ({ currency, realizedProfitLoss }));
  }, [trades]);

  const loadPortfolio = async (activeUserId = userId) => {
    const response = await getPortfolioSummaryApi(activeUserId.trim());
    setAccounts(response.data.accounts || []);
    setHoldings(response.data.holdings || []);
    setTrades(response.data.trades || []);
    setDailyProfitLoss(response.data.dailyProfitLoss || []);
  };

  const selectInstrument = (instrument) => {
    setSelected(instrument);
    setLiveQuote(null);
    setTradePrice(String(instrument.referencePrice));
    setMessage("");
    setError("");
  };

  const loadLivePrice = async (instrument = selected, showLoader = true) => {
    if (!instrument) return null;

    if (showLoader) setPriceLoading(true);
    try {
      const response = await getLivePriceApi(instrument.symbol);
      setLiveQuote(response.data);
      setTradePrice(String(response.data.price));
      return response.data;
    } catch {
      setError("Live price could not be loaded. Using reference price.");
      return null;
    } finally {
      if (showLoader) setPriceLoading(false);
    }
  };

  useEffect(() => {
    getInstrumentsApi({ market, query })
      .then((response) => {
        setInstruments(response.data);
        if (!selected && response.data.length > 0) selectInstrument(response.data[0]);
      })
      .catch(() => setInstruments([]));
  }, [market, query]);

  useEffect(() => {
    loadPortfolio().catch(() => setHoldings([]));
  }, []);

  useEffect(() => {
    if (!selected) return undefined;

    loadLivePrice(selected);
    const timerId = window.setInterval(() => loadLivePrice(selected, false), 15000);

    return () => window.clearInterval(timerId);
  }, [selected?.symbol]);

  const submitTrade = async (event) => {
    event.preventDefault();
    if (!selected) return;

    setLoading(true);
    setError("");
    setMessage("");

    try {
      const quote = await loadLivePrice(selected, false);
      const executionPrice = Number(quote?.price || tradePrice || selected.referencePrice);

      await executeTradeApi({
        userId: userId.trim(),
        symbol: selected.symbol,
        side,
        quantity: Number(quantity),
        price: executionPrice,
        currency: selected.currency,
      });

      setMessage(
        `${side} paper order submitted for ${selected.symbol} at ${formatMoney(
          executionPrice,
          selected.currency
        )}. Check recent orders for final status.`
      );
      setTimeout(() => loadPortfolio(userId), 700);
    } catch (err) {
      setError(err.response?.data?.message || err.response?.data || "Trade failed");
    } finally {
      setLoading(false);
    }
  };

  const updatePrice = async () => {
    if (!selected) return;

    setLoading(true);
    setError("");
    setMessage("");

    try {
      const quote = await loadLivePrice(selected, false);
      const marketPrice = Number(quote?.price || tradePrice || selected.referencePrice);
      const response = await updateMarketPriceApi(userId.trim(), {
        symbol: selected.symbol,
        currentPrice: marketPrice,
      });
      setHoldings(response.data);
      loadPortfolio(userId);
      setMessage(`Portfolio marked to ${formatMoney(marketPrice, selected.currency)} for ${selected.symbol}.`);
    } catch (err) {
      setError(err.response?.data?.message || err.response?.data || "Price update failed");
    } finally {
      setLoading(false);
    }
  };

  const depositCash = async (event) => {
    event.preventDefault();

    setLoading(true);
    setError("");
    setMessage("");

    try {
      const response = await depositDemoCashApi(userId.trim(), {
        currency: depositCurrency,
        amount: Number(depositAmount),
      });
      setAccounts(response.data.accounts || []);
      setHoldings(response.data.holdings || []);
      setTrades(response.data.trades || []);
      setDailyProfitLoss(response.data.dailyProfitLoss || []);
      setMessage(`${formatMoney(depositAmount, depositCurrency)} added to buying power.`);
    } catch (err) {
      setError(err.response?.data?.message || err.response?.data || "Could not add demo cash");
    } finally {
      setLoading(false);
    }
  };

  const displayedPrice = liveQuote?.price || tradePrice || selected?.referencePrice;

  return (
    <main className="trading-page">
      <header className="trading-hero">
        <div>
          <p className="eyebrow">MarketQuest paper trading</p>
          <h1>Paper Trading Desk</h1>
          <p>Test buy and sell decisions with demo cash while using the latest available market price.</p>
        </div>
        <div className="header-actions">
          <Link className="secondary-action" to="/dashboard">Dashboard</Link>
          <Link className="secondary-action" to="/watchlist">Watchlist</Link>
        </div>
      </header>

      <section className="trading-summary" aria-label="Portfolio summary">
        {accounts.map((account) => (
          <div className="summary-card cash-card" key={account.currency}>
            <span>{account.currency} buying power</span>
            <strong>{formatMoney(account.cashBalance, account.currency)}</strong>
            <small>Available demo balance</small>
          </div>
        ))}
        {totals.length === 0 && (
          <div>
            <span>Demo portfolio</span>
            <strong>{formatMoney(0)}</strong>
            <small>No holdings yet</small>
          </div>
        )}
        {totals.map((total) => (
          <div className={total.totalProfitLoss >= 0 ? "summary-card profit" : "summary-card loss"} key={total.currency}>
            <span>{total.currency} portfolio</span>
            <strong>{formatMoney(total.totalProfitLoss, total.currency)}</strong>
            <small>
              Invested {formatMoney(total.investedValue, total.currency)} / Current{" "}
              {formatMoney(total.currentValue, total.currency)}
            </small>
          </div>
        ))}
      </section>

      <section className="market-shell">
        <div className="market-workspace">
          <aside className="market-browser">
            <div className="market-toolbar">
              <label className="field-group">
                <span>Search company</span>
                <input
                  value={query}
                  onChange={(event) => setQuery(event.target.value)}
                  placeholder="Search symbol or company"
                />
              </label>
              <div className="market-tabs">
                {markets.map((item) => (
                  <button
                    className={market === item.value ? "tab-button active" : "tab-button"}
                    key={item.value || "all"}
                    type="button"
                    onClick={() => setMarket(item.value)}
                  >
                    {item.label}
                  </button>
                ))}
              </div>
            </div>

            <div className="instrument-list">
              {instruments.map((instrument) => (
                <button
                  className={
                    selected?.symbol === instrument.symbol
                      ? "instrument-card selected"
                      : "instrument-card"
                  }
                  key={instrument.symbol}
                  type="button"
                  onClick={() => selectInstrument(instrument)}
                >
                  <span>
                    <strong>{instrument.symbol}</strong>
                    <small>{instrument.exchange}</small>
                  </span>
                  <span>{instrument.name}</span>
                  <b>{formatMoney(instrument.referencePrice, instrument.currency)}</b>
                </button>
              ))}
            </div>
          </aside>

        </div>

        <section className="trade-ticket">
          <form className="trading-panel deposit-panel" onSubmit={depositCash}>
            <h2>Add Buying Power</h2>
            <div className="deposit-grid">
              <label className="field-group">
                <span>Currency</span>
                <select value={depositCurrency} onChange={(event) => setDepositCurrency(event.target.value)}>
                  <option value="INR">INR</option>
                  <option value="USD">USD</option>
                </select>
              </label>
              <label className="field-group">
                <span>Amount</span>
                <input
                  type="number"
                  min="1"
                  step="0.01"
                  value={depositAmount}
                  onChange={(event) => setDepositAmount(event.target.value)}
                  required
                />
              </label>
            </div>
            <button className="primary-action" type="submit" disabled={loading}>
              Add funds
            </button>
          </form>

          <form className="trading-panel" onSubmit={submitTrade}>
            <h2>Order Ticket</h2>
            {selected && (
              <div className="selected-stock">
                <div>
                  <strong>{selected.symbol}</strong>
                  <span>{selected.name}</span>
                  <small>{selected.exchange} / {selected.currency}</small>
                </div>
                <div className="live-price">
                  <span>{formatMoney(displayedPrice, selected.currency)}</span>
                  <small className={liveQuote?.live ? "quote-live" : "quote-fallback"}>
                    {priceLoading ? "Refreshing" : liveQuote?.live ? "Live quote" : "Reference price"}
                  </small>
                </div>
              </div>
            )}

            <label className="field-group">
              <span>User ID</span>
              <input value={userId} onChange={(event) => setUserId(event.target.value)} required />
            </label>

            <div className="side-toggle">
              <button
                className={side === "BUY" ? "buy active" : "buy"}
                type="button"
                onClick={() => setSide("BUY")}
              >
                Buy
              </button>
              <button
                className={side === "SELL" ? "sell active" : "sell"}
                type="button"
                onClick={() => setSide("SELL")}
              >
                Sell
              </button>
            </div>

            <label className="field-group">
              <span>Quantity</span>
              <input
                type="number"
                min="1"
                value={quantity}
                onChange={(event) => setQuantity(event.target.value)}
                required
              />
            </label>

            <label className="field-group">
              <span>Execution price</span>
              <input type="number" min="0.01" step="0.01" value={tradePrice} readOnly required />
            </label>

            <button className="primary-action" type="submit" disabled={loading || !selected}>
              {side === "BUY" ? "Place buy order" : "Place sell order"}
            </button>
          </form>

        </section>
      </section>

      <section className="price-theater" aria-label="Animated market price board">
        <div className="ticker-ribbon">
          <span>{selected?.symbol || "MARKET"}</span>
          <strong>{selected ? formatMoney(displayedPrice, selected.currency) : "--"}</strong>
          <small className={liveQuote?.live ? "quote-live" : "quote-fallback"}>
            {liveQuote?.live ? "Live" : "Reference"}
          </small>
        </div>

        <div className="pulse-stage">
          <svg viewBox="0 0 360 160" role="img" aria-label="Animated market movement">
            <defs>
              <linearGradient id="pulseStroke" x1="0%" x2="100%" y1="0%" y2="0%">
                <stop offset="0%" stopColor="#7dd3fc" />
                <stop offset="50%" stopColor="#22c55e" />
                <stop offset="100%" stopColor="#fbbf24" />
              </linearGradient>
              <linearGradient id="pulseFill" x1="0%" x2="0%" y1="0%" y2="100%">
                <stop offset="0%" stopColor="rgba(34, 197, 94, 0.35)" />
                <stop offset="100%" stopColor="rgba(34, 197, 94, 0)" />
              </linearGradient>
            </defs>
            <path className="pulse-area" d="M0 130 L30 110 L60 118 L90 88 L120 98 L150 62 L180 70 L210 42 L240 56 L270 30 L300 42 L330 18 L360 26 L360 160 L0 160 Z" />
            <polyline
              className="pulse-line"
              points={marketPulse.map((value, index) => `${index * 32},${138 - value}`).join(" ")}
            />
            {marketPulse.map((value, index) => (
              <circle className="pulse-dot" cx={index * 32} cy={138 - value} r="4" key={index} />
            ))}
          </svg>
        </div>

        <div className="market-metrics">
          <div>
            <span>Signal</span>
            <strong>{selected ? "Active" : "Waiting"}</strong>
          </div>
          <div>
            <span>Refresh</span>
            <strong>15s</strong>
          </div>
          <div>
            <span>Currency</span>
            <strong>{selected?.currency || "--"}</strong>
          </div>
        </div>

        <div className="price-actions">
          <button className="secondary-action" type="button" onClick={() => loadLivePrice()} disabled={priceLoading || !selected}>
            Refresh quote
          </button>
          <button className="secondary-action" type="button" onClick={updatePrice} disabled={loading || !selected}>
            Mark portfolio to quote
          </button>
        </div>
      </section>

      <section className="analytics-panel" aria-label="Profit and loss analytics">
        <div className="analytics-heading">
          <div>
            <p className="eyebrow">Performance</p>
            <h2>Profit and Loss Trend</h2>
            <p>Realized P/L is calculated from completed sell orders and grouped by trading day.</p>
          </div>
          <select value={chartCurrency} onChange={(event) => setChartCurrency(event.target.value)}>
            <option value="INR">INR</option>
            <option value="USD">USD</option>
          </select>
        </div>

        <div className="analytics-grid">
          <div className="chart-card">
            <ProfitLossChart data={dailyProfitLoss} currency={chartCurrency} />
          </div>
          <div className="pl-stat-stack">
            {realizedTotals.length === 0 && (
              <div className="pl-stat">
                <span>Realized P/L</span>
                <strong>{formatMoney(0, chartCurrency)}</strong>
                <small>No closed positions yet</small>
              </div>
            )}
            {realizedTotals.map((item) => (
              <div className={item.realizedProfitLoss >= 0 ? "pl-stat gain" : "pl-stat loss"} key={item.currency}>
                <span>{item.currency} realized P/L</span>
                <strong>{formatMoney(item.realizedProfitLoss, item.currency)}</strong>
                <small>From completed sell orders</small>
              </div>
            ))}
            {totals.map((item) => (
              <div className={item.totalProfitLoss >= 0 ? "pl-stat gain" : "pl-stat loss"} key={`${item.currency}-open`}>
                <span>{item.currency} open P/L</span>
                <strong>{formatMoney(item.totalProfitLoss, item.currency)}</strong>
                <small>Includes current holdings</small>
              </div>
            ))}
          </div>
        </div>
      </section>

      {message && <p className="auth-success">{message}</p>}
      {error && <p className="auth-error">{error}</p>}

      <section className="portfolio-table" aria-label="Portfolio holdings">
        <div className="portfolio-row portfolio-head">
          <span>Symbol</span>
          <span>Qty</span>
          <span>Avg price</span>
          <span>Market price</span>
          <span>Invested</span>
          <span>Current</span>
          <span>P/L</span>
        </div>
        {holdings.length === 0 && <p className="empty-state">No portfolio holdings yet.</p>}
        {holdings.map((holding) => (
          <div className="portfolio-row" key={holding.symbol}>
            <strong>{holding.symbol}</strong>
            <span>{holding.quantity}</span>
            <span>{formatMoney(holding.averagePrice, holding.currency)}</span>
            <span>{formatMoney(holding.currentPrice, holding.currency)}</span>
            <span>{formatMoney(holding.investedValue, holding.currency)}</span>
            <span>{formatMoney(holding.currentValue, holding.currency)}</span>
            <span className={Number(holding.totalProfitLoss) >= 0 ? "profit" : "loss"}>
              {formatMoney(holding.totalProfitLoss, holding.currency)}
            </span>
          </div>
        ))}
      </section>

      <section className="portfolio-table" aria-label="Recent paper orders">
        <div className="trade-row portfolio-head">
          <span>Symbol</span>
          <span>Side</span>
          <span>Qty</span>
          <span>Price</span>
          <span>Status</span>
          <span>Message</span>
        </div>
        {trades.length === 0 && <p className="empty-state">No paper orders yet.</p>}
        {trades.map((trade) => (
          <div className="trade-row" key={trade.tradeId}>
            <strong>{trade.symbol}</strong>
            <span>{trade.side}</span>
            <span>{trade.quantity}</span>
            <span>{formatMoney(trade.price, trade.currency)}</span>
            <span className={trade.status === "EXECUTED" ? "profit" : "loss"}>{trade.status}</span>
            <span>{trade.message}</span>
          </div>
        ))}
      </section>
    </main>
  );
};

export default TradingLab;
