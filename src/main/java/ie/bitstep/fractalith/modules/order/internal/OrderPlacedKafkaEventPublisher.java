package ie.bitstep.fractalith.modules.order.internal;

import ie.bitstep.fractalith.common.api.KafkaEventPublisher;
import ie.bitstep.fractalith.modules.order.api.OrderPlacedEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component("orderPlacedKafkaEventPublisher")
@ConditionalOnProperty(prefix = "order", name = "event.type", havingValue = "kafka")
class OrderPlacedKafkaEventPublisher extends KafkaEventPublisher<OrderPlacedEvent> {

    OrderPlacedKafkaEventPublisher(KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate) {
        super(kafkaTemplate, "orders.placed");
    }
}
