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

import tds.trt.model.TDSReport;

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

    @Test
    public void itShouldSubmitAExamToRescoreToTheExpectedTopic() {
        final UUID examId = UUID.randomUUID();
        final UUID jobId = UUID.randomUUID();
        final TDSReport tdsReport = new TDSReport();
        messagingService.sendExamRescore(examId, jobId, tdsReport);

        final ArgumentCaptor<CorrelationData> correlationDataCaptor = ArgumentCaptor.forClass(CorrelationData.class);
        verify(mockRabbitTemplate).convertAndSend(eq(TOPIC_EXCHANGE), eq(TOPIC_EXAM_COMPLETED), eq(tdsReport), correlationDataCaptor.capture());
        assertThat(correlationDataCaptor.getValue().getId()).isEqualTo("exam.rescore-" + examId.toString());
    }

}