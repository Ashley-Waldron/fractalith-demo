package ie.bitstep.fractalith.modules.shipping.internal;

import ie.bitstep.fractalith.common.api.ConditionalOnPropertyContains;
import ie.bitstep.fractalith.common.api.EventConsumer;
import ie.bitstep.fractalith.modules.order.api.OrderReadyForShipmentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component("shippingOrderReadyForShipmentKafkaEventConsumer")
@ConditionalOnProperty(prefix = "modules.shipping", name = "enabled", havingValue = "true")
@ConditionalOnPropertyContains(prefix = "modules.shipping.consumers", name = "order-ready-for-shipment", value = "kafka")
@RequiredArgsConstructor
@Slf4j
class OrderReadyForShipmentKafkaEventConsumer implements EventConsumer<OrderReadyForShipmentEvent> {

    private final ShippingService shippingService;

    @Override
    @KafkaListener(topics = "orders.ready-for-shipment", groupId = "fractalith")
    public void consume(OrderReadyForShipmentEvent event) {
        log.info("Received Kafka order ready for shipment event on topic 'orders.ready-for-shipment': {}", event);
        shippingService.onOrderReadyForShipment(event);
    }
}
