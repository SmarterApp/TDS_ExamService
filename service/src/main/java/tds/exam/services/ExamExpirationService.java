package tds.exam.services;

import tds.exam.ExpiredExamResponse;

/**
 * Expires exams
 */
public interface ExamExpirationService {
    /**
     * Expire all exams that fit criteria
     * @param clientName the client name associated with the exam
     *
     * @return {@link tds.exam.ExpiredExamResponse}
     */
    ExpiredExamResponse expireExams(final String clientName);
}
