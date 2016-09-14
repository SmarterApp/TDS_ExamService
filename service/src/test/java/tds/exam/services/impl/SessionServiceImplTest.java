package tds.exam.services.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

import tds.exam.configuration.ExamServiceProperties;
import tds.exam.services.SessionService;
import tds.session.Session;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

public class SessionServiceImplTest {
    private SessionService sessionService;
    private RestTemplate restTemplate;

    @Before
    public void setUp() {
        restTemplate = mock(RestTemplate.class);
        ExamServiceProperties properties = new ExamServiceProperties();
        properties.setSessionUrl("http://localhost:8080/session");
        sessionService = new SessionServiceImpl(restTemplate, properties);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void itShouldReturnSession() {
        UUID sessionUUID = UUID.randomUUID();
        Session session = new Session();
        session.setId(sessionUUID);
        String url = String.format("http://localhost:8080/session/%s", sessionUUID);

        when(restTemplate.getForObject(url, Session.class)).thenReturn(session);
        Optional<Session> sessionOptional = sessionService.getSession(sessionUUID);
        verify(restTemplate).getForObject(url, Session.class);

        assertThat(sessionOptional.isPresent()).isTrue();
        assertThat(sessionOptional.get().getId()).isEqualTo(sessionUUID);
    }

    @Test
    public void itShouldReturnEmptyWhenResponseExceptionOccurs() {
        UUID sessionUUID = UUID.randomUUID();
        String url = String.format("http://localhost:8080/session/%s", sessionUUID);
        when(restTemplate.getForObject(url, Session.class)).thenThrow(new RestClientException("Fail"));
        Optional<Session> sessionOptional = sessionService.getSession(sessionUUID);
        verify(restTemplate).getForObject(url, Session.class);

        assertThat(sessionOptional.isPresent()).isFalse();
    }
}
