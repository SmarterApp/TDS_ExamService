package tds.exam.services;

import tds.exam.ExpireExamsResult;

/**
 * Expires exams
 */
public interface ExamExpirationService {
    /**
     * Expire all exams that fit criteria
     * @return the {@link tds.exam.ExpireExamsResult}
     */
    ExpireExamsResult expireExams();
}
