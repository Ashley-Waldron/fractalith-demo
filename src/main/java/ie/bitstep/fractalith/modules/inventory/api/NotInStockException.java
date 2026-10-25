package ie.bitstep.fractalith.modules.inventory.api;

public class NotInStockException extends InventoryException {

    public NotInStockException(String message) {
        super(message);
    }
}
