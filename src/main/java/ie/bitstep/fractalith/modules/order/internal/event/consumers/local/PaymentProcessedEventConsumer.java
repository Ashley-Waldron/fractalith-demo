package ie.bitstep.fractalith.modules.order.internal.event.consumers.local;

import ie.bitstep.fractalith.common.api.ConditionalOnPropertyContains;
import ie.bitstep.fractalith.common.api.EventConsumer;
import ie.bitstep.fractalith.modules.order.internal.OrderService;
import ie.bitstep.fractalith.modules.payment.api.PaymentProcessedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component("orderPaymentProcessedEventConsumer")
@ConditionalOnProperty(prefix = "modules.order", name = "enabled", havingValue = "true")
@ConditionalOnPropertyContains(prefix = "modules.order.consumers", name = "payment-processed", value = "local")
@RequiredArgsConstructor
@Slf4j
class PaymentProcessedEventConsumer implements EventConsumer<PaymentProcessedEvent> {

    private final OrderService orderService;

    @Async("applicationTaskExecutor")
    @EventListener(PaymentProcessedEvent.class)
    @Override
    public void consume(PaymentProcessedEvent event) {
        log.info("Received payment processed event over Spring event Listener: {}", event);
        orderService.onPaymentProcessed(event);
    }
}
