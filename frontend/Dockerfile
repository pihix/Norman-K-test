# ---------- Build ----------
FROM node:20-alpine AS builder

WORKDIR /app


COPY package*.json ./
COPY pnpm-lock.yaml ./
COPY .npmrc .npmrc

RUN npm install -g pnpm && pnpm install

COPY . .

RUN pnpm build

# ---------- Runtime ----------
FROM node:20-alpine AS runner

WORKDIR /app


COPY --from=builder /app/package.json ./
COPY --from=builder /app/.next ./.next
COPY --from=builder /app/public ./public
COPY --from=builder /app/node_modules ./node_modules
COPY --from=builder /app/next.config.js ./
COPY --from=builder /app/.env ./

ENV NODE_ENV=production
EXPOSE 3000

CMD ["node_modules/.bin/next", "start"]
