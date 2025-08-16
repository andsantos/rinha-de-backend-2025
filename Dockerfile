FROM alpine:3.20

RUN apk add --no-cache libstdc++ libc6-compat

WORKDIR /app

COPY target/rinhabackend2025 /app/rinhabackend2025

RUN chmod +x /app/rinhabackend2025

EXPOSE 8080

ENTRYPOINT ["/app/rinhabackend2025"]
