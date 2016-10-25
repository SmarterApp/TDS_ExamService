package tds.exam.services.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import tds.exam.configuration.ExamServiceProperties;
import tds.exam.services.SessionService;
import tds.session.ExternalSessionConfiguration;
import tds.session.PauseSessionResponse;
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
        Session session = new Session.Builder()
            .withId(sessionUUID)
            .build();

        String url = String.format("http://localhost:8080/session/%s", sessionUUID);

        when(restTemplate.getForObject(url, Session.class)).thenReturn(session);
        Optional<Session> maybeSession = sessionService.findSessionById(sessionUUID);
        verify(restTemplate).getForObject(url, Session.class);

        assertThat(maybeSession.isPresent()).isTrue();
        assertThat(maybeSession.get().getId()).isEqualTo(sessionUUID);
    }

    @Test
    public void shouldReturnEmptySessionWhenStatusIsNotFound() {
        UUID sessionUUID = UUID.randomUUID();
        String url = String.format("http://localhost:8080/session/%s", sessionUUID);
        when(restTemplate.getForObject(url, Session.class)).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
        Optional<Session> maybeSession = sessionService.findSessionById(sessionUUID);
        verify(restTemplate).getForObject(url, Session.class);

        assertThat(maybeSession.isPresent()).isFalse();
    }

    @Test
    public void shouldPauseASession() {
        String sessionStatus = "closed";
        Session mockSession = new Session.Builder()
                .withId(UUID.randomUUID())
                .withStatus(sessionStatus)
                .withDateChanged(Instant.now())
                .withDateEnd(Instant.now())
                .build();
        String url = String.format("http://localhost:8080/session/%s/pause", mockSession.getId());
        when(restTemplate.getForObject(url, PauseSessionResponse.class)).thenReturn(new PauseSessionResponse(mockSession));

        Optional<PauseSessionResponse> maybePauseResponse = sessionService.pause(mockSession.getId(), sessionStatus);

        assertThat(maybePauseResponse).isPresent();
        assertThat(maybePauseResponse.get().getStatus()).isEqualTo(sessionStatus);
    }

    @Test
    public void shouldReturnOptionalEmptyWhenAttemptingToPauseASessionThatIsNotFound() {
        UUID sessionId = UUID.randomUUID();
        String url = String.format("http://localhost:8080/session/%s/pause", sessionId);
        when(restTemplate.getForObject(url, PauseSessionResponse.class)).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        Optional<PauseSessionResponse> maybePauseResponse = sessionService.pause(sessionId, "closed");

        assertThat(maybePauseResponse).isNotPresent();
    }

    @Test(expected = RestClientException.class)
    public void shouldThrowWhenSessionErrorIsNotNotFound() {
        UUID sessionUUID = UUID.randomUUID();
        String url = String.format("http://localhost:8080/session/%s", sessionUUID);
        when(restTemplate.getForObject(url, Session.class)).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        sessionService.findSessionById(sessionUUID);
    }


    @Test
    public void shouldReturnExternalSessionConfigForClientName() {
        String url = "http://localhost:8080/session/external-config/SBAC";
        ExternalSessionConfiguration externalSessionConfiguration = new ExternalSessionConfiguration("SBAC", "SIMULATION", 0, 0, 0, 0);
        when(restTemplate.getForObject(url, ExternalSessionConfiguration.class)).thenReturn(externalSessionConfiguration);
        Optional<ExternalSessionConfiguration> maybeExternalSessionConfiguration = sessionService.findExternalSessionConfigurationByClientName("SBAC");
        verify(restTemplate).getForObject(url, ExternalSessionConfiguration.class);

        assertThat(maybeExternalSessionConfiguration.get()).isEqualTo(externalSessionConfiguration);
    }

    @Test
    public void shouldReturnEmptyExternalSessionConfigForClientNameWhenNotFound() {
        String url = "http://localhost:8080/session/external-config/SBAC";
        when(restTemplate.getForObject(url, ExternalSessionConfiguration.class)).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
        Optional<ExternalSessionConfiguration> maybeExternalSessionConfiguration = sessionService.findExternalSessionConfigurationByClientName("SBAC");
        verify(restTemplate).getForObject(url, ExternalSessionConfiguration.class);

        assertThat(maybeExternalSessionConfiguration).isNotPresent();
    }

    @Test(expected = RestClientException.class)
    public void shouldThrowIfStatusNotNotFoundFetchingExternalSessionConfigurationByClientName() {
        String url = "http://localhost:8080/session/external-config/SBAC";
        when(restTemplate.getForObject(url, ExternalSessionConfiguration.class)).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        sessionService.findExternalSessionConfigurationByClientName("SBAC");
    }
}
