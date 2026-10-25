package ie.bitstep.fractalith.common.api;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SpringEventPublisher<T> implements EventPublisher<T> {

    private final ApplicationEventPublisher delegate;

    @Override
    public void publish(T event) {
        delegate.publishEvent(event);
    }
}
