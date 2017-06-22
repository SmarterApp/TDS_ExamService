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

    /**
     * Retrieves a count of unfulfilled requests for a given exam and item position
     *
     * @param examId       The id of the exam for the {@link tds.exam.ExamPrintRequest}s
     * @param itemPosition The item position of the request to check for
     * @param pagePosition The page position of the request to check for
     * @return
     */
    int findCountOfUnfulfilledRequestsForExamAndItemPosition(final UUID examId, final int itemPosition, final int pagePosition);
}
