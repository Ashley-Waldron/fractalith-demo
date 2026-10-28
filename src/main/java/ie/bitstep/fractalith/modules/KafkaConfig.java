package ie.bitstep.fractalith.modules;

import ie.bitstep.fractalith.modules.order.api.OrderPlacedEvent;
import ie.bitstep.fractalith.modules.payment.api.PaymentProcessedEvent;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

@EnableKafka
@Configuration
public class KafkaConfig {

    @Bean
    public ProducerFactory<String, OrderPlacedEvent> orderPlacedProducerFactory(
            @Value("${spring.kafka.bootstrap-servers:localhost:9092}") String bootstrapServers) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, OrderPlacedEvent> orderPlacedKafkaTemplate(
            ProducerFactory<String, OrderPlacedEvent> orderPlacedProducerFactory) {
        return new KafkaTemplate<>(orderPlacedProducerFactory);
    }

    @Bean
    public ConsumerFactory<String, OrderPlacedEvent> orderPlacedConsumerFactory(
            @Value("${spring.kafka.bootstrap-servers:localhost:9092}") String bootstrapServers) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "fractalith");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new JsonDeserializer<>(OrderPlacedEvent.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderPlacedEvent> kafkaListenerContainerFactory(
            ConsumerFactory<String, OrderPlacedEvent> orderPlacedConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, OrderPlacedEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(orderPlacedConsumerFactory);
        return factory;
    }

    @Bean
    public ProducerFactory<String, PaymentProcessedEvent> paymentProcessedProducerFactory(
            @Value("${spring.kafka.bootstrap-servers:localhost:9092}") String bootstrapServers) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, PaymentProcessedEvent> paymentProcessedKafkaTemplate(
            ProducerFactory<String, PaymentProcessedEvent> paymentProcessedProducerFactory) {
        return new KafkaTemplate<>(paymentProcessedProducerFactory);
    }

    @Bean
    public ConsumerFactory<String, PaymentProcessedEvent> paymentProcessedConsumerFactory(
            @Value("${spring.kafka.bootstrap-servers:localhost:9092}") String bootstrapServers) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "fractalith");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new JsonDeserializer<>(PaymentProcessedEvent.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentProcessedEvent> paymentProcessedKafkaListenerContainerFactory(
            ConsumerFactory<String, PaymentProcessedEvent> paymentProcessedConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, PaymentProcessedEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(paymentProcessedConsumerFactory);
        return factory;
    }
}
