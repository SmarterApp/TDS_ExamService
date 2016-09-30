package tds.exam.services;

import tds.config.TimeLimitConfiguration;

import java.util.Optional;

/**
 * Handles interaction with time limit configuration data.
 */
public interface TimeLimitConfigurationService {
    /**
     * Get {@link TimeLimitConfiguration} from the configuration service.
     *
     * @param clientName The name of the client that owns the {@link TimeLimitConfiguration}
     * @param assessmentId The id of the assessment that might have specific time limit settings.
     * @return An optional containing the {@link TimeLimitConfiguration} for the client name; otherwise empty.
     */
    Optional<TimeLimitConfiguration> findTimeLimitConfiguration(String clientName, String assessmentId);
}
