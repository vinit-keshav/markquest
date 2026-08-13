import { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { Link } from "react-router-dom";
import {
  addWatchlistStock,
  fetchWatchlist,
  updateWatchlistNote,
} from "../features/watchlist/watchListSlice";

const emptyStock = { symbol: "", companyName: "" };

const Watchlist = () => {
  const dispatch = useDispatch();
  const { items, loading, error } = useSelector((state) => state.watchlist);
  const [stock, setStock] = useState(emptyStock);
  const [draftNotes, setDraftNotes] = useState({});

  useEffect(() => {
    dispatch(fetchWatchlist());
  }, [dispatch]);

  const handleChange = (event) => {
    const { name, value } = event.target;
    setStock((current) => ({ ...current, [name]: value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    const result = await dispatch(
      addWatchlistStock({
        symbol: stock.symbol.trim().toUpperCase(),
        companyName: stock.companyName.trim(),
      }),
    );
    if (addWatchlistStock.fulfilled.match(result)) setStock(emptyStock);
  };

  const saveNote = (id) => {
    dispatch(updateWatchlistNote({ id, note: draftNotes[id]?.trim() || "" }));
  };

  return (
    <main className="watchlist-page">
      <header className="watchlist-header">
        <div>
          <p className="eyebrow">MarketQuest AI</p>
          <h1>Research Watchlist</h1>
          <p>Organize companies you want to follow and keep concise investment notes.</p>
        </div>
        <Link className="secondary-action" to="/dashboard">Dashboard</Link>
      </header>

      <form className="watchlist-form" onSubmit={handleSubmit}>
        <label className="field-group">
          <span>Symbol</span>
          <input
            name="symbol"
            value={stock.symbol}
            onChange={handleChange}
            placeholder="AAPL"
            required
          />
        </label>
        <label className="field-group">
          <span>Company name</span>
          <input
            name="companyName"
            value={stock.companyName}
            onChange={handleChange}
            placeholder="Apple Inc."
            required
          />
        </label>
        <button className="primary-action" type="submit" disabled={loading}>
          Add to watchlist
        </button>
      </form>

      {error && <p className="auth-error">{error}</p>}
      {loading && items.length === 0 && <p>Loading watchlist...</p>}
      {!loading && items.length === 0 && <p className="empty-state">Your watchlist is empty.</p>}

      <section className="watchlist-grid" aria-label="Watched stocks">
        {items.map((item) => (
          <article className="watchlist-card" key={item.id}>
            <div className="stock-heading">
              <strong>{item.symbol}</strong>
              <span>{item.companyName}</span>
            </div>
            <label className="field-group">
              <span>Research note</span>
              <textarea
                value={draftNotes[item.id] ?? item.note ?? ""}
                onChange={(event) =>
                  setDraftNotes((current) => ({
                    ...current,
                    [item.id]: event.target.value,
                  }))
                }
                placeholder="Key thesis, trigger, or risk"
                rows={3}
              />
            </label>
            <button
              className="secondary-action"
              type="button"
              onClick={() => saveNote(item.id)}
              disabled={loading}
            >
              Save note
            </button>
          </article>
        ))}
      </section>
    </main>
  );
};

export default Watchlist;
