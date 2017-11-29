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

import java.util.Collection;
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
    String getFormattedMessage(final String clientName, final String context, final String messageKey, final Object... replacements);

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
    String getFormattedMessage(final String clientName, final String context, final String messageKey, final String languageCode, final Object... replacements);

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
    String getFormattedMessage(final String clientName, final String context, final String messageKey, final String languageCode, final String subject, final String grade, final Object... replacements);

    /**
     * Finds assessment ids that have force complete enabled
     *
     * @param clientName the client name
     * @return collection of assessment ids
     */
    Collection<String> findForceCompleteAssessmentIds(final String clientName);
}
