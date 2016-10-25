package tds.exam.services;

import java.util.Optional;

import tds.config.AssessmentWindow;
import tds.config.AssessmentWindowParameters;
import tds.config.ClientTestProperty;

/**
 * A service that handles configuration interaction.
 */
public interface ConfigService {

    /**
     * Retrieves the {@link ClientTestProperty} for the given client and assessment
     *
     * @param clientName   client name of the environment
     * @param assessmentId assessment to retrieve {@link ClientTestProperty} for
     * @return A set of client and assessment-specific properties.
     */
    Optional<ClientTestProperty> findClientTestProperty(final String clientName, final String assessmentId);

    /**
     * Finds the associated {@link tds.config.AssessmentWindow} based on the {@link tds.config.AssessmentWindowParameters}
     *
     * @param parameters the {@link tds.config.AssessmentWindowParameters} to use to find the windows
     * @return array of {@link tds.config.AssessmentWindow}
     */
    AssessmentWindow[] findAssessmentWindows(AssessmentWindowParameters parameters);
}
