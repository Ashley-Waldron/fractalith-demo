package ie.bitstep.fractalith.modules.shipping.internal;

import ie.bitstep.fractalith.common.api.ConditionalOnPropertyContains;
import ie.bitstep.fractalith.common.api.EventConsumer;
import ie.bitstep.fractalith.modules.order.api.OrderReadyForShipmentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component("shippingOrderReadyForShipmentEventConsumer")
@ConditionalOnProperty(prefix = "modules.shipping", name = "enabled", havingValue = "true")
@ConditionalOnPropertyContains(prefix = "modules.shipping.consumers", name = "order-ready-for-shipment", value = "local")
@RequiredArgsConstructor
@Slf4j
class OrderReadyForShipmentEventConsumer implements EventConsumer<OrderReadyForShipmentEvent> {

    private final ShippingService shippingService;

    @Async("applicationTaskExecutor")
    @EventListener(OrderReadyForShipmentEvent.class)
    @Override
    public void consume(OrderReadyForShipmentEvent event) {
        log.debug("Received order ready for shipment event over Spring event Listener: {}", event);
        shippingService.onOrderReadyForShipment(event);
    }
}
