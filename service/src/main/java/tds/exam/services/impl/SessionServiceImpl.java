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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import tds.common.cache.CacheType;
import tds.exam.configuration.ExamServiceProperties;
import tds.exam.services.SessionService;
import tds.session.ExternalSessionConfiguration;
import tds.session.PauseSessionResponse;
import tds.session.Session;
import tds.session.SessionAssessment;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static tds.exam.configuration.SupportApplicationConfiguration.SESSION_APP_CONTEXT;

@Service
class SessionServiceImpl implements SessionService {
    private final RestTemplate restTemplate;
    private final ExamServiceProperties examServiceProperties;

    @Autowired
    public SessionServiceImpl(final RestTemplate restTemplate, final ExamServiceProperties examServiceProperties) {
        this.restTemplate = restTemplate;
        this.examServiceProperties = examServiceProperties;
    }

    @Override
    public Optional<Session> findSessionById(final UUID sessionId) {
        UriComponentsBuilder builder =
            UriComponentsBuilder
                .fromHttpUrl(String.format("%s/%s/%s",
                    examServiceProperties.getSessionUrl(),
                    SESSION_APP_CONTEXT,
                    sessionId));

        Optional<Session> maybeSession = Optional.empty();
        try {
            final Session session = restTemplate.getForObject(builder.build().toUri(), Session.class);
            maybeSession = Optional.of(session);
        } catch (HttpClientErrorException hce) {
            if (hce.getStatusCode() != HttpStatus.NOT_FOUND) {
                throw hce;
            }
        }

        return maybeSession;
    }

    @Override
    @Cacheable(CacheType.LONG_TERM)
    public Optional<ExternalSessionConfiguration> findExternalSessionConfigurationByClientName(final String clientName) {
        UriComponentsBuilder builder =
            UriComponentsBuilder
                .fromHttpUrl(String.format("%s/%s/external-config/%s",
                    examServiceProperties.getSessionUrl(),
                    SESSION_APP_CONTEXT,
                    clientName));

        Optional<ExternalSessionConfiguration> maybeExternalSessionConfig = Optional.empty();
        try {
            final ExternalSessionConfiguration externalSessionConfiguration = restTemplate.getForObject(builder.build().toUri(), ExternalSessionConfiguration.class);
            maybeExternalSessionConfig = Optional.of(externalSessionConfiguration);
        } catch (HttpClientErrorException hce) {
            if (hce.getStatusCode() != HttpStatus.NOT_FOUND) {
                throw hce;
            }
        }

        return maybeExternalSessionConfig;
    }

    @Override
    public Optional<PauseSessionResponse> pause(final UUID sessionId, final String newStatus) {
        UriComponentsBuilder builder =
            UriComponentsBuilder.fromHttpUrl(String.format("%s/%s/%s/pause",
                examServiceProperties.getSessionUrl(),
                SESSION_APP_CONTEXT,
                sessionId));

        Optional<PauseSessionResponse> maybePauseSessionResponse = Optional.empty();

        try {
            final PauseSessionResponse pauseSessionResponse = restTemplate.getForObject(builder.build().toUri(), PauseSessionResponse.class);
            maybePauseSessionResponse = Optional.of(pauseSessionResponse);
        } catch (HttpClientErrorException hce) {
            if (hce.getStatusCode() != HttpStatus.NOT_FOUND) {
                throw hce;
            }
        }

        return maybePauseSessionResponse;
    }

    @Override
    public Optional<SessionAssessment> findSessionAssessment(UUID sessionId, String assessmentKey) {
        UriComponentsBuilder builder =
            UriComponentsBuilder.fromHttpUrl(String.format("%s/%s/%s/assessment/%s",
                examServiceProperties.getSessionUrl(),
                SESSION_APP_CONTEXT,
                sessionId,
                assessmentKey));

        Optional<SessionAssessment> maybeSessionAssessment = Optional.empty();

        try {
            final SessionAssessment sessionAssessment = restTemplate.getForObject(builder.build().toUri(), SessionAssessment.class);
            maybeSessionAssessment = Optional.of(sessionAssessment);
        } catch (HttpClientErrorException hce) {
            if (hce.getStatusCode() != HttpStatus.NOT_FOUND) {
                throw hce;
            }
        }

        return maybeSessionAssessment;
    }

    @Override
    public List<SessionAssessment> findSessionAssessments(UUID sessionId) {
        UriComponentsBuilder builder =
            UriComponentsBuilder.fromHttpUrl(String.format("%s/%s/%s/assessment",
                examServiceProperties.getSessionUrl(),
                SESSION_APP_CONTEXT,
                sessionId));

        ResponseEntity<List<SessionAssessment>> sessionAssessmentsResponse = restTemplate.exchange(
            builder.build().toUri(),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<SessionAssessment>>() {});

        return sessionAssessmentsResponse.getBody();
    }

    @Override
    public List<Session> findSessionsByIds(final List<UUID> sessionIds) {
        if (sessionIds.isEmpty()) {
            return new ArrayList<>();
        }

        final UriComponentsBuilder builder =
            UriComponentsBuilder
                .fromHttpUrl(String.format("%s/%s",
                    examServiceProperties.getSessionUrl(),
                    SESSION_APP_CONTEXT));

        for (UUID sessionId : sessionIds) {
            builder.queryParam("sessionId", sessionId.toString());
        }

        final ResponseEntity<List<Session>> responseEntity = restTemplate.exchange(builder.build().toUri(),
            HttpMethod.GET, null, new ParameterizedTypeReference<List<Session>>() {});

        return responseEntity.getBody();
    }
}
