package ie.bitstep.fractalith.modules.search.internal;

import ie.bitstep.fractalith.modules.order.api.OrderPlacedEvent;
import ie.bitstep.fractalith.modules.search.api.ProductDocument;
import ie.bitstep.fractalith.modules.search.api.SearchApi;
import ie.bitstep.fractalith.modules.search.api.SearchRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
class SearchService implements SearchApi {

    @Override
    public List<ProductDocument> search(SearchRequest request) {
        log.debug("Received search request: {}", request);
        if (request == null || request.query() == null || request.query().isBlank()) {
            log.debug("Search request is invalid.");
            return List.of();
        }

        log.debug("Executing search for query: {}", request.query());
        return List.of(
            new ProductDocument("SKU-100", "Sample product"),
            new ProductDocument("SKU-200", "Another sample product")
        );
    }

    void processOrderPlacedEvent(OrderPlacedEvent event) {
        ProductDocument document = new ProductDocument(event.sku(), "Indexed product for order " + event.orderId());
        log.info("Search index update for sku {}: {}", event.sku(), document.name());
    }
}
