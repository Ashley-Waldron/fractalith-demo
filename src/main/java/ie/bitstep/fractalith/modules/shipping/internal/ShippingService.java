package ie.bitstep.fractalith.modules.shipping.internal;

import ie.bitstep.fractalith.common.api.EventPublisher;
import ie.bitstep.fractalith.modules.order.api.OrderDeliveredEvent;
import ie.bitstep.fractalith.modules.order.api.OrderReadyForShipmentEvent;
import ie.bitstep.fractalith.modules.order.api.OrderShippedEvent;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "modules.shipping", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
class ShippingService {

    private final List<EventPublisher<OrderShippedEvent>> shippedEventPublishers;
    private final List<EventPublisher<OrderDeliveredEvent>> deliveredEventPublishers;

    @Async("applicationTaskExecutor")
    @EventListener(OrderReadyForShipmentEvent.class)
    void onOrderReadyForShipment(OrderReadyForShipmentEvent event) {
        log.info("Order {} is ready for shipment; creating shipment and marking as shipped.", event.orderId());
        shippedEventPublishers.forEach(publisher -> publisher.publish(new OrderShippedEvent(event.orderId())));
    }

    @Async("applicationTaskExecutor")
    @EventListener(OrderShippedEvent.class)
    void onOrderShipped(OrderShippedEvent event) {
        log.info("Order {} is now shipped; delivering it to the customer.", event.orderId());
        deliveredEventPublishers.forEach(publisher -> publisher.publish(new OrderDeliveredEvent(event.orderId())));
    }
}
