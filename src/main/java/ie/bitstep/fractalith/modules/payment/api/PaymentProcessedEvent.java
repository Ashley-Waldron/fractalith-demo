package ie.bitstep.fractalith.modules.payment.api;

public record PaymentProcessedEvent(String orderId, double amount, String status) {
}
