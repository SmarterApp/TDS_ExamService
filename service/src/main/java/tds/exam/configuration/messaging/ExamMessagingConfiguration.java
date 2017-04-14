package tds.exam.configuration.messaging;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tds.exam.utils.LoggingAmqpConfirmationCallback;

import javax.annotation.PostConstruct;

import static tds.exam.ExamTopics.TOPIC_EXCHANGE;

/**
 * This configuration is responsible for initializing AMQP (RabbitMQ)
 */
@Configuration
public class ExamMessagingConfiguration {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(TOPIC_EXCHANGE, true, false);
    }

    /**
     * Add a logging confirmation callback that will log errors if an exam could not be submitted.
     */
    @PostConstruct
    public void rabbitTemplate() {
        rabbitTemplate.setConfirmCallback(new LoggingAmqpConfirmationCallback());
    }
}
