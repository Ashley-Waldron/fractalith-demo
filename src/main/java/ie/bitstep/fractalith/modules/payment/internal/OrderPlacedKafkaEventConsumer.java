package ie.bitstep.fractalith.modules.payment.internal;

import ie.bitstep.fractalith.common.api.EventConsumer;
import ie.bitstep.fractalith.modules.order.api.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component("paymentOrderPlacedKafkaEventConsumer")
@ConditionalOnProperty(prefix = "modules.payment", name = "enabled", havingValue = "true")
@ConditionalOnProperty(prefix = "modules.payment.event", name = "type", havingValue = "kafka")
@RequiredArgsConstructor
@Slf4j
class OrderPlacedKafkaEventConsumer implements EventConsumer<OrderPlacedEvent> {

    private final PaymentService paymentService;

    @Override
    @KafkaListener(topics = "orders.placed", groupId = "fractalith")
    public void consume(OrderPlacedEvent event) {
        log.debug("Received Kafka order placed event on Kafka topic 'orders.placed': {}", event);
        paymentService.processOrderPlacedEvent(event);
    }
}