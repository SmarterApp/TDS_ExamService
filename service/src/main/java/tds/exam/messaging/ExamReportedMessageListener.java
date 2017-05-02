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
            log.error("Unable to update exam status for the exam: " + e.getMessage());
        }
    }
}
