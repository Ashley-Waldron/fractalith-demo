package ie.bitstep.fractalith.modules.inventory.internal;

import ie.bitstep.fractalith.modules.inventory.api.InventoryApi;
import ie.bitstep.fractalith.modules.inventory.api.InventoryRequest;
import org.springframework.stereotype.Service;

@Service
class InventoryService implements InventoryApi {

    @Override
    public boolean isAvailable(InventoryRequest request) {
        return request.quantity() > 0 && request.sku() != null && !request.sku().isBlank();
    }
}
