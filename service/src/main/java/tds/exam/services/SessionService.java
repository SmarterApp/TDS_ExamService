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
import java.util.Optional;
import java.util.UUID;

import tds.session.ExternalSessionConfiguration;
import tds.session.PauseSessionResponse;
import tds.session.Session;
import tds.session.SessionAssessment;

/**
 * Handles interaction with session properties
 */
public interface SessionService {
    /**
     * Retrieves a session by id
     *
     * @param sessionId the session id
     * @return optional populated with {@link tds.session.Session session} if found otherwise empty
     */
    Optional<Session> findSessionById(final UUID sessionId);

    /**
     * Retrieves the extern by client name
     *
     * @param clientName the client name for the exam
     * @return optional populated with {@link tds.session.ExternalSessionConfiguration} if found otherwise empty
     */
    Optional<ExternalSessionConfiguration> findExternalSessionConfigurationByClientName(final String clientName);

    /**
     * Pause a {@link Session}
     *
     * @param sessionId The id of the {@link Session} to pauseExam.
     * @param newStatus The new status of the {@link Session}.
     * @return A {@link PauseSessionResponse} indicating the {@link Session} has been paused; otherwise empty.
     */
    Optional<PauseSessionResponse> pause(final UUID sessionId, final String newStatus);

    /**
     * Finds the {@link tds.session.SessionAssessment} for the session id and assessment key
     *
     * @param sessionId     session id
     * @param assessmentKey assessment key
     * @return {@link tds.session.SessionAssessment} if found otherwise empty
     */
    Optional<SessionAssessment> findSessionAssessment(final UUID sessionId, final String assessmentKey);

    /**
     * Finds the {@link tds.session.SessionAssessment}s for the session id
     *
     * @param sessionId session id
     * @return The list of {@link tds.session.SessionAssessment}s for this session
     */
    List<SessionAssessment> findSessionAssessments(final UUID sessionId);

    /**
     * Finds the list of {@link tds.session.Session}s for the specified session ids
     *
     * @param sessionIds The ids of the {@link tds.session.Session}s to find
     * @return A list of sessions for the provided ids
     */
    List<Session> findSessionsByIds(final List<UUID> sessionIds);
}
