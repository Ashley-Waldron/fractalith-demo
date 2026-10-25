package ie.bitstep.fractalith.modules.search.internal;

import ie.bitstep.fractalith.common.api.EventConsumer;
import ie.bitstep.fractalith.modules.order.api.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component("searchOrderPlacedEventConsumer")
@ConditionalOnProperty(prefix = "search", name = "transport", havingValue = "spring", matchIfMissing = true)
@RequiredArgsConstructor
class OrderPlacedEventConsumer implements EventConsumer<OrderPlacedEvent> {

    private final SearchService searchService;

    @Async("applicationTaskExecutor")
    @EventListener(OrderPlacedEvent.class)
    @Override
    public void consume(OrderPlacedEvent event) {
        searchService.processOrderPlacedEvent(event);
    }
}
