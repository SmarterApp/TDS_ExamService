package tds.exam.services;

import java.util.Optional;

import tds.config.ClientSystemFlag;

/**
 * A service that handles configuration interaction.
 */
public interface ConfigService {
    /**
     * Finds the {@link tds.config.ClientSystemFlag} for client
     *
     * @param clientName  environment's client name
     * @param auditObject type of system flag
     * @return {@link tds.config.ClientSystemFlag} if found otherwise empty
     */
    Optional<ClientSystemFlag> findClientSystemFlag(final String clientName, final String auditObject);
}
