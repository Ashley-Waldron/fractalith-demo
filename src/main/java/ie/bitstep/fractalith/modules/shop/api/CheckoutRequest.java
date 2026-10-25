package ie.bitstep.fractalith.modules.shop.api;

public record CheckoutRequest(String customerId, String sku, int quantity) {
}
