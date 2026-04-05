package io.github.ilkka_n.hotswap.adapters.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * Luo PostgreSQL-yhteyden JdbcTemplatelle ilman DataSource-beania.
 * DriverManagerDataSource ei poolaa eikä validoi yhteyttä käynnistyksessä,
 * joten sovellus käynnistyy normaalisti vaikka PostgreSQL ei ole käynnissä.
 */
@Configuration
class PostgresSQLConfig {

    @Bean("postgresJdbcTemplate")
    JdbcTemplate postgresJdbcTemplate(
            @Value("${hotswap.postgres.url:jdbc:postgresql://localhost:5432/hotswap}") String url,
            @Value("${hotswap.postgres.username:postgres}") String username,
            @Value("${hotswap.postgres.password:postgres}") String password) {
        var ds = new DriverManagerDataSource();
        ds.setDriverClassName("org.postgresql.Driver");
        ds.setUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);
        return new JdbcTemplate(ds);
    }
}
