package ie.bitstep.fractalith.common.api;

@FunctionalInterface
public interface EventConsumer<T> {

    void consume(T event);
}
