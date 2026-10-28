package ie.bitstep.fractalith.modules.shop.api;

import ie.bitstep.fractalith.modules.shop.internal.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/shop")
@ConditionalOnProperty(prefix = "modules.shop", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;

    @PostMapping("/checkout")
    public String checkout(@RequestBody CheckoutRequest request) {
        return shopService.checkout(request);
    }
}
