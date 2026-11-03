package ie.bitstep.fractalith.modules.order.internal;

import com.zaxxer.hikari.HikariDataSource;
import ie.bitstep.fractalith.common.db.ModuleDatabase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "modules.order", name = "enabled", havingValue = "true")
class OrderDataSourceConfig {

    @Bean(name = "orderDataSource", destroyMethod = "close")
    HikariDataSource orderDataSource(
            @Value("${modules.order.datasource.url}") String url,
            @Value("${modules.order.datasource.username}") String username,
            @Value("${modules.order.datasource.password}") String password) {
        HikariDataSource dataSource = ModuleDatabase.pool("order", url, username, password);
        ModuleDatabase.migrate(dataSource, "orders");
        return dataSource;
    }
}
