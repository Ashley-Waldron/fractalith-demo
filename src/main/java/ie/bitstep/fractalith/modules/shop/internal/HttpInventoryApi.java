package ie.bitstep.fractalith.modules.shop.internal;

import ie.bitstep.fractalith.modules.inventory.api.InventoryApi;
import ie.bitstep.fractalith.modules.inventory.api.InventoryException;
import ie.bitstep.fractalith.modules.inventory.api.InventoryRequest;
import ie.bitstep.fractalith.modules.inventory.api.NotInStockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Service
@ConditionalOnProperty(prefix = "modules.shop", name = "enabled", havingValue = "true")
@ConditionalOnProperty(prefix = "modules.shop.apis.inventory", name = "type", havingValue = "http")
@Slf4j
class HttpInventoryApi implements InventoryApi {

    private final RestClient restClient;

    HttpInventoryApi(@Value("${modules.shop.apis.inventory.url:http://localhost:8081}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public void hold(InventoryRequest request) {
        log.debug("Calling inventory service over HTTP to hold item: {}", request);
        try {
            restClient.post().uri("/inventory/hold").body(request).retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            log.warn("Failed to hold inventory for sku {}: {}", request.sku(), request.quantity());
            log.debug("Inventory service error: {}", e.getMessage());
            if (e.getStatusCode().value() == HttpStatus.CONFLICT.value()) {
                throw new NotInStockException("Item is not in stock: " + request.sku());
            }
            throw new InventoryException("Inventory service unavailable");
        }
        log.debug("Successfully called inventory service over HTTP to hold item: {}", request);
    }
}