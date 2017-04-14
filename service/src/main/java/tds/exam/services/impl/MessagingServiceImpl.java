package tds.exam.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tds.exam.services.MessagingService;

import java.util.UUID;

import static tds.exam.ExamTopics.TOPIC_EXAM_COMPLETED;
import static tds.exam.ExamTopics.TOPIC_EXCHANGE;

/**
 * Default implementation of a MessagingService using a RabbitTemplate.
 */
@Service
public class MessagingServiceImpl implements MessagingService {
    private final static Logger LOG = LoggerFactory.getLogger(MessagingServiceImpl.class);

    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public MessagingServiceImpl(final RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void sendExamCompletion(final UUID examId) {
        final String stringId = examId.toString();
        final CorrelationData correlationData = new CorrelationData("exam.completion-" + stringId);
        this.rabbitTemplate.convertAndSend(TOPIC_EXCHANGE, TOPIC_EXAM_COMPLETED, stringId, correlationData);
    }
}
