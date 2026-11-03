package ie.bitstep.fractalith.modules.order.internal;

import ie.bitstep.fractalith.modules.order.api.Order;
import ie.bitstep.fractalith.modules.order.api.OrderDetails;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "modules.order", name = "enabled", havingValue = "true")
@Slf4j
class OrderRepository {

    private final JdbcClient jdbc;

    OrderRepository(@Qualifier("orderDataSource") DataSource dataSource) {
        this.jdbc = JdbcClient.create(dataSource);
    }

    void save(Order order, String customerId) {
        try {
            jdbc.sql("INSERT INTO orders.orders (id, customer_id, sku, quantity, status) "
                            + "VALUES (:id, :customerId, :sku, :quantity, :status)")
                    .param("id", order.id())
                    .param("customerId", customerId)
                    .param("sku", order.sku())
                    .param("quantity", order.quantity())
                    .param("status", order.status())
                    .update();
        } catch (DuplicateKeyException e) {
            log.debug("Order {} already recorded", order.id());
        }
    }

    void updateStatus(String orderId, String status) {
        int rows = jdbc.sql("UPDATE orders.orders SET status = :status WHERE id = :orderId")
                .param("status", status)
                .param("orderId", orderId)
                .update();
        log.debug("Updated order {} status to {} (rows={})", orderId, status, rows);
    }

    OrderDetails findById(String orderId) {
        return jdbc.sql("SELECT id, customer_id, sku, quantity, status FROM orders.orders WHERE id = :orderId")
                .param("orderId", orderId)
                .query((rs, rowNum) -> new OrderDetails(
                        rs.getString("id"),
                        rs.getString("customer_id"),
                        rs.getString("sku"),
                        rs.getInt("quantity"),
                        rs.getString("status")))
                .single();
    }
}
