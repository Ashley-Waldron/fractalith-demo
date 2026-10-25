package ie.bitstep.fractalith.common.api;

import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class KafkaEventConsumer<T> implements EventConsumer<T> {

    private final String topic;
    private final Consumer<T> delegate;

    @Override
    public void consume(T event) {
        delegate.accept(event);
    }

    public String topic() {
        return topic;
    }
}
