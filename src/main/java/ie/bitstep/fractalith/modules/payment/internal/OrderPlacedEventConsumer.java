package ie.bitstep.fractalith.modules.payment.internal;

import ie.bitstep.fractalith.common.api.ConditionalOnPropertyContains;
import ie.bitstep.fractalith.common.api.EventConsumer;
import ie.bitstep.fractalith.modules.order.api.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component("paymentOrderPlacedEventConsumer")
@ConditionalOnProperty(prefix = "modules.payment", name = "enabled", havingValue = "true")
@ConditionalOnPropertyContains(prefix = "modules.payment.consumers", name = "order-placed", value = "local")
@RequiredArgsConstructor
@Slf4j
class OrderPlacedEventConsumer implements EventConsumer<OrderPlacedEvent> {

    private final PaymentService paymentService;

    @Async("applicationTaskExecutor")
    @EventListener(OrderPlacedEvent.class)
    @Override
    public void consume(OrderPlacedEvent event) {
        log.debug("Received order placed event over Spring event Listener: {}", event);
        paymentService.processOrderPlacedEvent(event);
    }
}
