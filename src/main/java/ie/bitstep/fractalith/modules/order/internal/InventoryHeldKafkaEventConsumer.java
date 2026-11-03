package ie.bitstep.fractalith.modules.order.internal;

import ie.bitstep.fractalith.common.api.ConditionalOnPropertyContains;
import ie.bitstep.fractalith.common.api.EventConsumer;
import ie.bitstep.fractalith.modules.inventory.api.InventoryHeldEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component("orderInventoryHeldKafkaEventConsumer")
@ConditionalOnProperty(prefix = "modules.order", name = "enabled", havingValue = "true")
@ConditionalOnPropertyContains(prefix = "modules.order.consumers", name = "inventory-held", value = "kafka")
@RequiredArgsConstructor
@Slf4j
class InventoryHeldKafkaEventConsumer implements EventConsumer<InventoryHeldEvent> {

    private final OrderService orderService;

    @Override
    @KafkaListener(topics = "inventory.held", groupId = "fractalith")
    public void consume(InventoryHeldEvent event) {
        log.info("Received Kafka inventory held event on topic 'inventory.held': {}", event);
        orderService.onInventoryHeld(event);
    }
}
