package ie.bitstep.fractalith.common.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;

@RequiredArgsConstructor
@Slf4j
public class KafkaEventPublisher<T> implements EventPublisher<T> {

    private final KafkaTemplate<String, T> kafkaTemplate;
    private final String topic;

    @Override
    public void publish(T event) {
        log.trace("Publishing event to Kafka topic '{}': {}", topic, event);
        kafkaTemplate.send(topic, event);
        log.info("Published event to Kafka topic '{}': {}", topic, event);
    }
}
