package ie.bitstep.fractalith.modules.order.internal;

import ie.bitstep.fractalith.common.api.ConditionalOnPropertyContains;
import ie.bitstep.fractalith.common.api.EventConsumer;
import ie.bitstep.fractalith.modules.order.api.CreateOrderRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component("orderCreateOrderRequestKafkaEventConsumer")
@ConditionalOnProperty(prefix = "modules.order", name = "enabled", havingValue = "true")
@ConditionalOnPropertyContains(prefix = "modules.order.consumers", name = "create-order-request", value = "kafka")
@RequiredArgsConstructor
@Slf4j
class CreateOrderRequestKafkaEventConsumer implements EventConsumer<CreateOrderRequest> {

    private final OrderService orderService;

    @Override
    @KafkaListener(topics = "orders.created", groupId = "fractalith")
    public void consume(CreateOrderRequest event) {
        log.info("Received Kafka create order request event on topic 'orders.created': {}", event);
        orderService.handleOrderRequest(event);
    }
}
