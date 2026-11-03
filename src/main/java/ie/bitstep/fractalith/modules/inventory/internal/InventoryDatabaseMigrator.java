package ie.bitstep.fractalith.modules.inventory.internal;

import com.zaxxer.hikari.HikariDataSource;
import ie.bitstep.fractalith.common.db.ModuleDatabase;
import ie.bitstep.fractalith.common.db.SwitchableDataSource;
import java.time.Duration;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Moves the inventory schema to another database while the service keeps running.
 * The data itself is copied by Postgres logical replication; this class only creates the
 * target tables, pauses connections around the swap and points the pool at the new database.
 */
@Component
@ConditionalOnProperty(prefix = "modules.inventory", name = "enabled", havingValue = "true")
@ConditionalOnProperty(prefix = "modules.inventory", name = "inbound", havingValue = "http")
@Slf4j
class InventoryDatabaseMigrator {

    private static final Duration DRAIN_TIMEOUT = Duration.ofSeconds(10);

    private final SwitchableDataSource dataSource;
    private final String targetUrl;
    private final String username;
    private final String password;

    InventoryDatabaseMigrator(
            @Qualifier("inventoryDataSource") SwitchableDataSource dataSource,
            @Value("${modules.inventory.datasource.migration-target-url:}") String targetUrl,
            @Value("${modules.inventory.datasource.username}") String username,
            @Value("${modules.inventory.datasource.password}") String password) {
        this.dataSource = dataSource;
        this.targetUrl = targetUrl;
        this.username = username;
        this.password = password;
    }

    Map<String, Object> status() {
        return Map.of(
                "currentUrl", dataSource.currentUrl(),
                "targetUrl", targetUrl,
                "frozen", dataSource.isFrozen());
    }

    /** Creates the inventory tables in the target database so a subscription can fill them. */
    void prepareTarget() {
        requireTarget();
        try (HikariDataSource target = ModuleDatabase.pool("inventory-prepare", targetUrl, username, password)) {
            ModuleDatabase.migrate(target, "inventory");
        }
        log.info("Prepared inventory schema in {}", targetUrl);
    }

    void freeze(Duration autoResumeAfter) {
        requireTarget();
        dataSource.freeze(DRAIN_TIMEOUT, autoResumeAfter);
        log.warn("Inventory datasource frozen; new connections are waiting");
    }

    void cutover() {
        requireTarget();
        if (!dataSource.isFrozen()) {
            throw new IllegalStateException("Freeze the datasource before cutting over");
        }
        HikariDataSource target;
        try {
            target = ModuleDatabase.pool("inventory", targetUrl, username, password);
            ModuleDatabase.migrate(target, "inventory");
        } catch (RuntimeException e) {
            dataSource.resume();
            throw e;
        }
        dataSource.swapAndResume(target);
        log.warn("Inventory datasource switched to {}", targetUrl);
    }

    void abort() {
        dataSource.resume();
        log.warn("Inventory datasource resumed on {}", dataSource.currentUrl());
    }

    private void requireTarget() {
        if (targetUrl.isBlank()) {
            throw new IllegalStateException("modules.inventory.datasource.migration-target-url is not configured");
        }
    }
}