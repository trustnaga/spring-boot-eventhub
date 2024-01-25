package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.azure.spring.messaging.checkpoint.Checkpointer;
import com.azure.spring.messaging.eventhubs.support.EventHubsHeaders;
import java.util.function.Consumer;
import org.springframework.messaging.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static com.azure.spring.messaging.AzureHeaders.CHECKPOINTER;


@SpringBootApplication
public class DemoApplication {
	private static final Logger LOGGER = LoggerFactory.getLogger(DemoApplication.class);
	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}
	@Bean
    public Consumer<Message<String>> consume() {
        return message->{
            Checkpointer checkpointer = (Checkpointer) message.getHeaders().get(CHECKPOINTER);
            LOGGER.info("New message received: '{}', partition key: {}, sequence number: {}, offset: {}, enqueued "
                    +"time: {}",
                message.getPayload(),
                message.getHeaders().get(EventHubsHeaders.PARTITION_KEY),
                message.getHeaders().get(EventHubsHeaders.SEQUENCE_NUMBER),
                message.getHeaders().get(EventHubsHeaders.OFFSET),
                message.getHeaders().get(EventHubsHeaders.ENQUEUED_TIME)
            );
            checkpointer.success()
                        .doOnSuccess(success->LOGGER.info("Message '{}' successfully checkpointed",
                            message.getPayload()))
                        .doOnError(error->LOGGER.error("Exception found", error))
                        .block();
        };
    }

}
