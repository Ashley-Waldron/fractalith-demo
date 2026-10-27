package ie.bitstep.fractalith.modules.inventory.internal;

import ie.bitstep.fractalith.modules.inventory.api.InventoryApi;
import ie.bitstep.fractalith.modules.inventory.api.InventoryRequest;
import ie.bitstep.fractalith.modules.inventory.api.NotInStockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
@ConditionalOnProperty(prefix = "modules.inventory", name = "enabled", havingValue = "true")
@ConditionalOnProperty(prefix = "modules.inventory.api", name = "type", havingValue = "local", matchIfMissing = true)
@Slf4j
class InventoryService implements InventoryApi {

    @Override
    public void hold(InventoryRequest request) {
        log.debug("Processing inventory hold request: {}", request);
        if (request == null || request.quantity() <= 0 || request.sku() == null || request.sku().isBlank()) {
            throw new NotInStockException("Item is not in stock: " + (request == null ? "unknown" : request.sku()));
        }
        log.debug("Inventory hold request processed successfully for SKU: {}, Quantity: {}", request.sku(), request.quantity());
    }
}
