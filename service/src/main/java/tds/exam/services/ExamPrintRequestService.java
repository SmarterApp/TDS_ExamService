package tds.exam.services;

import java.util.Map;
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
}
