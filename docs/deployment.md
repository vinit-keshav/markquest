# Deployment preparation

## VPS Compose file

`backend/docker-compose.deploy.yml` runs the six published images from `ghcr.io/vinit-keshav/markquest`, plus MySQL, Kafka and Zookeeper. It uses the supplied release SHA by default; it does not rebuild application images. `backend/docker-compose.yml` remains the local Kafka/Zookeeper setup.

On the VPS, from the `backend` directory:

```bash
cp .env.deploy.example .env.deploy
chmod 600 .env.deploy
nano .env.deploy
docker compose --env-file .env.deploy -f docker-compose.deploy.yml config --quiet
docker compose --env-file .env.deploy -f docker-compose.deploy.yml pull
docker compose --env-file .env.deploy -f docker-compose.deploy.yml up -d
docker compose --env-file .env.deploy -f docker-compose.deploy.yml ps
docker compose --env-file .env.deploy -f docker-compose.deploy.yml logs --tail=100
```

Fill every empty required setting with private values. Use distinct strong database passwords and a random JWT secret of at least 32 bytes. Gmail requires an app password for SMTP. This demo uses one fresh shared MySQL schema; existing laptop data is not imported. MySQL initialization settings apply only to a new data volume: changing the password in the env file later does not change existing database accounts.

Application processes starting is not proof of readiness; verify logs and rehearse signup, email delivery and settlement. Auth uploads use a named volume, initialized from the image's writable uploads directory. Do not run `down -v`: that deletes the database, broker data and profile images. Back up data before upgrading; changing IMAGE_TAG and rerunning pull/up replaces code, not schema changes already applied by Hibernate.

The frontend listens only on `127.0.0.1:8080` on the VPS. Configure an HTTPS reverse proxy to this address before public use. For private testing, use an SSH tunnel (`ssh -L 8080:127.0.0.1:8080 deploy@SERVER_IP`) and temporarily set APP_ORIGIN to `http://localhost:8080`. No database, broker or individual backend ports are published. HTTPS and automatic CD rollout still need server/domain configuration.

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
