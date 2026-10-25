package ie.bitstep.fractalith.modules.payment.internal;

import ie.bitstep.fractalith.common.api.KafkaEventConsumer;
import ie.bitstep.fractalith.modules.order.api.OrderPlacedEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component("paymentOrderPlacedKafkaEventConsumer")
@ConditionalOnProperty(prefix = "payment", name = "transport", havingValue = "kafka")
class OrderPlacedKafkaEventConsumer extends KafkaEventConsumer<OrderPlacedEvent> {

    OrderPlacedKafkaEventConsumer(PaymentService paymentService) {
        super("orders.placed", paymentService::processOrderPlacedEvent);
    }
}