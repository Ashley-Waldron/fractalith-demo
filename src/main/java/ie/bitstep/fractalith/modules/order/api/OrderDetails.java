package ie.bitstep.fractalith.modules.order.api;

public record OrderDetails(String id, String customerId, String sku, int quantity, String status) {
}
