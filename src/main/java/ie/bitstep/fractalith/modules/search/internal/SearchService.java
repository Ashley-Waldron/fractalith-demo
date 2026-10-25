package ie.bitstep.fractalith.modules.search.internal;

import ie.bitstep.fractalith.modules.order.api.OrderPlacedEvent;
import ie.bitstep.fractalith.modules.search.api.ProductDocument;
import ie.bitstep.fractalith.modules.search.api.SearchApi;
import ie.bitstep.fractalith.modules.search.api.SearchRequest;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
class SearchService implements SearchApi {

    private static final Logger log = LoggerFactory.getLogger(SearchService.class);

    @Override
    public List<ProductDocument> search(SearchRequest request) {
        if (request == null || request.query() == null || request.query().isBlank()) {
            return List.of();
        }

        return List.of(
            new ProductDocument("SKU-100", "Sample product"),
            new ProductDocument("SKU-200", "Another sample product")
        );
    }

    public void processOrderPlacedEvent(OrderPlacedEvent event) {
        ProductDocument document = new ProductDocument(event.sku(), "Indexed product for order " + event.orderId());
        log.info("Asynchronous search index update for sku {}: {}", event.sku(), document.name());
    }
}
