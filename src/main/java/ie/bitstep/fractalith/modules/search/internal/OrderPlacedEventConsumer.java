package ie.bitstep.fractalith.modules.search.internal;

import ie.bitstep.fractalith.common.api.EventConsumer;
import ie.bitstep.fractalith.modules.order.api.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component("searchOrderPlacedEventConsumer")
@ConditionalOnProperty(prefix = "modules.search", name = "enabled", havingValue = "true")
@ConditionalOnProperty(prefix = "modules.search.consumers", name = "order-placed", havingValue = "local", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
class OrderPlacedEventConsumer implements EventConsumer<OrderPlacedEvent> {

    private final SearchService searchService;

    @Async("applicationTaskExecutor")
    @EventListener(OrderPlacedEvent.class)
    @Override
    public void consume(OrderPlacedEvent event) {
        log.debug("Received order placed event over Spring event Listener: {}", event);
        searchService.processOrderPlacedEvent(event);
    }
}