package ie.bitstep.fractalith.common.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpringEventPublisher<T> implements EventPublisher<T> {

    private final ApplicationEventPublisher delegate;

    @Override
    public void publish(T event) {
        delegate.publishEvent(event);
        log.debug("Published event using Spring Event Publisher: {}", event);
    }
}
