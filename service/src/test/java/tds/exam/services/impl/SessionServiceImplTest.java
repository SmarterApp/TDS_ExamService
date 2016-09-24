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
import tds.session.ExternalSessionConfiguration;
import tds.session.Session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    public void shouldReturnSession() {
        UUID sessionUUID = UUID.randomUUID();
        Session session = new Session();
        session.setId(sessionUUID);
        String url = String.format("http://localhost:8080/session/%s", sessionUUID);

        when(restTemplate.getForObject(url, Session.class)).thenReturn(session);
        Optional<Session> maybeSession = sessionService.getSession(sessionUUID);
        verify(restTemplate).getForObject(url, Session.class);

        assertThat(maybeSession.isPresent()).isTrue();
        assertThat(maybeSession.get().getId()).isEqualTo(sessionUUID);
    }

    @Test
    public void shouldReturnEmptySessionWhenResponseExceptionOccurs() {
        UUID sessionUUID = UUID.randomUUID();
        String url = String.format("http://localhost:8080/session/%s", sessionUUID);
        when(restTemplate.getForObject(url, Session.class)).thenThrow(new RestClientException("Fail"));
        Optional<Session> maybeSession = sessionService.getSession(sessionUUID);
        verify(restTemplate).getForObject(url, Session.class);

        assertThat(maybeSession.isPresent()).isFalse();
    }

    @Test
    public void shouldReturnExternForClientName() {
        String url = "http://localhost:8080/session/externs/SBAC";
        ExternalSessionConfiguration extern = new ExternalSessionConfiguration("SBAC", "SIMULATION");
        when(restTemplate.getForObject(url, ExternalSessionConfiguration.class)).thenReturn(extern);
        sessionService.getExternalSessionConfigurationByClientName("SBAC");
        verify(restTemplate).getForObject(url, ExternalSessionConfiguration.class);
    }
}
