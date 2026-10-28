package ie.bitstep.fractalith.modules.payment.internal;

import ie.bitstep.fractalith.common.api.EventPublisher;
import ie.bitstep.fractalith.modules.order.api.OrderPlacedEvent;
import ie.bitstep.fractalith.modules.payment.api.Payment;
import ie.bitstep.fractalith.modules.payment.api.PaymentProcessedEvent;
import ie.bitstep.fractalith.modules.payment.api.PaymentRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "modules.payment", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
class PaymentService {

    private final List<EventPublisher<PaymentProcessedEvent>> eventPublishers;

    public void processOrderPlacedEvent(OrderPlacedEvent event) {
        log.debug("Processing order placed event: {}", event);
        Payment payment = processPayment(new PaymentRequest(event.orderId(), event.quantity() * 9.99));
        log.info("Payment processed for order {}: {}", event.orderId(), payment.status());

        PaymentProcessedEvent paymentProcessedEvent = new PaymentProcessedEvent(payment.orderId(), payment.amount(), payment.status());
        eventPublishers.forEach(publisher -> publisher.publish(paymentProcessedEvent));
    }

    private Payment processPayment(PaymentRequest request) {
        return new Payment(request.orderId(), request.amount(), "APPROVED");
    }
}
