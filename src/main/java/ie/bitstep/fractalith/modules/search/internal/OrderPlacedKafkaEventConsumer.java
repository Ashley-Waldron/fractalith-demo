package ie.bitstep.fractalith.modules.search.internal;

import ie.bitstep.fractalith.common.api.EventConsumer;
import ie.bitstep.fractalith.modules.order.api.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component("searchOrderPlacedKafkaEventConsumer")
@ConditionalOnProperty(prefix = "search", name = "event.type", havingValue = "kafka")
@RequiredArgsConstructor
@Slf4j
class OrderPlacedKafkaEventConsumer implements EventConsumer<OrderPlacedEvent> {

    private final SearchService searchService;

    @Override
    @KafkaListener(topics = "orders.placed", groupId = "fractalith")
    public void consume(OrderPlacedEvent event) {
        log.debug("Received order placed event over Kafka topic 'orders.placed': {}", event);
        searchService.processOrderPlacedEvent(event);
    }
}