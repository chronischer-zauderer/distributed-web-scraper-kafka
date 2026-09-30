FROM node:22-alpine AS deps
WORKDIR /app
COPY package.json pnpm-lock.yaml pnpm-workspace.yaml ./
RUN corepack enable && pnpm install --frozen-lockfile

FROM node:22-alpine AS builder
WORKDIR /app
ARG NEXT_PUBLIC_DASHBOARD_API_URL=http://localhost:8082
ENV NEXT_PUBLIC_DASHBOARD_API_URL=$NEXT_PUBLIC_DASHBOARD_API_URL
COPY --from=deps /app/node_modules ./node_modules
COPY . .
RUN corepack enable && pnpm build

FROM node:22-alpine AS runner
WORKDIR /app
ENV NODE_ENV=production
COPY --from=builder /app ./
EXPOSE 3000
CMD ["corepack", "pnpm", "start"]
