package ie.bitstep.fractalith.modules.order.internal;

import ie.bitstep.fractalith.common.api.EventPublisher;
import ie.bitstep.fractalith.modules.order.api.Order;
import ie.bitstep.fractalith.modules.order.api.OrderPlacedEvent;
import ie.bitstep.fractalith.modules.order.api.CreateOrderRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class OrderService {

    private final EventPublisher<OrderPlacedEvent> eventPublisher;

    @Async("applicationTaskExecutor")
    @EventListener(CreateOrderRequest.class)
    void handleOrderRequest(CreateOrderRequest request) {
        createOrder(request);
    }

    private void createOrder(CreateOrderRequest request) {
        String orderId = request.id();
        Order order = new Order(orderId, request.sku(), request.quantity(), "CREATED");
        eventPublisher.publish(new OrderPlacedEvent(order.id(), request.customerId(), request.sku(), request.quantity()));
    }
}
