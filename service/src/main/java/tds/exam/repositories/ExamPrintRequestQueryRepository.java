package tds.exam.repositories;

import java.util.Map;
import java.util.UUID;

/**
 * A repository for reading {@link tds.exam.ExamPrintRequest} data
 */
public interface ExamPrintRequestQueryRepository {

    /**
     * Retrieves a map of exam ids to their respective count of unfulfilled {@link tds.exam.ExamPrintRequest}
     *
     * @param sessionId The session of the exams
     * @param examIds   The list of exam ids for each {@link tds.exam.ExamPrintRequest}
     * @return A map of exam ids to their request count
     */
    Map<UUID, Integer> findRequestCountsForExamIds(final UUID sessionId, final UUID... examIds);
}
