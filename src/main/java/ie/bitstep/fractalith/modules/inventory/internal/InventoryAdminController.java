package ie.bitstep.fractalith.modules.inventory.internal;

import java.time.Duration;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inventory/admin/datasource")
@ConditionalOnProperty(prefix = "modules.inventory", name = "enabled", havingValue = "true")
@ConditionalOnProperty(prefix = "modules.inventory", name = "inbound", havingValue = "http")
@RequiredArgsConstructor
class InventoryAdminController {

    private final InventoryDatabaseMigrator migrator;

    @GetMapping
    Map<String, Object> status() {
        return migrator.status();
    }

    @PostMapping("/prepare")
    Map<String, Object> prepare() {
        migrator.prepareTarget();
        return migrator.status();
    }

    @PostMapping("/freeze")
    Map<String, Object> freeze(@RequestParam(defaultValue = "30") long autoResumeSeconds) {
        migrator.freeze(Duration.ofSeconds(autoResumeSeconds));
        return migrator.status();
    }

    @PostMapping("/cutover")
    Map<String, Object> cutover() {
        migrator.cutover();
        return migrator.status();
    }

    @PostMapping("/abort")
    Map<String, Object> abort() {
        migrator.abort();
        return migrator.status();
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    Map<String, String> conflict(IllegalStateException e) {
        return Map.of("error", e.getMessage());
    }
}