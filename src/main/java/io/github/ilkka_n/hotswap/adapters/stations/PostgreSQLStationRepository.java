package io.github.ilkka_n.hotswap.adapters.stations;

import io.github.ilkka_n.hotswap.core.domain.Station;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
class PostgreSQLStationRepository {

    private final JdbcTemplate jdbcTemplate;

    PostgreSQLStationRepository(@Qualifier("postgresJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    void createTableIfNotExists() {
        try {
            jdbcTemplate.execute("""
                    CREATE TABLE IF NOT EXISTS stations (
                        short_code  VARCHAR(10)  PRIMARY KEY,
                        name        VARCHAR(100) NOT NULL,
                        latitude    DOUBLE PRECISION NOT NULL,
                        longitude   DOUBLE PRECISION NOT NULL,
                        source      VARCHAR(50)  NOT NULL DEFAULT ''
                    )""");
            jdbcTemplate.execute("""
                    ALTER TABLE stations ADD COLUMN IF NOT EXISTS source VARCHAR(50) NOT NULL DEFAULT ''
                    """);
            log.info("PostgreSQL stations-taulu valmis");
        } catch (DataAccessException e) {
            log.warn("PostgreSQL ei saatavilla käynnistyksessä, stations-taulua ei luotu: {}", e.getMessage());
        }
    }

    void saveAll(List<Station> stations) {
        stations.forEach(s -> jdbcTemplate.update("""
                INSERT INTO stations (short_code, name, latitude, longitude, source)
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT (short_code) DO UPDATE
                    SET name = EXCLUDED.name,
                        latitude = EXCLUDED.latitude,
                        longitude = EXCLUDED.longitude,
                        source = EXCLUDED.source
                """, s.shortCode(), s.name(), s.latitude(), s.longitude(), s.source()));
    }

    void deleteBySource(String source) {
        jdbcTemplate.update("DELETE FROM stations WHERE source = ?", source);
    }

    List<Station> findAll() {
        return jdbcTemplate.query(
                "SELECT short_code, name, latitude, longitude, source FROM stations ORDER BY name",
                (rs, rowNum) -> new Station(
                        rs.getString("short_code"),
                        rs.getString("name"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude"),
                        rs.getString("source")));
    }

    long count() {
        Long result = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM stations", Long.class);
        return result != null ? result : 0L;
    }

    void deleteAll() {
        jdbcTemplate.execute("DELETE FROM stations");
    }
}
