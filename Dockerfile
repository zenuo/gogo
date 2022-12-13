FROM rust:alpine as server_builder
WORKDIR /opt/
COPY gogo-server .
RUN apk add --no-cache musl-dev openssl-dev && cargo build -rv

FROM node:18-alpine as web_builder
WORKDIR /opt/
COPY gogo-web .
RUN npm install && npm run build --omit=dev

FROM alpine:3.16
WORKDIR /opt/
RUN apk upgrade --no-cache && apk add --no-cache openssl libgcc
COPY --from=server_builder /opt/target/release/gogo-server .
COPY --from=server_builder /opt/config.json .
COPY --from=web_builder /opt/dist/gogo-web .
ENTRYPOINT ["/opt/gogo-server","config.json"]
