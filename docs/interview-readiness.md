# Review and interview preparation

## Verdict

This is a useful **learning-level backend/full-stack interview project**, provided you can run and explain the working flows. Present it as an event-driven paper-trading prototype. It is not a production brokerage, a completed eleven-service platform, or a working AI adviser.

An interviewer's main interest will be your reasoning: where data lives, how identity is enforced, why an asynchronous order can be rejected, and what happens when infrastructure fails. More empty service folders do not make the project stronger.

## Issues found and changes made

| Finding | Change |
|---|---|
| Watchlist returned every user's rows and allowed edits by any ID | JWT validation; owner-filtered reads/writes; unique owner/symbol constraint |
| Browser could choose `user1` or another portfolio | Authenticated subject supplies trade identity; portfolio ownership checked server-side |
| JWT signing secret embedded in source | Shared private `JWT_SECRET` loaded from local environment; signature/expiry validation |
| Trading accepted browser currency | Currency and price resolved on the server |
| API announced execution before cash/holdings validation | HTTP 202/PENDING after broker acknowledgement; frontend checks settlement status |
| Producers ignored asynchronous send failures | Bounded wait for broker acknowledgement with service-unavailable response on failure |
| Concurrent portfolio updates could overwrite balances | Optimistic version columns; reject mismatched holding currency |
| Realized P/L used only the latest 20 trades | Frontend totals all daily realized P/L records |
| Stale quote response could replace a different selected symbol | Ignore responses for an instrument that is no longer selected |
| Mobile-only signup had no SMS delivery | Email required and clearly described in signup |
| Uploaded filename came from the client | Random server filename, decoded JPEG/PNG validation, optional image, size restriction |
| Predictable OTP generator and unlimited sequential attempts | SecureRandom; five-attempt limit and 60-second resend cooldown |
| Startup could continue after Docker failure | Prerequisite/broker checks and accurate launch messages |
| Root README empty; CI required local-style database config | Setup/architecture/demo docs, isolated test configuration, frontend CI |

The pre-existing edits in the working tree were preserved and incorporated. Existing uploads and database records were not deleted.

## A five-minute demo

### Review verification (2026-09-11)

- `mvn test`: auth 5, watchlist 3, trading 2, portfolio 4, notification 1; **15 tests passed**.
- `frontendr`: `npm run lint` and `npm run build` passed.
- `scripts/start-dev.ps1` and `scripts/stop-dev.ps1`: PowerShell syntax parsing passed. Their full start/stop lifecycle was not executed during this review.
- `git diff --check` passed.
- A live browser/MySQL/Kafka/SMTP end-to-end run was not performed. Rehearse the following steps before presenting.

Restart the backend services after these changes and log in again: the signing key changed from the old source-code constant to a private value in `.env`. Existing tokens will be rejected. Local database connection values were preserved in `.env`.

1. Start MySQL, Docker, the backend script and `frontendr`. Verify each service log before the interview.
2. Register using an email you control, receive an OTP, verify it, then log in. Show that an expired/incorrect code fails.
3. Add a watchlist item and note. Log in as a second user and show that the first user's watchlist is invisible.
4. Place a small AAPL buy. Explain its USD reference/provider quote, PENDING acknowledgement and final settlement status.
5. Attempt to sell more shares than you own. Show the rejected history entry and unchanged holdings.
6. Mark a demo holding to a quote, then explain unrealized versus realized P/L. Do not describe the decorative animation as a market chart.
7. Show one ownership test and one settlement test. Explain what they prove and what an H2 test cannot prove about MySQL or Kafka.

Use accounts you control and avoid displaying `.env`, OTP values, uploaded personal documents or real credentials during screen sharing. Older commits already contained a sample database password and profile uploads; ignoring future uploads does not remove tracked files or repository history. Review those before publishing.

## Questions to prepare

- **Why Kafka?** Email delivery and order settlement can run independently from the HTTP request. The cost is eventual consistency, retry/duplicate handling and more operational dependencies.
- **Why is an order pending?** Kafka acknowledgement confirms the broker accepted a message, not that portfolio cash/holdings checks passed.
- **What prevents an oversell?** Portfolio checks quantity inside settlement; version checks protect against conflicting updates. Describe concurrency limits honestly.
- **What prevents duplicate settlement?** Recorded trade IDs make replay of a completed event a no-op. This is not an end-to-end exactly-once guarantee.
- **Why BigDecimal?** Currency arithmetic and weighted-average cost need controlled decimal precision rather than binary floating-point behavior.
- **Why JWT checks in each service?** Frontend route guards are only UI behavior. A caller can bypass the browser and send HTTP requests directly.
- **What would you build next?** A durable order record/outbox, request idempotency keys, explicit failed-message handling, migrations, and a complete integration test.

## Remaining limitations

1. **No AI implementation or gateway routing.** Those modules are scaffolds. Keep them clearly labelled as planned.
2. **No atomic database-to-Kafka outbox.** Signup can save a user before event publication fails. A timed-out send can still have reached Kafka. Orders have no durable PENDING ledger or client idempotency key, so blindly resubmitting can create another order.
3. **Retry recovery needs work.** No configured dead-letter workflow, operational alerts, or expired-OTP filtering at delivery. The five-attempt OTP guard is not a distributed rate limiter; concurrent requests and account/IP throttling need further design. OTPs remain stored in plaintext in the database.
4. **Authentication is demo scope.** No refresh tokens, password-reset-by-email flow or immediate revocation of old tokens after password change. Browser localStorage and a shared symmetric signing secret are explicit tradeoffs. Profile images remain public URLs.
5. **Market simulation is limited.** Fixed reference-price fallback, small catalogue, no fees/slippage/order book/market-hours enforcement, and unrestricted virtual-cash deposits. Manual marks affect only your own demo valuation; they are not trusted market data.
6. **Production data management is incomplete.** `ddl-auto=update` instead of migrations; existing shared development schemas; first-account creation can conflict under concurrent requests. Optimistic conflicts need a deliberate retry policy for every path.
7. **Verification is not complete end-to-end certification.** Automated tests use H2 and mock/disabled message delivery. Perform the live MySQL + Kafka + SMTP + browser demo above on the exact machine you will use.

Spring JWT validation follows the [Spring Security resource server documentation](https://docs.spring.io/spring-security/reference/6.5/servlet/oauth2/resource-server/jwt.html). The repository retains its existing Spring Boot versions; this review was about behavior, not a dependency upgrade.
