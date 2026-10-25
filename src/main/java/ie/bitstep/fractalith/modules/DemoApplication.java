package ie.bitstep.fractalith.modules;

import ie.bitstep.fractalith.common.api.EventPublisher;
import ie.bitstep.fractalith.common.api.SpringEventPublisher;
import ie.bitstep.fractalith.modules.order.api.OrderPlacedEvent;
import ie.bitstep.fractalith.modules.order.api.CreateOrderRequest;
import java.util.concurrent.Executor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@EnableAsync
@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Bean
	@ConditionalOnProperty(prefix = "order", name = "event.type", havingValue = "local", matchIfMissing = true)
	EventPublisher<OrderPlacedEvent> orderPlacedEventPublisher(ApplicationEventPublisher delegate) {
		return new SpringEventPublisher<>(delegate);
	}

	@Bean
	@ConditionalOnProperty(prefix = "order", name = "event.type", havingValue = "local", matchIfMissing = true)
	EventPublisher<CreateOrderRequest> orderRequestEventPublisher(ApplicationEventPublisher delegate) {
		return new SpringEventPublisher<>(delegate);
	}

	@Bean(name = "applicationTaskExecutor")
	Executor applicationTaskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(2);
		executor.setMaxPoolSize(4);
		executor.setQueueCapacity(100);
		executor.setThreadNamePrefix("fractalith-");
		executor.initialize();
		return executor;
	}

}
