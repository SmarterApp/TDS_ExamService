package tds.exam.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;
import java.util.UUID;

import tds.exam.configuration.ExamServiceProperties;
import tds.exam.services.SessionService;
import tds.session.Extern;
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
    public Optional<Session> getSession(UUID sessionId) {
        UriComponentsBuilder builder =
            UriComponentsBuilder
                .fromHttpUrl(String.format("%s/%s", examServiceProperties.getSessionUrl(), sessionId));

        Optional<Session> sessionOptional = Optional.empty();
        try {
            final Session session = restTemplate.getForObject(builder.toUriString(), Session.class);
            sessionOptional = Optional.of(session);
        } catch (RestClientException rce) {
            LOG.debug("Exception thrown when retrieving session", rce);
        }

        return sessionOptional;
    }

    @Override
    public Optional<Extern> getExternByClientName(String clientName) {
        UriComponentsBuilder builder =
            UriComponentsBuilder
                .fromHttpUrl(String.format("%s/externs/%s", examServiceProperties.getSessionUrl(), clientName));

        Optional<Extern> externOptional = Optional.empty();
        try {
            final Extern extern = restTemplate.getForObject(builder.toUriString(), Extern.class);
            externOptional = Optional.of(extern);
        } catch (RestClientException rce) {
            LOG.debug("Exception thrown when retrieving session", rce);
        }

        return externOptional;
    }
}
