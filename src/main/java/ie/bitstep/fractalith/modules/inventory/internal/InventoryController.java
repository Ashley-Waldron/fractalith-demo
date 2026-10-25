package ie.bitstep.fractalith.modules.inventory.internal;

import ie.bitstep.fractalith.modules.inventory.api.InventoryRequest;
import ie.bitstep.fractalith.modules.inventory.api.NotInStockException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inventory")
@ConditionalOnProperty(prefix = "inventory", name = "api.type", havingValue = "http")
@RequiredArgsConstructor
class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/hold")
    public void hold(@RequestBody InventoryRequest request) {
        inventoryService.hold(request);
    }

    @ExceptionHandler(NotInStockException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public void handleNotInStock(NotInStockException ex) {
    }
}
