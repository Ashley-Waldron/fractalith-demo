package ie.bitstep.fractalith.modules.order.internal;

import ie.bitstep.fractalith.common.api.ConditionalOnPropertyContains;
import ie.bitstep.fractalith.common.api.KafkaEventPublisher;
import ie.bitstep.fractalith.modules.order.api.OrderPlacedEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component("orderPlacedKafkaEventPublisher")
@ConditionalOnProperty(prefix = "modules.order", name = "enabled", havingValue = "true")
@ConditionalOnPropertyContains(prefix = "modules.order.publishers", name = "order-placed", value = "kafka")
class OrderPlacedKafkaEventPublisher extends KafkaEventPublisher<OrderPlacedEvent> {

    OrderPlacedKafkaEventPublisher(KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate) {
        super(kafkaTemplate, "orders.placed");
    }
}
