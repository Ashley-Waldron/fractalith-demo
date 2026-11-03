package ie.bitstep.fractalith.modules.order.internal;

import ie.bitstep.fractalith.common.api.ConditionalOnPropertyContains;
import ie.bitstep.fractalith.common.api.EventConsumer;
import ie.bitstep.fractalith.modules.order.api.OrderDeliveredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component("orderDeliveredEventConsumer")
@ConditionalOnProperty(prefix = "modules.order", name = "enabled", havingValue = "true")
@ConditionalOnPropertyContains(prefix = "modules.order.consumers", name = "order-delivered", value = "local")
@RequiredArgsConstructor
@Slf4j
class OrderDeliveredEventConsumer implements EventConsumer<OrderDeliveredEvent> {

    private final OrderService orderService;

    @Async("applicationTaskExecutor")
    @EventListener(OrderDeliveredEvent.class)
    @Override
    public void consume(OrderDeliveredEvent event) {
        log.debug("Received order delivered event over Spring event Listener: {}", event);
        orderService.onOrderDelivered(event);
    }
}
