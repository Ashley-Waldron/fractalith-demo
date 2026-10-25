package ie.bitstep.fractalith.modules.search.api;

import java.util.List;

public interface SearchApi {

    List<ProductDocument> search(SearchRequest request);
}
