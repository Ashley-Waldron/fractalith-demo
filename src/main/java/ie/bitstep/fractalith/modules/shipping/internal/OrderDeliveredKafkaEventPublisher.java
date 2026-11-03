package ie.bitstep.fractalith.modules.shipping.internal;

import ie.bitstep.fractalith.common.api.ConditionalOnPropertyContains;
import ie.bitstep.fractalith.common.api.KafkaEventPublisher;
import ie.bitstep.fractalith.modules.order.api.OrderDeliveredEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component("shippingOrderDeliveredKafkaEventPublisher")
@ConditionalOnProperty(prefix = "modules.shipping", name = "enabled", havingValue = "true")
@ConditionalOnPropertyContains(prefix = "modules.shipping.publishers", name = "order-delivered", value = "kafka")
class OrderDeliveredKafkaEventPublisher extends KafkaEventPublisher<OrderDeliveredEvent> {

    OrderDeliveredKafkaEventPublisher(KafkaTemplate<String, OrderDeliveredEvent> kafkaTemplate) {
        super(kafkaTemplate, "orders.delivered");
    }
}
