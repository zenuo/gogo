FROM rust:alpine as server_builder
WORKDIR /opt/
COPY gogo-server .
RUN apk add --no-cache openssl-dev && cargo build -rv

FROM node:18-alpine as web_builder
WORKDIR /opt/
COPY gogo-web .
RUN npm install && ng build

FROM alpine
WORKDIR /opt/
COPY --from=server_builder /opt/target/release/gogo-server gogo-server \
    /opt/config.json config.json 
COPY --from=web_builder /opt/dist/gogo-web gogo-web
ENTRYPOINT ["gogo-server","config.json"]
