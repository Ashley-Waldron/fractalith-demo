package ie.bitstep.fractalith.common.api;

@FunctionalInterface
public interface EventPublisher<T> {

	void publish(T event);
}
