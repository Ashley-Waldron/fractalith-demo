package ie.bitstep.fractalith.modules.inventory.internal;

import ie.bitstep.fractalith.common.api.ConditionalOnPropertyContains;
import ie.bitstep.fractalith.common.api.EventConsumer;
import ie.bitstep.fractalith.modules.payment.api.PaymentProcessedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component("inventoryPaymentProcessedKafkaEventConsumer")
@ConditionalOnProperty(prefix = "modules.inventory", name = "enabled", havingValue = "true")
@ConditionalOnPropertyContains(prefix = "modules.inventory.consumers", name = "payment-processed", value = "kafka")
@Slf4j
class PaymentProcessedKafkaEventConsumer implements EventConsumer<PaymentProcessedEvent> {

    @Override
    @KafkaListener(topics = "payments.processed", groupId = "fractalith")
    public void consume(PaymentProcessedEvent event) {
        log.info("Received Kafka payment processed event on topic 'payments.processed': {}", event);
        log.info("decreasing inventory by 1");
    }
}
