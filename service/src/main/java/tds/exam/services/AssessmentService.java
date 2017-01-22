package tds.exam.services;

import java.util.List;
import java.util.Optional;

import tds.accommodation.Accommodation;
import tds.assessment.Assessment;
import tds.assessment.AssessmentWindow;
import tds.session.ExternalSessionConfiguration;

/**
 * Service handles assessment interaction
 */
public interface AssessmentService {
    /**
     * Finds the {@link tds.assessment.Assessment}
     *
     * @param clientName The name of the client (e.g. SBAC or SBAC_PT)
     * @param key unique key for the assessment
     * @return {@link tds.assessment.Assessment the assessment}
     */
    Optional<Assessment> findAssessment(String clientName, String key);

    /**
     * Finds the assessment windows for an exam
     *
     * @param clientName    environment's client name
     * @param assessmentId  the assessment id for the assessment
     * @param studentId     identifier to the student
     * @param configuration {@link tds.session.ExternalSessionConfiguration} for the environment
     * @return array of {@link tds.assessment.AssessmentWindow}
     */
    List<AssessmentWindow> findAssessmentWindows(String clientName, String assessmentId, long studentId, ExternalSessionConfiguration configuration);

    /**
     * Finds the {@link tds.accommodation.Accommodation} for the assessment key
     *
     * @param clientName    the client name associated with the assessment
     * @param assessmentKey the assessment key
     * @return {@link tds.accommodation.Accommodation} for the assessment key
     */
    List<Accommodation> findAssessmentAccommodationsByAssessmentKey(final String clientName, final String assessmentKey);

    /**
     * Finds the {@link tds.accommodation.Accommodation} for the assessment id
     *
     * @param clientName   the client name associated with the assessment
     * @param assessmentId the assessment id
     * @return {@link tds.accommodation.Accommodation} for the assessment id
     */
    List<Accommodation> findAssessmentAccommodationsByAssessmentId(final String clientName, final String assessmentId);

}
