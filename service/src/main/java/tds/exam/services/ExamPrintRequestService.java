package tds.exam.services;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import tds.exam.ExamPrintRequest;

/**
 * Service used to create, read, and update exam print and emboss requests
 */
public interface ExamPrintRequestService {

    /**
     * Creates an exam print or emboss request
     *
     * @param examPrintRequest an object containing exam print request information
     */
    void insert(final ExamPrintRequest examPrintRequest);

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
     * Denies the {@link tds.exam.ExamPrintRequest} with the provided reason.
     *
     * @param id     The id of the {@link tds.exam.ExamPrintRequest} being denied
     * @param reason The reason for the denial of the request
     */
    void denyRequest(final UUID id, final String reason);

    /**
     * Fetches and marks as approved the specified {@link tds.exam.ExamPrintRequest}
     *
     * @param id the identifier of the {@link tds.exam.ExamPrintRequest} to approve
     * @return The approved {@link tds.exam.ExamPrintRequest}
     */
    Optional<ExamPrintRequest> findAndApprovePrintRequest(final UUID id);

    /**
     * Retrieves a list of approved requests for the session.
     *
     * @param sessionId The session id of the approved {@link tds.exam.ExamPrintRequest}s
     * @return A {@link List<tds.exam.ExamPrintRequest>} that have been approved
     */
    List<ExamPrintRequest> findApprovedRequests(final UUID sessionId);
}
