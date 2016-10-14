package tds.exam.services;

import tds.config.ClientTestProperty;

import java.util.Optional;

/**
 * A service that handles configuration interaction.
 */
public interface ConfigService {

    /**
     * Retrieves the {@link ClientTestProperty} for the given client and assessment
     *
     * @param clientName    client name of the environment
     * @param assessmentId  assessment to retrieve {@link ClientTestProperty} for
     * @return  A set of client and assessment-specific properties.
     */
    Optional<ClientTestProperty> findClientTestProperty(final String clientName, final String assessmentId);
}
