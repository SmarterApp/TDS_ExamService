package tds.exam.services;

import java.util.HashMap;
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

    /**
     * Get the message string for this client with the default language.
     *
     * @param clientName   The client name
     * @param context      The context for the message key
     * @param messageKey   The message key found in the {@code configs.tds_coremessageobject} table in the {@code appkey} column
     * @param replacements Key value pairs that are used to replace the data in the templated message
     * @return The message string with placeholders included
     */
    String getFormattedMessage(String clientName, String context, String messageKey, Object... replacements);

    /**
     * Get the message string for this client that is translated for the language specified.
     *
     * @param clientName   The client name
     * @param context      The context for the message key
     * @param messageKey   The message key found in the {@code configs.tds_coremessageobject} table in the {@code appkey} column
     * @param languageCode The language code to translate the message into
     * @param replacements Key value pairs that are used to replace the data in the templated message
     * @return The message string with placeholders included
     */
    String getFormattedMessage(String clientName, String context, String messageKey, String languageCode, Object... replacements);

    /**
     * Get the message string for this client that is translated for the language specified, filtering by grade and subject
     *
     * @param clientName   The client name
     * @param context      The context for the message key
     * @param messageKey   The message key found in the {@code configs.tds_coremessageobject} table in the {@code appkey} column
     * @param languageCode The language code to translate the message into
     * @param subject      A subject code used to find a more specific message.  NULL will match on all
     * @param grade        A grade level used to find a more specific message.  NULL will match on all
     * @param replacements Key value pairs that are used to replace the data in the templated message
     * @return The message string with placeholders included
     */
    String getFormattedMessage(String clientName, String context, String messageKey, String languageCode, String subject, String grade, Object... replacements);
}
