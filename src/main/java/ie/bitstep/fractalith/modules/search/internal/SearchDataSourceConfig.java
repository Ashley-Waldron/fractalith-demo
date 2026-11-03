package ie.bitstep.fractalith.modules.search.internal;

import com.zaxxer.hikari.HikariDataSource;
import ie.bitstep.fractalith.common.db.ModuleDatabase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "modules.search", name = "enabled", havingValue = "true")
class SearchDataSourceConfig {

    @Bean(name = "searchDataSource", destroyMethod = "close")
    HikariDataSource searchDataSource(
            @Value("${modules.search.datasource.url}") String url,
            @Value("${modules.search.datasource.username}") String username,
            @Value("${modules.search.datasource.password}") String password) {
        HikariDataSource dataSource = ModuleDatabase.pool("search", url, username, password);
        ModuleDatabase.migrate(dataSource, "search");
        return dataSource;
    }
}
