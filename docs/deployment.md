# Deployment preparation

The container build definitions and manual **Build release images** workflow prepare releases for the five implemented backend services and `frontendr`. They do not yet deploy to a running host.

Each backend image runs `mvn verify`; the frontend image runs tests, lint and build. An unsuccessful build is not published. Images are tagged with their source commit SHA in GitHub Container Registry. All six jobs must succeed before treating the commit as a complete release. Do not use a partial release.

## Required hosting decision

Choose a hosting provider/server and budget before configuring deployment credentials, HTTPS, storage and automatic rollout. A release image is not a running deployment. No server was provisioned and no images were published during preparation.

## Runtime contract

- Run Linux containers with network names `auth-service`, `watchlist-service`, `trading-service`, `portfolio-service`, and `notification-service`.
- The frontend Nginx image proxies `/api` to those services. All Vite API URLs are built as `/api`, including profile-image URLs. Only the frontend should be exposed through the host's HTTPS ingress.
- Configure `CORS_ALLOWED_ORIGINS` on the four HTTP services to the exact public origin, for example `https://marketquest.example.com`, without a trailing slash. Local defaults remain unchanged.
- Supply private `JWT_SECRET`, the `AUTH_DB_*`, `WATCHLIST_DB_*`, `PORTFOLIO_DB_*` database settings, and notification SMTP settings from server-side secrets. Do not upload the development `.env` into images.
- Set `SPRING_KAFKA_BOOTSTRAP_SERVERS` to a reachable broker address. Container `localhost` refers to that individual container, not the host or another service. Kafka advertised listeners must also be reachable from the backend network.
- Persist MySQL data, Kafka data and `/app/uploads` for auth. Ensure the uploads volume is writable by the image's `marketquest` user. Existing development data is not automatically migrated.
- Terminate HTTPS at the hosting ingress/reverse proxy before exposing authentication publicly. Set forwarded headers consistently for the chosen ingress.
- Set `SPRING_JPA_SHOW_SQL=false` outside local development. The existing Hibernate schema-update strategy still requires a migration/backup plan before production use.

After hosting is selected, add the deployment job with host credentials, health/readiness checks, persistent volumes and an explicit rollback process. The release workflow is manual to avoid activating an incomplete deployment automatically on push.

References: [GitHub image publishing](https://docs.github.com/en/actions/tutorials/publish-packages/publish-docker-images) and [Docker Compose deployment](https://docs.docker.com/compose/how-tos/production/).
