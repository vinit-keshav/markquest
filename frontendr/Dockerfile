FROM node:24-alpine AS build
WORKDIR /build
COPY package*.json ./
RUN npm ci
COPY . .
ENV VITE_API_BASE_URL=/api
ENV VITE_WATCHLIST_API_BASE_URL=/api
ENV VITE_TRADING_API_BASE_URL=/api
ENV VITE_PORTFOLIO_API_BASE_URL=/api
RUN npm test && npm run lint && npm run build

FROM nginx:stable-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /build/dist /usr/share/nginx/html
EXPOSE 80
