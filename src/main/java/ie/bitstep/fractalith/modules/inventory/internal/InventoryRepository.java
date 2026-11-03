package ie.bitstep.fractalith.modules.inventory.internal;

import ie.bitstep.fractalith.modules.inventory.api.InventoryRequest;
import ie.bitstep.fractalith.modules.inventory.api.NotInStockException;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Component
@ConditionalOnProperty(prefix = "modules.inventory", name = "enabled", havingValue = "true")
class InventoryRepository {

    private final JdbcClient jdbc;
    private final TransactionTemplate transactions;

    InventoryRepository(@Qualifier("inventoryDataSource") DataSource dataSource) {
        this.jdbc = JdbcClient.create(dataSource);
        this.transactions = new TransactionTemplate(new DataSourceTransactionManager(dataSource));
    }

    void seedIfAbsent(String sku, int quantity) {
        jdbc.sql("INSERT INTO inventory.stock (sku, quantity, reserved) "
                        + "SELECT :sku, :quantity, 0 WHERE NOT EXISTS (SELECT 1 FROM inventory.stock WHERE sku = :sku)")
                .param("sku", sku)
                .param("quantity", quantity)
                .update();
    }

    /** Idempotent: a repeated hold for the same order id is a no-op. */
    void hold(InventoryRequest request) {
        transactions.executeWithoutResult(status -> {
            int existing = jdbc.sql("SELECT COUNT(*) FROM inventory.holds WHERE id = :id")
                    .param("id", request.orderId())
                    .query(Integer.class)
                    .single();
            if (existing > 0) {
                return;
            }
            int reserved = jdbc.sql("UPDATE inventory.stock SET reserved = reserved + :quantity "
                            + "WHERE sku = :sku AND quantity - reserved >= :quantity")
                    .param("sku", request.sku())
                    .param("quantity", request.quantity())
                    .update();
            if (reserved == 0) {
                throw new NotInStockException("Item is not in stock: " + request.sku());
            }
            jdbc.sql("INSERT INTO inventory.holds (id, sku, quantity, status) VALUES (:id, :sku, :quantity, 'HELD')")
                    .param("id", request.orderId())
                    .param("sku", request.sku())
                    .param("quantity", request.quantity())
                    .update();
        });
    }

    /** Idempotent: only a HELD hold can be settled, so replayed events change nothing. */
    boolean settle(String orderId, boolean approved) {
        return Boolean.TRUE.equals(transactions.execute(status -> {
            var hold = jdbc.sql("SELECT sku, quantity FROM inventory.holds WHERE id = :id AND status = 'HELD'")
                    .param("id", orderId)
                    .query((rs, rowNum) -> Map.entry(rs.getString("sku"), rs.getInt("quantity")))
                    .optional();
            if (hold.isEmpty()) {
                return false;
            }
            int quantity = hold.get().getValue();
            String sku = hold.get().getKey();
            if (approved) {
                jdbc.sql("UPDATE inventory.stock SET quantity = quantity - :quantity, reserved = reserved - :quantity WHERE sku = :sku")
                        .param("quantity", quantity)
                        .param("sku", sku)
                        .update();
            } else {
                jdbc.sql("UPDATE inventory.stock SET reserved = reserved - :quantity WHERE sku = :sku")
                        .param("quantity", quantity)
                        .param("sku", sku)
                        .update();
            }
            jdbc.sql("UPDATE inventory.holds SET status = :status, updated_at = CURRENT_TIMESTAMP WHERE id = :id")
                    .param("status", approved ? "CONFIRMED" : "RELEASED")
                    .param("id", orderId)
                    .update();
            return true;
        }));
    }
}