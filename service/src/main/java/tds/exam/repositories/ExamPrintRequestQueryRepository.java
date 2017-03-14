package tds.exam.repositories;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import tds.exam.ExamPrintRequest;

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

    /**
     * Retrieves a list of unfulfilled requests. These are request that have been neither approved nor denied.
     *
     * @param examId    The id of the exam for the {@link tds.exam.ExamPrintRequest}s
     * @param sessionId The id of the session for the {@link tds.exam.ExamPrintRequest}s
     * @return The list of the unfulfilled {@link tds.exam.ExamPrintRequest}s
     */
    List<ExamPrintRequest> findUnfulfilledRequests(final UUID examId, final UUID sessionId);

    /**
     * Retrieves an {@link tds.exam.ExamPrintRequest} with the specified id
     *
     * @param id The id of the {@link tds.exam.ExamPrintRequest} to fetch
     * @return The {@link tds.exam.ExamPrintRequest}
     */
    Optional<ExamPrintRequest> findExamPrintRequest(final UUID id);

    /**
     * Retrieves a list of approved requests for the session.
     *
     * @param sessionId The session id of the approved {@link tds.exam.ExamPrintRequest}s
     * @return A {@link List<tds.exam.ExamPrintRequest>} that have been approved
     */
    List<ExamPrintRequest> findApprovedRequests(final UUID sessionId);
}
