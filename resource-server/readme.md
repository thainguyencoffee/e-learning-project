# Dev guide

## Run docker compose
```bash
cd ../deployment/docker/ && docker compose down && docker compose up -d
```

## Run reverse proxy
```bash
cd ../reverse-proxy/ && ./gradlew bootRun
```

## Run backend for frontend
```bash
cd ../bff/ && ./gradlew bootRun
```

## Down docker compose
```bash
cd ../deployment/docker/ && docker compose down
```