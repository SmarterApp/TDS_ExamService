package tds.exam.services.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static tds.exam.ExamTopics.TOPIC_EXAM_COMPLETED;
import static tds.exam.ExamTopics.TOPIC_EXCHANGE;

@RunWith(MockitoJUnitRunner.class)
public class MessagingServiceImplTest {

    @Mock
    private RabbitTemplate mockRabbitTemplate;

    private MessagingServiceImpl messagingService;

    @Before
    public void setup() {
        messagingService = new MessagingServiceImpl(mockRabbitTemplate);
    }

    @Test
    public void itShouldSubmitACompletedExamToTheExpectedTopic() {
        final UUID examId = UUID.randomUUID();
        messagingService.sendExamCompletion(examId);

        final ArgumentCaptor<CorrelationData> correlationDataCaptor = ArgumentCaptor.forClass(CorrelationData.class);
        verify(mockRabbitTemplate).convertAndSend(eq(TOPIC_EXCHANGE), eq(TOPIC_EXAM_COMPLETED), eq(examId.toString()), correlationDataCaptor.capture());
        assertThat(correlationDataCaptor.getValue().getId()).isEqualTo("exam.completion-" + examId.toString());
    }

}