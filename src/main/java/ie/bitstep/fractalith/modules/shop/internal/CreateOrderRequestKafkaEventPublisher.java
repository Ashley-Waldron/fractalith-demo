package ie.bitstep.fractalith.modules.shop.internal;

import ie.bitstep.fractalith.common.api.ConditionalOnPropertyContains;
import ie.bitstep.fractalith.common.api.KafkaEventPublisher;
import ie.bitstep.fractalith.modules.order.api.CreateOrderRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component("shopCreateOrderRequestKafkaEventPublisher")
@ConditionalOnProperty(prefix = "modules.shop", name = "enabled", havingValue = "true")
@ConditionalOnPropertyContains(prefix = "modules.shop.publishers", name = "create-order-request", value = "kafka")
class CreateOrderRequestKafkaEventPublisher extends KafkaEventPublisher<CreateOrderRequest> {

    CreateOrderRequestKafkaEventPublisher(KafkaTemplate<String, CreateOrderRequest> kafkaTemplate) {
        super(kafkaTemplate, "orders.created");
    }
}
