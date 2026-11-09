package ie.bitstep.fractalith.modules;

import ie.bitstep.fractalith.common.api.ConditionalOnPropertyContains;
import ie.bitstep.fractalith.common.api.EventPublisher;
import ie.bitstep.fractalith.common.api.SpringEventPublisher;
import ie.bitstep.fractalith.modules.inventory.api.InventoryHeldEvent;
import ie.bitstep.fractalith.modules.order.api.OrderDeliveredEvent;
import ie.bitstep.fractalith.modules.order.api.OrderPlacedEvent;
import ie.bitstep.fractalith.modules.order.api.OrderReadyForShipmentEvent;
import ie.bitstep.fractalith.modules.order.api.OrderShippedEvent;
import ie.bitstep.fractalith.modules.order.api.CreateOrderRequest;
import ie.bitstep.fractalith.modules.payment.api.PaymentProcessedEvent;
import java.util.concurrent.Executor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@EnableAsync
@SpringBootApplication(scanBasePackages = {"ie.bitstep.fractalith.modules", "ie.bitstep.fractalith.common"})
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Bean
	@ConditionalOnProperty(prefix = "modules.order", name = "enabled", havingValue = "true", matchIfMissing = true)
	@ConditionalOnPropertyContains(prefix = "modules.order.publishers", name = "order-placed", value = "local")
	EventPublisher<OrderPlacedEvent> orderPlacedEventPublisher(ApplicationEventPublisher delegate) {
		return new SpringEventPublisher<>(delegate);
	}

	@Bean
	@ConditionalOnProperty(prefix = "modules.shop", name = "enabled", havingValue = "true", matchIfMissing = true)
	@ConditionalOnPropertyContains(prefix = "modules.shop.publishers", name = "create-order-request", value = "local")
	EventPublisher<CreateOrderRequest> orderRequestEventPublisher(ApplicationEventPublisher delegate) {
		return new SpringEventPublisher<>(delegate);
	}

	@Bean
	@ConditionalOnProperty(prefix = "modules.payment", name = "enabled", havingValue = "true", matchIfMissing = true)
	@ConditionalOnPropertyContains(prefix = "modules.payment.publishers", name = "payment-processed", value = "local")
	EventPublisher<PaymentProcessedEvent> paymentProcessedEventPublisher(ApplicationEventPublisher delegate) {
		return new SpringEventPublisher<>(delegate);
	}

	@Bean
	@ConditionalOnProperty(prefix = "modules.inventory", name = "enabled", havingValue = "true")
	@ConditionalOnPropertyContains(prefix = "modules.inventory.publishers", name = "inventory-held", value = "local")
	EventPublisher<InventoryHeldEvent> inventoryHeldEventPublisher(ApplicationEventPublisher delegate) {
		return new SpringEventPublisher<>(delegate);
	}

	@Bean
	@ConditionalOnProperty(prefix = "modules.order", name = "enabled", havingValue = "true")
	@ConditionalOnPropertyContains(prefix = "modules.order.publishers", name = "order-ready-for-shipment", value = "local")
	EventPublisher<OrderReadyForShipmentEvent> orderReadyForShipmentEventPublisher(ApplicationEventPublisher delegate) {
		return new SpringEventPublisher<>(delegate);
	}

	@Bean
	@ConditionalOnProperty(prefix = "modules.shipping", name = "enabled", havingValue = "true")
	@ConditionalOnPropertyContains(prefix = "modules.shipping.publishers", name = "order-shipped", value = "local")
	EventPublisher<OrderShippedEvent> orderShippedEventPublisher(ApplicationEventPublisher delegate) {
		return new SpringEventPublisher<>(delegate);
	}

	@Bean
	@ConditionalOnProperty(prefix = "modules.shipping", name = "enabled", havingValue = "true")
	@ConditionalOnPropertyContains(prefix = "modules.shipping.publishers", name = "order-delivered", value = "local")
	EventPublisher<OrderDeliveredEvent> orderDeliveredEventPublisher(ApplicationEventPublisher delegate) {
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
