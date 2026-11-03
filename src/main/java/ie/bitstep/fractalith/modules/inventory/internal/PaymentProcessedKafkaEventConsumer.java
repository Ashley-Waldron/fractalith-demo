package ie.bitstep.fractalith.modules.inventory.internal;

import ie.bitstep.fractalith.common.api.ConditionalOnPropertyContains;
import ie.bitstep.fractalith.common.api.EventConsumer;
import ie.bitstep.fractalith.modules.payment.api.PaymentProcessedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component("inventoryPaymentProcessedKafkaEventConsumer")
@ConditionalOnProperty(prefix = "modules.inventory", name = "enabled", havingValue = "true")
@ConditionalOnPropertyContains(prefix = "modules.inventory.consumers", name = "payment-processed", value = "kafka")
@RequiredArgsConstructor
@Slf4j
class PaymentProcessedKafkaEventConsumer implements EventConsumer<PaymentProcessedEvent> {

    private final InventoryService inventoryService;

    @Override
    @KafkaListener(topics = "payments.processed", groupId = "fractalith")
    public void consume(PaymentProcessedEvent event) {
        log.info("Received Kafka payment processed event on topic 'payments.processed': {}", event);
        inventoryService.onPaymentProcessed(event);
    }
}
