package ie.bitstep.fractalith.modules.shop.internal;

import ie.bitstep.fractalith.common.api.EventPublisher;
import ie.bitstep.fractalith.modules.inventory.api.InventoryApi;
import ie.bitstep.fractalith.modules.inventory.api.InventoryRequest;
import ie.bitstep.fractalith.modules.order.api.CreateOrderRequest;
import ie.bitstep.fractalith.modules.shop.api.CheckoutRequest;
import ie.bitstep.fractalith.modules.shop.api.ShopApi;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShopService implements ShopApi {

    private final InventoryApi inventoryApi;
    private final EventPublisher<CreateOrderRequest> eventPublisher;

    @Override
    public String checkout(CheckoutRequest request) {
        InventoryRequest inventoryRequest = new InventoryRequest(request.sku(), request.quantity());
        if (!inventoryApi.isAvailable(inventoryRequest)) {
            return "Unavailable";
        }

        String orderId = UUID.randomUUID().toString();
        CreateOrderRequest createOrderRequest = new CreateOrderRequest(orderId, request.customerId(), request.sku(), request.quantity());
        eventPublisher.publish(createOrderRequest);
        return "Customer " + request.customerId() + " checked out and async event processing has started.";
    }
}
