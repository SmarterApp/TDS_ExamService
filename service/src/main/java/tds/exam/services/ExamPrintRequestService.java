/***************************************************************************************************
 * Copyright 2017 Regents of the University of California. Licensed under the Educational
 * Community License, Version 2.0 (the “license”); you may not use this file except in
 * compliance with the License. You may obtain a copy of the license at
 *
 * https://opensource.org/licenses/ECL-2.0
 *
 * Unless required under applicable law or agreed to in writing, software distributed under the
 * License is distributed in an “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for specific language governing permissions
 * and limitations under the license.
 **************************************************************************************************/

package tds.exam.services;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import tds.exam.ExamPrintRequest;
import tds.exam.ExamPrintRequestStatus;
import tds.exam.ExpandableExamPrintRequest;

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
     * Updates and fetches the {@link tds.exam.ExamPrintRequest} with the provided reason.
     *
     * @param status The {@link tds.exam.ExamPrintRequestStatus} of the request - either approved or denied
     * @param id     The id of the {@link tds.exam.ExamPrintRequest} being denied
     * @param reason The reason for the denial of the request
     * @return The approved/denied {@link tds.exam.ExamPrintRequest}
     */
    Optional<ExamPrintRequest> updateAndGetRequest(final ExamPrintRequestStatus status, final UUID id, final String reason);

    /**
     * Updates and fetches and {@link tds.exam.ExpandableExamPrintRequest} with the specified additional parameters
     *
     * @param status               The {@link tds.exam.ExamPrintRequestStatus} of the request - either approved or denied
     * @param id                   The id of the {@link tds.exam.ExamPrintRequest} being denied
     * @param reason               The reason for the denial of the request
     * @param expandableProperties Optional expandable parameters for retrieving additional data
     * @return The approved/denied {@link tds.exam.ExamPrintRequest}
     */
    Optional<ExpandableExamPrintRequest> updateAndGetRequest(final ExamPrintRequestStatus status, final UUID id, final String reason,
                                                             final String... expandableProperties);

    /**
     * Retrieves a list of approved requests for the session.
     *
     * @param sessionId The session id of the approved {@link tds.exam.ExamPrintRequest}s
     * @return A {@link List<tds.exam.ExamPrintRequest>} that have been approved
     */
    List<ExamPrintRequest> findApprovedRequests(final UUID sessionId);
}
