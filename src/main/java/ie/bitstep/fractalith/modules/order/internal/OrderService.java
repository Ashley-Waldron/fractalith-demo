package ie.bitstep.fractalith.modules.order.internal;

import ie.bitstep.fractalith.common.api.EventPublisher;
import ie.bitstep.fractalith.modules.order.api.Order;
import ie.bitstep.fractalith.modules.order.api.OrderPlacedEvent;
import ie.bitstep.fractalith.modules.order.api.CreateOrderRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "modules.order", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
class OrderService {

    private final List<EventPublisher<OrderPlacedEvent>> eventPublishers;

    @Async("applicationTaskExecutor")
    @EventListener(CreateOrderRequest.class)
    void handleOrderRequest(CreateOrderRequest request) {
        log.debug("Received create order event over Spring event Listener: {}", request);
        createOrder(request);
    }

    private void createOrder(CreateOrderRequest request) {
        String orderId = request.id();
        Order order = new Order(orderId, request.sku(), request.quantity(), "CREATED");
        log.debug("Created order: {}", order);
        publishOrderPlacedEvent(new OrderPlacedEvent(order.id(), request.customerId(), request.sku(), request.quantity()));
    }

    void publishOrderPlacedEvent(OrderPlacedEvent event) {
        eventPublishers.forEach(publisher -> publisher.publish(event));
    }
}
