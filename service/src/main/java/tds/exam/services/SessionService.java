package tds.exam.services;

import java.util.Optional;
import java.util.UUID;

import tds.session.Extern;
import tds.session.Session;

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
    Optional<Session> getSession(UUID sessionId);

    /**
     * Retrieves the extern by client name
     *
     * @param clientName the client name for the exam
     * @return optional populated with {@link tds.session.Extern extern} if found otherwise empty
     */
    Optional<Extern> getExternByClientName(String clientName);
}
