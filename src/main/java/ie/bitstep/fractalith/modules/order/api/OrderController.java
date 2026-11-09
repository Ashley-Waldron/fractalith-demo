package ie.bitstep.fractalith.modules.order.api;

import ie.bitstep.fractalith.modules.order.internal.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@ConditionalOnProperty(prefix = "modules.order", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/{id}")
    public OrderDetails getOrder(@PathVariable String id) {
        log.debug("Received HTTP request to get order: {}", id);
        return orderService.getOrder(id);
    }
}
