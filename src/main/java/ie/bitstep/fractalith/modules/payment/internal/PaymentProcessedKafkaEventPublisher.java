package ie.bitstep.fractalith.modules.payment.internal;

import ie.bitstep.fractalith.common.api.ConditionalOnPropertyContains;
import ie.bitstep.fractalith.common.api.KafkaEventPublisher;
import ie.bitstep.fractalith.modules.payment.api.PaymentProcessedEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component("paymentProcessedKafkaEventPublisher")
@ConditionalOnProperty(prefix = "modules.payment", name = "enabled", havingValue = "true")
@ConditionalOnPropertyContains(prefix = "modules.payment.publishers", name = "payment-processed", value = "kafka")
class PaymentProcessedKafkaEventPublisher extends KafkaEventPublisher<PaymentProcessedEvent> {

    PaymentProcessedKafkaEventPublisher(KafkaTemplate<String, PaymentProcessedEvent> kafkaTemplate) {
        super(kafkaTemplate, "payments.processed");
    }
}
