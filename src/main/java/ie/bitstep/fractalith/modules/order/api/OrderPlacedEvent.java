package ie.bitstep.fractalith.modules.order.api;

public record OrderPlacedEvent(String orderId, String customerId, String sku, int quantity) {
}
