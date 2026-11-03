package ie.bitstep.fractalith.modules.order.internal;

import ie.bitstep.fractalith.common.api.ConditionalOnPropertyContains;
import ie.bitstep.fractalith.common.api.EventConsumer;
import ie.bitstep.fractalith.modules.order.api.OrderShippedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component("orderShippedEventConsumer")
@ConditionalOnProperty(prefix = "modules.order", name = "enabled", havingValue = "true")
@ConditionalOnPropertyContains(prefix = "modules.order.consumers", name = "order-shipped", value = "local")
@RequiredArgsConstructor
@Slf4j
class OrderShippedEventConsumer implements EventConsumer<OrderShippedEvent> {

    private final OrderService orderService;

    @Async("applicationTaskExecutor")
    @EventListener(OrderShippedEvent.class)
    @Override
    public void consume(OrderShippedEvent event) {
        log.debug("Received order shipped event over Spring event Listener: {}", event);
        orderService.onOrderShipped(event);
    }
}
