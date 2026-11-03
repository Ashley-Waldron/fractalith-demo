package ie.bitstep.fractalith.modules.inventory.internal;

import com.zaxxer.hikari.HikariDataSource;
import ie.bitstep.fractalith.common.db.ModuleDatabase;
import ie.bitstep.fractalith.common.db.SwitchableDataSource;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "modules.inventory", name = "enabled", havingValue = "true")
class InventoryDataSourceConfig {

    @Bean(name = "inventoryDataSource", destroyMethod = "close")
    SwitchableDataSource inventoryDataSource(
            @Value("${modules.inventory.datasource.url}") String url,
            @Value("${modules.inventory.datasource.username}") String username,
            @Value("${modules.inventory.datasource.password}") String password) {
        HikariDataSource pool = ModuleDatabase.pool("inventory", url, username, password);
        ModuleDatabase.migrate(pool, "inventory");
        return new SwitchableDataSource(pool, Duration.ofSeconds(30));
    }
}