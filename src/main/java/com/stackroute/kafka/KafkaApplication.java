package com.stackroute.kafka;

import com.stackroute.kafka.domain.Greetings;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class KafkaApplication {

	@Bean
	public MessageProducer messageProducer() {
		return new MessageProducer();
	}

	@Bean
	public MessageConsumer messageListener() {
		return new MessageConsumer();
	}

	public static void main(String[] args) throws Exception{

		ConfigurableApplicationContext context = SpringApplication.run(KafkaApplication.class, args);

		MessageProducer producer = context.getBean(MessageProducer.class);
		MessageConsumer listener = context.getBean(MessageConsumer.class);
		/*
		 * Sending a Hello World message to topic 'baeldung'.
		 * Must be received by both listeners with group foo
		 * and bar with containerFactory fooKafkaListenerContainerFactory
		 * and barKafkaListenerContainerFactory respectively.
		 * It will also be received by the listener with
		 * headersKafkaListenerContainerFactory as container factory
		 */
		producer.sendMessage("Hello, World!");
		listener.latch.await(10, TimeUnit.SECONDS);

		/*
		 * Sending message to a topic with 5 partition,
		 * each message to a different partition. But as per
		 * listener configuration, only the messages from
		 * partition 0 and 3 will be consumed.
		 */
		for (int i = 0; i < 5; i++) {
			producer.sendMessageToPartion("Hello To Partitioned Topic!", i);
		}
		listener.partitionLatch.await(10, TimeUnit.SECONDS);

		/*
		 * Sending message to 'filtered' topic. As per listener
		 * configuration,  all messages with char sequence
		 * 'World' will be discarded.
		 */
		producer.sendMessageToFiltered("Hello This is KAFKA!");
		producer.sendMessageToFiltered("Hello World!");
		listener.filterLatch.await(10, TimeUnit.SECONDS);

		/*
		 * Sending message to 'greeting' topic. This will send
		 * and recieved a java object with the help of
		 * greetingKafkaListenerContainerFactory.
		 */
		producer.sendGreetingMessage(new Greetings("Greetings", "World!"));
		listener.greetingLatch.await(10, TimeUnit.SECONDS);

		context.close();
	}

}
