package ie.bitstep.fractalith.modules.inventory.internal;

import ie.bitstep.fractalith.common.api.EventPublisher;
import ie.bitstep.fractalith.modules.inventory.api.InventoryApi;
import ie.bitstep.fractalith.modules.inventory.api.InventoryHeldEvent;
import ie.bitstep.fractalith.modules.inventory.api.InventoryRequest;
import ie.bitstep.fractalith.modules.inventory.api.NotInStockException;
import ie.bitstep.fractalith.modules.payment.api.PaymentProcessedEvent;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
@ConditionalOnProperty(prefix = "modules.inventory", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
class InventoryService implements InventoryApi {

    private final InventoryRepository repository;
    private final List<EventPublisher<InventoryHeldEvent>> eventPublishers;

    @Override
    public void hold(InventoryRequest request) {
        log.debug("Processing inventory hold request: {}", request);
        if (request == null || request.orderId() == null || request.quantity() <= 0
                || request.sku() == null || request.sku().isBlank()) {
            throw new NotInStockException("Item is not in stock: " + (request == null ? "unknown" : request.sku()));
        }
        repository.hold(request);
        log.debug("Inventory held for order {}: sku {}, quantity {}", request.orderId(), request.sku(), request.quantity());

        InventoryHeldEvent event = new InventoryHeldEvent(request.orderId(), request.sku(), request.quantity(), "HELD");
        eventPublishers.forEach(publisher -> publisher.publish(event));
    }

    void onPaymentProcessed(PaymentProcessedEvent event) {
        boolean approved = "APPROVED".equalsIgnoreCase(event.status());
        if (repository.settle(event.orderId(), approved)) {
            log.debug("{} inventory for order {}", approved ? "Committed" : "Released", event.orderId());
        }
    }
}