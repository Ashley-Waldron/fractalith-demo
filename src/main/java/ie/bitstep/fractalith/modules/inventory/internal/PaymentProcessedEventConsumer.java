package ie.bitstep.fractalith.modules.inventory.internal;

import ie.bitstep.fractalith.common.api.ConditionalOnPropertyContains;
import ie.bitstep.fractalith.common.api.EventConsumer;
import ie.bitstep.fractalith.modules.payment.api.PaymentProcessedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component("inventoryPaymentProcessedEventConsumer")
@ConditionalOnProperty(prefix = "modules.inventory", name = "enabled", havingValue = "true")
@ConditionalOnPropertyContains(prefix = "modules.inventory.consumers", name = "payment-processed", value = "local")
@RequiredArgsConstructor
@Slf4j
class PaymentProcessedEventConsumer implements EventConsumer<PaymentProcessedEvent> {

    private final InventoryService inventoryService;

    @Async("applicationTaskExecutor")
    @EventListener(PaymentProcessedEvent.class)
    @Override
    public void consume(PaymentProcessedEvent event) {
        log.info("Received payment processed event via Spring event Listener:: {}", event);
        inventoryService.onPaymentProcessed(event);
    }
}
