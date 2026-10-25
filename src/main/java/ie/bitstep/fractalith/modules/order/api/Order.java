package ie.bitstep.fractalith.modules.order.api;

public record Order(String id, String sku, int quantity, String status) {
}
