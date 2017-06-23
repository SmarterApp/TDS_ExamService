/***************************************************************************************************
 * Copyright 2017 Regents of the University of California. Licensed under the Educational
 * Community License, Version 2.0 (the “license”); you may not use this file except in
 * compliance with the License. You may obtain a copy of the license at
 *
 * https://opensource.org/licenses/ECL-2.0
 *
 * Unless required under applicable law or agreed to in writing, software distributed under the
 * License is distributed in an “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for specific language governing permissions
 * and limitations under the license.
 **************************************************************************************************/

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
