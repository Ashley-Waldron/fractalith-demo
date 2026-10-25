package ie.bitstep.fractalith.modules.search.internal;

import ie.bitstep.fractalith.common.api.KafkaEventConsumer;
import ie.bitstep.fractalith.modules.order.api.OrderPlacedEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component("searchOrderPlacedKafkaEventConsumer")
@ConditionalOnProperty(prefix = "search", name = "event.type", havingValue = "kafka")
class OrderPlacedKafkaEventConsumer extends KafkaEventConsumer<OrderPlacedEvent> {

    OrderPlacedKafkaEventConsumer(SearchService searchService) {
        super("orders.placed", searchService::processOrderPlacedEvent);
    }
}