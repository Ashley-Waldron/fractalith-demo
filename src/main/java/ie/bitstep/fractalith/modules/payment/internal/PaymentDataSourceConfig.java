package ie.bitstep.fractalith.modules.payment.internal;

import com.zaxxer.hikari.HikariDataSource;
import ie.bitstep.fractalith.common.db.ModuleDatabase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "modules.payment", name = "enabled", havingValue = "true")
class PaymentDataSourceConfig {

    @Bean(name = "paymentDataSource", destroyMethod = "close")
    HikariDataSource paymentDataSource(
            @Value("${modules.payment.datasource.url}") String url,
            @Value("${modules.payment.datasource.username}") String username,
            @Value("${modules.payment.datasource.password}") String password) {
        HikariDataSource dataSource = ModuleDatabase.pool("payment", url, username, password);
        ModuleDatabase.migrate(dataSource, "payments");
        return dataSource;
    }
}
