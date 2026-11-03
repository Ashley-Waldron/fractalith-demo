package ie.bitstep.fractalith.modules.search.internal;

import ie.bitstep.fractalith.modules.search.api.ProductDocument;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "modules.search", name = "enabled", havingValue = "true")
@Slf4j
class SearchRepository {

    private final JdbcClient jdbc;

    SearchRepository(@Qualifier("searchDataSource") DataSource dataSource) {
        this.jdbc = JdbcClient.create(dataSource);
    }

    void index(String orderId, ProductDocument document) {
        try {
            jdbc.sql("INSERT INTO search.documents (order_id, sku, name) VALUES (:orderId, :sku, :name)")
                    .param("orderId", orderId)
                    .param("sku", document.sku())
                    .param("name", document.name())
                    .update();
        } catch (DuplicateKeyException e) {
            log.debug("Order {} already indexed", orderId);
        }
    }
}
