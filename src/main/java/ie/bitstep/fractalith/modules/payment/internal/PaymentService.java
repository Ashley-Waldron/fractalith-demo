package ie.bitstep.fractalith.modules.payment.internal;

import ie.bitstep.fractalith.modules.order.api.OrderPlacedEvent;
import ie.bitstep.fractalith.modules.payment.api.Payment;
import ie.bitstep.fractalith.modules.payment.api.PaymentRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
class PaymentService {

    public void processOrderPlacedEvent(OrderPlacedEvent event) {
        log.debug("Processing order placed event: {}", event);
        Payment payment = processPayment(new PaymentRequest(event.orderId(), event.quantity() * 9.99));
        log.info("Payment processed for order {}: {}", event.orderId(), payment.status());
    }

    private Payment processPayment(PaymentRequest request) {
        return new Payment(request.orderId(), request.amount(), "APPROVED");
    }
}
