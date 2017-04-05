package tds.exam.repositories;

import com.google.common.base.Optional;
import org.joda.time.Instant;

import java.util.UUID;

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
    ExamStatusCode findExamStatusCode(final String code);

    /**
     * Finds the {@link org.joda.time.Instant} the specified exam was the in the given examStatus
     *
     * @param examId     The id of the exam to check the status for
     * @param examStatus The status to check for
     * @return The {@link org.joda.time.Instant} the exam was last set to the specified status
     */
    Optional<Instant> findRecentTimeAtStatus(final UUID examId, final String examStatus);
}
