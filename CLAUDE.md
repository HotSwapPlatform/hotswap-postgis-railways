# hotswap-postgis-railways — projektikonteksti

## Tarkoitus

Rautatieasemasovellus. Hakee Suomen rautatieasemien sijaintitiedot Digitraffic-API:sta, tallentaa PostGIS-tietokantaan ja näyttää ne Leaflet-kartalla.

Aineisto: Digitraffic / Fintraffic (CC 4.0 BY) — mainittava lähdekoodissa ja dokumentaatiossa.

---

## Tekninen stack

- Java 21, Spring Boot 4.0.3
- Spring JDBC + JdbcTemplate (ei JPA)
- PostgreSQL + PostGIS
- Thymeleaf + Leaflet.js (karttanäkymä)
- JUnit 5 + Mockito + MockMvc

---

## Rakenne

```
adapters/
├── config/
│   ├── PostgresSQLConfig   — tietokantayhteys (@Value-annotaatiot, oletukset postgres/postgres)
│   └── CorsConfig
└── stations/
    ├── DigitrafficClient           — REST-client Digitraffic-API:in
    ├── StationLoadService          — asemien latauslogiikka
    ├── PostgreSQLStationRepository — tallennus PostGIS-kantaan
    ├── RestStationController       — REST-endpointit
    └── StationMapViewController    — Thymeleaf-karttanäkymä
```

## REST-endpointit (portti 8082)

- `GET  /api/stations` — palauttaa asemat
- `POST /api/stations` — lataa asemat Digitrafficilta
- `DELETE /api/stations` — tyhjentää kannan
- `GET  /` — karttanäkymä

---

## Paikallinen kehitys

```bash
docker-compose up -d   # käynnistää PostgreSQL + PostGIS
./mvnw spring-boot:run
```

Tietokantayhteys: `postgres/postgres` (oletukset) — `.env.example` puuttuu vielä.

---

## Testien tila

- 13 testiä, kaikki vihreitä
- MockMvc + Mockito — ei oikeaa kantaa testeissä
- JaCoCo koodikattavuus käytössä (43% — `PostgreSQLStationRepository` ja `DigitrafficClient` testaamatta)

## Tärkeää

- `DataSourceAutoConfiguration` ja `JdbcTemplateAutoConfiguration` excludettu sovelluksesta
- Tietokantayhteys konfiguroidaan manuaalisesti `PostgresSQLConfig`:ssa

---

Työskentelyperiaatteet ja commit-käytäntö: katso `../hotswap-framework/CLAUDE.md`.
