package tds.exam.services;

import java.util.List;
import java.util.Optional;

import tds.config.Accommodation;
import tds.config.AssessmentWindow;
import tds.config.ClientSystemFlag;
import tds.session.ExternalSessionConfiguration;

/**
 * A service that handles configuration interaction.
 */
public interface ConfigService {
    /**
     * Finds the assessment windows for an exam
     *
     * @param clientName    environment's client name
     * @param assessmentId  the assessment id for the assessment
     * @param sessionType   exam session type
     * @param studentId     identifier to the student
     * @param configuration {@link tds.session.ExternalSessionConfiguration} for the environment
     * @return array of {@link tds.config.AssessmentWindow}
     */
    List<AssessmentWindow> findAssessmentWindows(String clientName, String assessmentId, int sessionType, long studentId, ExternalSessionConfiguration configuration);

    /**
     * Finds the {@link tds.config.ClientSystemFlag} for client
     *
     * @param clientName  environment's client name
     * @param auditObject type of system flag
     * @return {@link tds.config.ClientSystemFlag} if found otherwise empty
     */
    Optional<ClientSystemFlag> findClientSystemFlag(final String clientName, final String auditObject);

    /**
     * Finds the {@link tds.config.Accommodation} for the assessment key
     *
     * @param clientName the client name associated with the assessment
     * @param assessmentKey the assessment key
     * @return {@link tds.config.Accommodation} for the assessment key
     */
    List<Accommodation> findAssessmentAccommodationsByAssessmentKey(final String clientName, final String assessmentKey);

    /**
     * Finds the {@link tds.config.Accommodation} for the assessment id
     * @param clientName the client name associated with the assessment
     * @param assessmentId the assessment id
     * @return {@link tds.config.Accommodation} for the assessment id
     */
    List<Accommodation> findAssessmentAccommodationsByAssessmentId(final String clientName, final String assessmentId);
}
