package ie.bitstep.fractalith.modules.order.api;

public record CreateOrderRequest(String id, String customerId, String sku, int quantity) {
}
