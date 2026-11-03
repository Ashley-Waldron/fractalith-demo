package ie.bitstep.fractalith.modules.order.internal;

import ie.bitstep.fractalith.common.api.EventPublisher;
import ie.bitstep.fractalith.modules.inventory.api.InventoryHeldEvent;
import ie.bitstep.fractalith.modules.order.api.Order;
import ie.bitstep.fractalith.modules.order.api.OrderDeliveredEvent;
import ie.bitstep.fractalith.modules.order.api.OrderDetails;
import ie.bitstep.fractalith.modules.order.api.OrderPlacedEvent;
import ie.bitstep.fractalith.modules.order.api.OrderReadyForShipmentEvent;
import ie.bitstep.fractalith.modules.order.api.OrderShippedEvent;
import ie.bitstep.fractalith.modules.order.api.CreateOrderRequest;
import ie.bitstep.fractalith.modules.payment.api.PaymentProcessedEvent;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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
public class OrderService {

    private final List<EventPublisher<OrderPlacedEvent>> orderPlacedEventPublishers;
    private final List<EventPublisher<OrderReadyForShipmentEvent>> readyForShipmentEventPublishers;
    private final List<EventPublisher<OrderShippedEvent>> shippedEventPublishers;
    private final List<EventPublisher<OrderDeliveredEvent>> deliveredEventPublishers;
    private final OrderRepository orderRepository;
    private final ConcurrentMap<String, Set<String>> orderStages = new ConcurrentHashMap<>();

    @Async("applicationTaskExecutor")
    @EventListener(CreateOrderRequest.class)
    void handleOrderRequest(CreateOrderRequest request) {
        log.debug("Received create order event over Spring event Listener: {}", request);
        createOrder(request);
    }

    private void createOrder(CreateOrderRequest request) {
        String orderId = request.id();
        Order order = new Order(orderId, request.sku(), request.quantity(), "CREATED");
        orderRepository.save(order, request.customerId());
        log.debug("Created order: {}", order);
        publishOrderPlacedEvent(new OrderPlacedEvent(order.id(), request.customerId(), request.sku(), request.quantity()));
    }

    public OrderDetails getOrder(String id) {
        return orderRepository.findById(id);
    }

    @Async("applicationTaskExecutor")
    void onInventoryHeld(InventoryHeldEvent event) {
        log.debug("Updating order {} to inventory held", event.orderId());
        recordStage(event.orderId(), "INVENTORY_HELD");
        orderRepository.updateStatus(event.orderId(), "INVENTORY_HELD");
    }

    @Async("applicationTaskExecutor")
    void onPaymentProcessed(PaymentProcessedEvent event) {
        String status = "APPROVED".equalsIgnoreCase(event.status()) ? "PAID" : "FAILED";
        log.debug("Updating order {} to {} after payment processing", event.orderId(), status);
        if ("FAILED".equalsIgnoreCase(status)) {
            orderRepository.updateStatus(event.orderId(), status);
            return;
        }
        recordStage(event.orderId(), "PAID");
        orderRepository.updateStatus(event.orderId(), status);
    }

    @Async("applicationTaskExecutor")
    void onOrderShipped(OrderShippedEvent event) {
        log.info("Order {} has shipped; updating status to SHIPPED.", event.orderId());
        orderRepository.updateStatus(event.orderId(), "SHIPPED");
    }

    @Async("applicationTaskExecutor")
    void onOrderDelivered(OrderDeliveredEvent event) {
        log.info("Order {} has been delivered; updating status to DELIVERED.", event.orderId());
        orderRepository.updateStatus(event.orderId(), "DELIVERED");
    }

    void publishOrderPlacedEvent(OrderPlacedEvent event) {
        orderPlacedEventPublishers.forEach(publisher -> publisher.publish(event));
    }

    private void recordStage(String orderId, String stage) {
        Set<String> stages = orderStages.computeIfAbsent(orderId, key -> ConcurrentHashMap.newKeySet());
        stages.add(stage);
        if (stages.contains("INVENTORY_HELD") && stages.contains("PAID")) {
            orderRepository.updateStatus(orderId, "READY_FOR_SHIPMENT");
            OrderReadyForShipmentEvent ready = new OrderReadyForShipmentEvent(orderId);
            log.info("Order {} is ready for shipment; emitting domain event.", orderId);
            readyForShipmentEventPublishers.forEach(publisher -> publisher.publish(ready));
        }
    }
}
