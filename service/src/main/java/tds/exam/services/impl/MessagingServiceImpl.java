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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tds.exam.services.MessagingService;
import tds.trt.model.TDSReport;

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

    @Override
    public void sendExamRescore(final UUID examId, final TDSReport testResults) {
        final String stringId = examId.toString();
        final CorrelationData correlationData = new CorrelationData("exam.rescore-" + stringId);
        this.rabbitTemplate.convertAndSend(TOPIC_EXCHANGE, TOPIC_EXAM_COMPLETED, testResults, correlationData);
    }
}
