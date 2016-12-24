package tds.exam.services;

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
    Optional<Session> findSessionById(UUID sessionId);

    /**
     * Retrieves the extern by client name
     *
     * @param clientName the client name for the exam
     * @return optional populated with {@link tds.session.ExternalSessionConfiguration} if found otherwise empty
     */
    Optional<ExternalSessionConfiguration> findExternalSessionConfigurationByClientName(String clientName);

    /**
     * Pause a {@link Session}
     *
     * @param sessionId The id of the {@link Session} to pauseExam.
     * @param newStatus The new status of the {@link Session}.
     * @return A {@link PauseSessionResponse} indicating the {@link Session} has been paused; otherwise empty.
     */
    Optional<PauseSessionResponse> pause(UUID sessionId, String newStatus);

    /**
     * Finds the {@link tds.session.SessionAssessment} for the session id and assessment key
     *
     * @param sessionId     session id
     * @param assessmentKey assessment key
     * @return {@link tds.session.SessionAssessment} if found otherwise empty
     */
    Optional<SessionAssessment> findSessionAssessment(UUID sessionId, String assessmentKey);
}
