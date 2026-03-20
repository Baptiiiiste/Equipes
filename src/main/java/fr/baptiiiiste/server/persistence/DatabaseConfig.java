package fr.baptiiiiste.server.persistence;

import lombok.Getter;
import org.flywaydb.core.Flyway;

/**
 * Reads DB settings from environment variables so local Docker and CI can share the same code path.
 */
@Getter
public class DatabaseConfig {

    private final String jdbcUrl;
    private final String username;
    private final String password;

    public DatabaseConfig(String jdbcUrl, String username, String password) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
    }

    public static DatabaseConfig fromEnvironment() {
        String jdbcUrl = readOrDefault("APP_DB_URL", "jdbc:postgresql://localhost:5432/equipes");
        String username = readOrDefault("APP_DB_USER", "equipes");
        String password = readOrDefault("APP_DB_PASSWORD", "equipes");

        return new DatabaseConfig(jdbcUrl, username, password);
    }

    private static String readOrDefault(String envName, String fallbackValue) {
        String value = System.getenv(envName);
        if (value == null || value.isBlank()) {
            return fallbackValue;
        }
        return value;
    }

    public void migrate() {
        Flyway.configure()
                .dataSource(jdbcUrl, username, password)
                .load()
                .migrate();
    }

}

