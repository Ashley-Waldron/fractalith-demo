package ie.bitstep.fractalith.modules.inventory.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "modules.inventory", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
class InventorySeeder implements ApplicationRunner {

    private final InventoryRepository repository;

    @Value("${modules.inventory.seed-stock:1000000}")
    private int seedStock;

    @Override
    public void run(ApplicationArguments args) {
        repository.seedIfAbsent("sku-1", seedStock);
        log.debug("Inventory stock ready for sku-1 (seed quantity {})", seedStock);
    }
}