package ie.bitstep.fractalith.modules;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import ie.bitstep.fractalith.modules.order.api.OrderPlacedEvent;
import ie.bitstep.fractalith.modules.shop.api.CheckoutRequest;
import ie.bitstep.fractalith.modules.shop.internal.ShopService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.PayloadApplicationEvent;
import org.springframework.modulith.core.ApplicationModules;

@SpringBootTest
class DemoApplicationTests {

	@Autowired
	private ShopService shopService;

	@Autowired
	private ConfigurableApplicationContext applicationContext;

	@Test
	void applicationModulesAreValid() {
		assertThatCode(() -> ApplicationModules.of(DemoApplication.class).verify())
				.doesNotThrowAnyException();
	}

	@Test
	void checkoutTriggersOrderPlacedEvent() throws Exception {
		CountDownLatch latch = new CountDownLatch(1);
		AtomicReference<OrderPlacedEvent> publishedEvent = new AtomicReference<>();
		ApplicationListener<ApplicationEvent> listener = event -> {
			if (event instanceof PayloadApplicationEvent<?> payloadEvent && payloadEvent.getPayload() instanceof OrderPlacedEvent orderPlacedEvent) {
				publishedEvent.set(orderPlacedEvent);
				latch.countDown();
			}
		};
		applicationContext.addApplicationListener(listener);

		String result = shopService.checkout(new CheckoutRequest("customer-1", "sku-1", 2));

		assertThat(result).contains("checked out");
		assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
		assertThat(publishedEvent.get()).isNotNull();
		assertThat(publishedEvent.get().customerId()).isEqualTo("customer-1");
		assertThat(publishedEvent.get().sku()).isEqualTo("sku-1");
		assertThat(publishedEvent.get().quantity()).isEqualTo(2);
	}

}
