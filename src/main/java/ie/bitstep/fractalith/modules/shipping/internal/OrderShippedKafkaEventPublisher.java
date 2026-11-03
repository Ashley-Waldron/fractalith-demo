package ie.bitstep.fractalith.modules.shipping.internal;

import ie.bitstep.fractalith.common.api.ConditionalOnPropertyContains;
import ie.bitstep.fractalith.common.api.KafkaEventPublisher;
import ie.bitstep.fractalith.modules.order.api.OrderShippedEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component("shippingOrderShippedKafkaEventPublisher")
@ConditionalOnProperty(prefix = "modules.shipping", name = "enabled", havingValue = "true")
@ConditionalOnPropertyContains(prefix = "modules.shipping.publishers", name = "order-shipped", value = "kafka")
class OrderShippedKafkaEventPublisher extends KafkaEventPublisher<OrderShippedEvent> {

    OrderShippedKafkaEventPublisher(KafkaTemplate<String, OrderShippedEvent> kafkaTemplate) {
        super(kafkaTemplate, "orders.shipped");
    }
}
