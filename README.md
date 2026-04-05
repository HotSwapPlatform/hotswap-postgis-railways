# hotswap-postgis-railways

Spring Boot demo application displaying Finnish railway station locations on an interactive map. Station data is fetched from the [Digitraffic API](https://www.digitraffic.fi/) (Fintraffic, CC 4.0 BY) and stored in a PostGIS-enabled PostgreSQL database.

## Features

- REST client fetching station data from Digitraffic open API
- PostgreSQL + PostGIS for geospatial storage
- Leaflet.js interactive map via Thymeleaf template
- Manual JdbcTemplate usage (no Spring Data JPA)

## Requirements

- Java 21
- PostgreSQL with PostGIS extension (use docker-compose)

## Run locally

```bash
docker-compose up -d
./mvnw spring-boot:run
```

Open http://localhost:8080

## Build & test

```bash
./mvnw test
```

## Part of HotSwapPlatform

- [hotswap-framework](https://github.com/HotSwapPlatform/hotswap-framework) — ports and adapters library
- [hotswap-finance-ai](https://github.com/HotSwapPlatform/hotswap-finance-ai) — Finance AI demo
- [hotswap-platform-ui](https://github.com/HotSwapPlatform/hotswap-platform-ui) — React control panel
