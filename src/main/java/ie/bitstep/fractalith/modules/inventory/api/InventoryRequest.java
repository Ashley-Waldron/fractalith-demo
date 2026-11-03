package ie.bitstep.fractalith.modules.inventory.api;

public record InventoryRequest(String orderId, String sku, int quantity) {
}