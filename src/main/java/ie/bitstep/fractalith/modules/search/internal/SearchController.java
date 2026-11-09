package ie.bitstep.fractalith.modules.search.internal;

import ie.bitstep.fractalith.modules.search.api.ProductDocument;
import ie.bitstep.fractalith.modules.search.api.SearchRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/search")
@ConditionalOnProperty(prefix = "modules.search", name = "enabled", havingValue = "true")
@ConditionalOnProperty(prefix = "modules.search", name = "inbound", havingValue = "http")
@RequiredArgsConstructor
@Slf4j
class SearchController {

    private final SearchService searchService;

    @PostMapping
    public List<ProductDocument> search(@RequestBody SearchRequest request) {
        log.debug("Received HTTP search request: {}", request);
        return searchService.search(request);
    }
}
