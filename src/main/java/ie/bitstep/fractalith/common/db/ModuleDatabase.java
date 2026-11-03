package ie.bitstep.fractalith.common.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;

/**
 * Helpers for giving each module its own connection pool and its own schema.
 * Migrations live in {@code db/migration/<schema>} so a schema can be moved as a unit.
 */
public final class ModuleDatabase {

    private ModuleDatabase() {
    }

    public static HikariDataSource pool(String poolName, String url, String username, String password) {
        HikariConfig config = new HikariConfig();
        config.setPoolName(poolName);
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(10_000);
        return new HikariDataSource(config);
    }

    public static void migrate(DataSource dataSource, String schema) {
        Flyway.configure()
                .dataSource(dataSource)
                .schemas(schema)
                .locations("classpath:db/migration/" + schema)
                .load()
                .migrate();
    }
}
