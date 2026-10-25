package ie.bitstep.fractalith.modules.shop.internal;

import ie.bitstep.fractalith.modules.inventory.api.InventoryApi;
import ie.bitstep.fractalith.modules.inventory.api.InventoryException;
import ie.bitstep.fractalith.modules.inventory.api.InventoryRequest;
import ie.bitstep.fractalith.modules.inventory.api.NotInStockException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Service
@ConditionalOnProperty(prefix = "inventory", name = "api.type", havingValue = "http")
class HttpInventoryApi implements InventoryApi {

    private final RestClient restClient;

    HttpInventoryApi(@Value("${inventory.api.url:http://localhost:8080}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public void hold(InventoryRequest request) {
        try {
            restClient.post().uri("/inventory/hold").body(request).retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == HttpStatus.CONFLICT.value()) {
                throw new NotInStockException("Item is not in stock: " + request.sku());
            }
            throw new InventoryException("Inventory service unavailable");
        }
    }
}