package ie.bitstep.fractalith.modules.order.internal;

import ie.bitstep.fractalith.common.api.ConditionalOnPropertyContains;
import ie.bitstep.fractalith.common.api.EventConsumer;
import ie.bitstep.fractalith.modules.order.api.OrderDeliveredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component("orderDeliveredKafkaEventConsumer")
@ConditionalOnProperty(prefix = "modules.order", name = "enabled", havingValue = "true")
@ConditionalOnPropertyContains(prefix = "modules.order.consumers", name = "order-delivered", value = "kafka")
@RequiredArgsConstructor
@Slf4j
class OrderDeliveredKafkaEventConsumer implements EventConsumer<OrderDeliveredEvent> {

    private final OrderService orderService;

    @Override
    @KafkaListener(topics = "orders.delivered", groupId = "fractalith")
    public void consume(OrderDeliveredEvent event) {
        log.info("Received Kafka order delivered event on topic 'orders.delivered': {}", event);
        orderService.onOrderDelivered(event);
    }
}
