package ie.bitstep.fractalith.modules.shop.internal;

import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "modules.shop", name = "enabled", havingValue = "true")
@Slf4j
class CheckoutRepository {

    private final JdbcClient jdbc;

    CheckoutRepository(@Qualifier("shopDataSource") DataSource dataSource) {
        this.jdbc = JdbcClient.create(dataSource);
    }

    void save(String id, String customerId, String sku, int quantity, String status) {
        try {
            jdbc.sql("INSERT INTO shop.checkouts (id, customer_id, sku, quantity, status) "
                            + "VALUES (:id, :customerId, :sku, :quantity, :status)")
                    .param("id", id)
                    .param("customerId", customerId)
                    .param("sku", sku)
                    .param("quantity", quantity)
                    .param("status", status)
                    .update();
        } catch (DuplicateKeyException e) {
            log.debug("Checkout {} already recorded", id);
        }
    }
}
