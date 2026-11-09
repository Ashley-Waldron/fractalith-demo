package ie.bitstep.fractalith.modules.order.internal.event.consumers.local;

import ie.bitstep.fractalith.common.api.ConditionalOnPropertyContains;
import ie.bitstep.fractalith.common.api.EventConsumer;
import ie.bitstep.fractalith.modules.inventory.api.InventoryHeldEvent;
import ie.bitstep.fractalith.modules.order.internal.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component("orderInventoryHeldEventConsumer")
@ConditionalOnProperty(prefix = "modules.order", name = "enabled", havingValue = "true")
@ConditionalOnPropertyContains(prefix = "modules.order.consumers", name = "inventory-held", value = "local")
@RequiredArgsConstructor
@Slf4j
class InventoryHeldEventConsumer implements EventConsumer<InventoryHeldEvent> {

    private final OrderService orderService;

    @Async("applicationTaskExecutor")
    @EventListener(InventoryHeldEvent.class)
    @Override
    public void consume(InventoryHeldEvent event) {
        log.info("Received inventory held event over Spring event Listener: {}", event);
        orderService.onInventoryHeld(event);
    }
}
