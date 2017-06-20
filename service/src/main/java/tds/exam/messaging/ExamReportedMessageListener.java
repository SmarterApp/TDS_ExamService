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

package tds.exam.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

import tds.common.web.exceptions.NotFoundException;
import tds.exam.ExamStatusCode;
import tds.exam.services.ExamService;

@Component
public class ExamReportedMessageListener {
    private static final Logger log = LoggerFactory.getLogger(ExamReportedMessageListener.class);
    private final ExamService examService;

    public ExamReportedMessageListener(final ExamService examService) {
        this.examService = examService;
    }

    /**
     * Handle the reporting of an exam to TIS.
     * This method is an external entry-point called when we receive a new exam reported
     * message from our message broker.
     *
     * @param examId The completed exam id
     */
    public void handleMessage(final String examId) {
        log.debug("Received reported exam notification for id: {}", examId);
        try {
            examService.updateExamStatus(UUID.fromString(examId), new ExamStatusCode(ExamStatusCode.STATUS_REPORTED));
        } catch (NotFoundException e) {
            // If an examId is provided that is not found on the system, rabbitmq may potentially continue to attempt
            // to process the message endlessly. If this exception occurs, we can simply ditch the message and log it.
            log.error("Unable to update exam status for the exam: " + e.getMessage());
        }
    }
}
