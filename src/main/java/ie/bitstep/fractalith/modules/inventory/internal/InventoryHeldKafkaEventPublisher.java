package ie.bitstep.fractalith.modules.inventory.internal;

import ie.bitstep.fractalith.common.api.ConditionalOnPropertyContains;
import ie.bitstep.fractalith.common.api.KafkaEventPublisher;
import ie.bitstep.fractalith.modules.inventory.api.InventoryHeldEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component("inventoryHeldKafkaEventPublisher")
@ConditionalOnProperty(prefix = "modules.inventory", name = "enabled", havingValue = "true")
@ConditionalOnPropertyContains(prefix = "modules.inventory.publishers", name = "inventory-held", value = "kafka")
class InventoryHeldKafkaEventPublisher extends KafkaEventPublisher<InventoryHeldEvent> {

    InventoryHeldKafkaEventPublisher(KafkaTemplate<String, InventoryHeldEvent> kafkaTemplate) {
        super(kafkaTemplate, "inventory.held");
    }
}
