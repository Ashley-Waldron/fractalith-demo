package ie.bitstep.fractalith.modules.shop.internal;

import ie.bitstep.fractalith.common.api.EventPublisher;
import ie.bitstep.fractalith.modules.inventory.api.InventoryApi;
import ie.bitstep.fractalith.modules.inventory.api.InventoryException;
import ie.bitstep.fractalith.modules.inventory.api.InventoryRequest;
import ie.bitstep.fractalith.modules.order.api.CreateOrderRequest;
import ie.bitstep.fractalith.modules.shop.api.CheckoutRequest;
import ie.bitstep.fractalith.modules.shop.api.ShopApi;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "modules.shop", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class ShopService implements ShopApi {

    private final InventoryApi inventoryApi;
    private final EventPublisher<CreateOrderRequest> eventPublisher;

    @Override
    public String checkout(CheckoutRequest request) {
        InventoryRequest inventoryRequest = new InventoryRequest(request.sku(), request.quantity());
        log.debug("Processing checkout request for sku {}: {}", request.sku(), request.quantity());
        try {
            inventoryApi.hold(inventoryRequest);
        } catch (InventoryException e) {
            log.warn("Failed to hold inventory for sku {}: {}", request.sku(), request.quantity());
            return "Unavailable";
        }

        String orderId = UUID.randomUUID().toString();
        CreateOrderRequest createOrderRequest = new CreateOrderRequest(orderId, request.customerId(), request.sku(), request.quantity());
        eventPublisher.publish(createOrderRequest);
        return "Customer " + request.customerId() + " checked out and async event processing has started.";
    }
}
