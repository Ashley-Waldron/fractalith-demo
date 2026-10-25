package ie.bitstep.fractalith.modules.payment.api;

public record PaymentRequest(String orderId, double amount) {
}
