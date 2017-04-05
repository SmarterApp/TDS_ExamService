package tds.exam.services;

import com.google.common.base.Optional;
import org.joda.time.Instant;

import java.util.UUID;

/**
 * A service for fetching exam status dates
 */
public interface ExamStatusService {
    /**
     * Finds the {@link org.joda.time.Instant} the specified exam was the in the given examStatus
     *
     * @param examId     The id of the exam to check the status for
     * @param examStatus The status to check for
     * @return The {@link org.joda.time.Instant} the exam was last set to the specified status
     */
    Optional<Instant> findRecentTimeAtStatus(final UUID examId, final String examStatus);
}
