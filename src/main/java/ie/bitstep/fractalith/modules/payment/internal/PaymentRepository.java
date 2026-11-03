package ie.bitstep.fractalith.modules.payment.internal;

import ie.bitstep.fractalith.modules.payment.api.Payment;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "modules.payment", name = "enabled", havingValue = "true")
@Slf4j
class PaymentRepository {

    private final JdbcClient jdbc;

    PaymentRepository(@Qualifier("paymentDataSource") DataSource dataSource) {
        this.jdbc = JdbcClient.create(dataSource);
    }

    void save(Payment payment) {
        try {
            jdbc.sql("INSERT INTO payments.payments (order_id, amount, status) VALUES (:orderId, :amount, :status)")
                    .param("orderId", payment.orderId())
                    .param("amount", payment.amount())
                    .param("status", payment.status())
                    .update();
        } catch (DuplicateKeyException e) {
            log.debug("Payment for order {} already recorded", payment.orderId());
        }
    }
}
