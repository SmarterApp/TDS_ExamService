package tds.exam.configuration.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

import tds.exam.messaging.ExamReportedMessageListener;
import tds.exam.utils.LoggingAmqpConfirmationCallback;

import static tds.exam.ExamTopics.TOPIC_EXAM_REPORTED;
import static tds.exam.ExamTopics.TOPIC_EXCHANGE;

/**
 * This configuration is responsible for initializing AMQP (RabbitMQ)
 */
@Configuration
public class ExamMessagingConfiguration {
    private final static String QUEUE_EXAM_REPORTED = "exam_reported_results_transmitter_queue";

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(TOPIC_EXCHANGE, true, false);
    }

    @Bean
    public Queue examReportedQueue() {
        return new Queue(QUEUE_EXAM_REPORTED, true);
    }

    @Bean
    public Binding examReportedBinding(@Qualifier("examReportedQueue") final Queue queue,
                                       @Qualifier("exchange") final TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(TOPIC_EXAM_REPORTED);
    }

    @Bean
    public SimpleMessageListenerContainer examReportedListenerContainer(final ConnectionFactory connectionFactory,
                                                                        final ExamReportedMessageListener listener) {
        final SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(QUEUE_EXAM_REPORTED);
        container.setMessageListener(new MessageListenerAdapter(listener, "handleMessage"));
        return container;
    }

    /**
     * Add a logging confirmation callback that will log errors if an exam could not be submitted.
     */
    @PostConstruct
    public void rabbitTemplate() {
        rabbitTemplate.setConfirmCallback(new LoggingAmqpConfirmationCallback());
    }
}
