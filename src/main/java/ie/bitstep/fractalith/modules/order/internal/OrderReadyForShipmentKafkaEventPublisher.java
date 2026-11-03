package ie.bitstep.fractalith.modules.order.internal;

import ie.bitstep.fractalith.common.api.ConditionalOnPropertyContains;
import ie.bitstep.fractalith.common.api.KafkaEventPublisher;
import ie.bitstep.fractalith.modules.order.api.OrderReadyForShipmentEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component("orderReadyForShipmentKafkaEventPublisher")
@ConditionalOnProperty(prefix = "modules.order", name = "enabled", havingValue = "true")
@ConditionalOnPropertyContains(prefix = "modules.order.publishers", name = "order-ready-for-shipment", value = "kafka")
class OrderReadyForShipmentKafkaEventPublisher extends KafkaEventPublisher<OrderReadyForShipmentEvent> {

    OrderReadyForShipmentKafkaEventPublisher(KafkaTemplate<String, OrderReadyForShipmentEvent> kafkaTemplate) {
        super(kafkaTemplate, "orders.ready-for-shipment");
    }
}
