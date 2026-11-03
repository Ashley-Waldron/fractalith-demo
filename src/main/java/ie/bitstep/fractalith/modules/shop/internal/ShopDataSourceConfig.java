package ie.bitstep.fractalith.modules.shop.internal;

import com.zaxxer.hikari.HikariDataSource;
import ie.bitstep.fractalith.common.db.ModuleDatabase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "modules.shop", name = "enabled", havingValue = "true")
class ShopDataSourceConfig {

    @Bean(name = "shopDataSource", destroyMethod = "close")
    HikariDataSource shopDataSource(
            @Value("${modules.shop.datasource.url}") String url,
            @Value("${modules.shop.datasource.username}") String username,
            @Value("${modules.shop.datasource.password}") String password) {
        HikariDataSource dataSource = ModuleDatabase.pool("shop", url, username, password);
        ModuleDatabase.migrate(dataSource, "shop");
        return dataSource;
    }
}
