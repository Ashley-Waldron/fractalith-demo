package ie.bitstep.fractalith.modules.inventory.api;

public record InventoryHeldEvent(String orderId, String sku, int quantity, String status) {
}
