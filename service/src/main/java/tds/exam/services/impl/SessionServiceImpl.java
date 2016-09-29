package tds.exam.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;
import java.util.UUID;

import tds.exam.configuration.ExamServiceProperties;
import tds.exam.services.SessionService;
import tds.session.ExternalSessionConfiguration;
import tds.session.Session;

@Service
class SessionServiceImpl implements SessionService {
    private static final Logger LOG = LoggerFactory.getLogger(SessionServiceImpl.class);

    private final RestTemplate restTemplate;
    private final ExamServiceProperties examServiceProperties;

    @Autowired
    public SessionServiceImpl(RestTemplate restTemplate, ExamServiceProperties examServiceProperties) {
        this.restTemplate = restTemplate;
        this.examServiceProperties = examServiceProperties;
    }

    @Override
    public Optional<Session> findSessionById(UUID sessionId) {
        UriComponentsBuilder builder =
            UriComponentsBuilder
                .fromHttpUrl(String.format("%s/%s", examServiceProperties.getSessionUrl(), sessionId));

        Optional<Session> maybeSession = Optional.empty();
        try {
            final Session session = restTemplate.getForObject(builder.toUriString(), Session.class);
            maybeSession = Optional.of(session);
        } catch (HttpClientErrorException hce) {
            if(hce.getStatusCode() != HttpStatus.NOT_FOUND) {
                throw hce;
            }
        }

        return maybeSession;
    }

    @Override
    public Optional<ExternalSessionConfiguration> findExternalSessionConfigurationByClientName(String clientName) {
        UriComponentsBuilder builder =
            UriComponentsBuilder
                .fromHttpUrl(String.format("%s/external-config/%s", examServiceProperties.getSessionUrl(), clientName));

        Optional<ExternalSessionConfiguration> maybeExternalSessionConfig = Optional.empty();
        try {
            final ExternalSessionConfiguration externalSessionConfiguration = restTemplate.getForObject(builder.toUriString(), ExternalSessionConfiguration.class);
            maybeExternalSessionConfig = Optional.of(externalSessionConfiguration);
        } catch (HttpClientErrorException hce) {
            if(hce.getStatusCode() != HttpStatus.NOT_FOUND) {
                throw hce;
            }
        }

        return maybeExternalSessionConfig;
    }
}
