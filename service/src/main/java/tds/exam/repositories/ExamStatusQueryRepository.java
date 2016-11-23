package tds.exam.repositories;

import tds.exam.ExamStatusCode;

/**
 * Handles finding exam statuses
 */
public interface ExamStatusQueryRepository {
    /**
     * Find the {@link tds.exam.ExamStatusCode} with the code
     * @param code code for lookup
     * @return {@link tds.exam.ExamStatusCode} or empty if not found
     * @throws org.springframework.dao.EmptyResultDataAccessException if the status code cannot be found
     */
    ExamStatusCode findExamStatusCode(String code);
}
