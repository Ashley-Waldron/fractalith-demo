package ie.bitstep.fractalith.modules.order.internal.event.consumers.kafka;

import ie.bitstep.fractalith.common.api.ConditionalOnPropertyContains;
import ie.bitstep.fractalith.common.api.EventConsumer;
import ie.bitstep.fractalith.modules.order.api.OrderShippedEvent;
import ie.bitstep.fractalith.modules.order.internal.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component("orderShippedKafkaEventConsumer")
@ConditionalOnProperty(prefix = "modules.order", name = "enabled", havingValue = "true")
@ConditionalOnPropertyContains(prefix = "modules.order.consumers", name = "order-shipped", value = "kafka")
@RequiredArgsConstructor
@Slf4j
class OrderShippedKafkaEventConsumer implements EventConsumer<OrderShippedEvent> {

    private final OrderService orderService;

    @Override
    @KafkaListener(topics = "orders.shipped", groupId = "fractalith")
    public void consume(OrderShippedEvent event) {
        log.info("Received Kafka order shipped event on topic 'orders.shipped': {}", event);
        orderService.onOrderShipped(event);
    }
}
