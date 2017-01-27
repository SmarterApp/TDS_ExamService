package tds.exam.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;
import java.util.UUID;

import tds.common.cache.CacheType;
import tds.exam.configuration.ExamServiceProperties;
import tds.exam.services.SessionService;
import tds.session.ExternalSessionConfiguration;
import tds.session.PauseSessionResponse;
import tds.session.Session;
import tds.session.SessionAssessment;

@Service
class SessionServiceImpl implements SessionService {
    static final String APP_ROOT_CONTEXT = "session";
    private final RestTemplate restTemplate;
    private final ExamServiceProperties examServiceProperties;

    @Autowired
    public SessionServiceImpl(RestTemplate restTemplate, ExamServiceProperties examServiceProperties) {
        this.restTemplate = restTemplate;
        this.examServiceProperties = examServiceProperties;
    }

    @Override
    @Cacheable(CacheType.SHORT_TERM)
    public Optional<Session> findSessionById(UUID sessionId) {
        UriComponentsBuilder builder =
            UriComponentsBuilder
                .fromHttpUrl(String.format("%s/%s/%s",
                    examServiceProperties.getSessionUrl(),
                    APP_ROOT_CONTEXT,
                    sessionId));

        Optional<Session> maybeSession = Optional.empty();
        try {
            final Session session = restTemplate.getForObject(builder.toUriString(), Session.class);
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
    public Optional<ExternalSessionConfiguration> findExternalSessionConfigurationByClientName(String clientName) {
        UriComponentsBuilder builder =
            UriComponentsBuilder
                .fromHttpUrl(String.format("%s/%s/external-config/%s",
                    examServiceProperties.getSessionUrl(),
                    APP_ROOT_CONTEXT,
                    clientName));

        Optional<ExternalSessionConfiguration> maybeExternalSessionConfig = Optional.empty();
        try {
            final ExternalSessionConfiguration externalSessionConfiguration = restTemplate.getForObject(builder.toUriString(), ExternalSessionConfiguration.class);
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
                APP_ROOT_CONTEXT,
                sessionId));

        Optional<PauseSessionResponse> maybePauseSessionResponse = Optional.empty();

        try {
            final PauseSessionResponse pauseSessionResponse = restTemplate.getForObject(builder.toUriString(), PauseSessionResponse.class);
            maybePauseSessionResponse = Optional.of(pauseSessionResponse);
        } catch (HttpClientErrorException hce) {
            if (hce.getStatusCode() != HttpStatus.NOT_FOUND) {
                throw hce;
            }
        }

        return maybePauseSessionResponse;
    }

    @Override
    @Cacheable(CacheType.LONG_TERM)
    public Optional<SessionAssessment> findSessionAssessment(UUID sessionId, String assessmentKey) {
        UriComponentsBuilder builder =
            UriComponentsBuilder.fromHttpUrl(String.format("%s/%s/%s/assessment/%s",
                examServiceProperties.getSessionUrl(),
                APP_ROOT_CONTEXT,
                sessionId,
                assessmentKey));

        Optional<SessionAssessment> maybeSessionAssessment = Optional.empty();

        try {
            final SessionAssessment sessionAssessment = restTemplate.getForObject(builder.toUriString(), SessionAssessment.class);
            maybeSessionAssessment = Optional.of(sessionAssessment);
        } catch (HttpClientErrorException hce) {
            if (hce.getStatusCode() != HttpStatus.NOT_FOUND) {
                throw hce;
            }
        }

        return maybeSessionAssessment;
    }
}
