package tds.exam.services;

import java.util.Collection;

import tds.exam.ExpiredExamInformation;

/**
 * Expires exams
 */
public interface ExamExpirationService {
    /**
     * Expire all exams that fit criteria
     * @param clientName the client name associated with the exam
     *
     * @return collection of {@link tds.exam.ExpiredExamInformation}
     */
    Collection<ExpiredExamInformation> expireExams(final String clientName);
}
