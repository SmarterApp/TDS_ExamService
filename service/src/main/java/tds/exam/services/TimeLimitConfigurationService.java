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

import java.util.Optional;

import tds.config.TimeLimitConfiguration;

/**
 * Handles interaction with time limit configuration data.
 */
public interface TimeLimitConfigurationService {
    /**
     * Get {@link TimeLimitConfiguration} for the client and assessment
     *
     * @param clientName The name of the client that owns the {@link TimeLimitConfiguration}
     * @param assessmentId The id of the assessment that might have specific time limit settings.
     * @return An optional containing the {@link TimeLimitConfiguration} for the client name; otherwise empty.
     */
    Optional<TimeLimitConfiguration> findTimeLimitConfiguration(final String clientName, final String assessmentId);

    /**
     * Get {@link TimeLimitConfiguration} for the client and assessment
     *
     * @param clientName The name of the client that owns the {@link TimeLimitConfiguration}
     * @return An optional containing the {@link TimeLimitConfiguration} for the client name; otherwise empty.
     */
    Optional<TimeLimitConfiguration> findTimeLimitConfiguration(final String clientName);
}
