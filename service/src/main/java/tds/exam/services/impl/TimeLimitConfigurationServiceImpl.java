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

package tds.exam.services.impl;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

import tds.config.TimeLimitConfiguration;
import tds.exam.configuration.ExamServiceProperties;
import tds.exam.services.TimeLimitConfigurationService;

import static tds.exam.configuration.SupportApplicationConfiguration.CONFIG_APP_CONTEXT;

@Service
class TimeLimitConfigurationServiceImpl implements TimeLimitConfigurationService {

    private final RestTemplate restTemplate;
    private final ExamServiceProperties examServiceProperties;

    TimeLimitConfigurationServiceImpl(final RestTemplate restTemplate, final ExamServiceProperties examServiceProperties) {
        this.restTemplate = restTemplate;
        this.examServiceProperties = examServiceProperties;
    }

    @Override
    public Optional<TimeLimitConfiguration> findTimeLimitConfiguration(final String clientName, final String assessmentId) {
        UriComponentsBuilder uriBuilder =
                UriComponentsBuilder
                    .fromHttpUrl(String.format("%s/%s/time-limits/%s/%s",
                        examServiceProperties.getConfigUrl(),
                        CONFIG_APP_CONTEXT,
                        clientName,
                        assessmentId));

        return getTimeLimitConfiguration(uriBuilder);
    }

    @Override
    public Optional<TimeLimitConfiguration> findTimeLimitConfiguration(final String clientName) {
        UriComponentsBuilder uriBuilder =
            UriComponentsBuilder
                .fromHttpUrl(String.format("%s/%s/time-limits/%s",
                    examServiceProperties.getConfigUrl(),
                    CONFIG_APP_CONTEXT,
                    clientName));

        return getTimeLimitConfiguration(uriBuilder);
    }

    private Optional<TimeLimitConfiguration> getTimeLimitConfiguration(final UriComponentsBuilder uriBuilder) {
        Optional<TimeLimitConfiguration> maybeTimeLimitConfig = Optional.empty();
        try {
            final TimeLimitConfiguration timeLimitConfiguration =
                restTemplate.getForObject(uriBuilder.build().toUri(), TimeLimitConfiguration.class);
            maybeTimeLimitConfig = Optional.of(timeLimitConfiguration);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() != HttpStatus.NOT_FOUND) {
                throw e;
            }
        }
        return maybeTimeLimitConfig;
    }
}
