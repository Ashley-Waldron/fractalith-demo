package ie.bitstep.fractalith.common.api;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;

@RequiredArgsConstructor
public class KafkaEventPublisher<T> implements EventPublisher<T> {

    private final KafkaTemplate<String, T> kafkaTemplate;
    private final String topic;

    @Override
    public void publish(T event) {
        kafkaTemplate.send(topic, event);
    }
}
