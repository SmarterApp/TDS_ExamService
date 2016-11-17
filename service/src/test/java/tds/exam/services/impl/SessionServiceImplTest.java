package tds.exam.services.impl;

import org.joda.time.Instant;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;
import java.util.UUID;

import tds.exam.builder.ExternalSessionConfigurationBuilder;
import tds.exam.configuration.ExamServiceProperties;
import tds.exam.services.SessionService;
import tds.session.ExternalSessionConfiguration;
import tds.session.PauseSessionResponse;
import tds.session.Session;
import tds.session.SessionAssessment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SessionServiceImplTest {
    private static final String BASE_URL = "http://localhost:8080/session";

    private SessionService sessionService;

    @Mock
    private RestTemplate restTemplate;

    @Before
    public void setUp() {
        ExamServiceProperties properties = new ExamServiceProperties();
        properties.setSessionUrl(BASE_URL);
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

        String url = String.format("%s/%s", BASE_URL, sessionUUID);

        when(restTemplate.getForObject(url, Session.class)).thenReturn(session);
        Optional<Session> maybeSession = sessionService.findSessionById(sessionUUID);
        verify(restTemplate).getForObject(url, Session.class);

        assertThat(maybeSession.isPresent()).isTrue();
        assertThat(maybeSession.get().getId()).isEqualTo(sessionUUID);
    }

    @Test
    public void shouldReturnEmptySessionWhenStatusIsNotFound() {
        UUID sessionUUID = UUID.randomUUID();
        String url = String.format("%s/%s", BASE_URL, sessionUUID);
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
        String url = String.format("%s/%s/pause", BASE_URL, mockSession.getId());
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
        String url = String.format("%s/%s", BASE_URL, sessionUUID);
        when(restTemplate.getForObject(url, Session.class)).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        sessionService.findSessionById(sessionUUID);
    }


    @Test
    public void shouldReturnExternalSessionConfigForClientName() {
        String url = BASE_URL + "/external-config/SBAC";
        ExternalSessionConfiguration externalSessionConfiguration = new ExternalSessionConfigurationBuilder().withClientName("SBAC").build();
        when(restTemplate.getForObject(url, ExternalSessionConfiguration.class)).thenReturn(externalSessionConfiguration);
        Optional<ExternalSessionConfiguration> maybeExternalSessionConfiguration = sessionService.findExternalSessionConfigurationByClientName("SBAC");
        verify(restTemplate).getForObject(url, ExternalSessionConfiguration.class);

        assertThat(maybeExternalSessionConfiguration.get()).isEqualTo(externalSessionConfiguration);
    }

    @Test
    public void shouldReturnEmptyExternalSessionConfigForClientNameWhenNotFound() {
        String url = BASE_URL + "/external-config/SBAC";
        when(restTemplate.getForObject(url, ExternalSessionConfiguration.class)).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
        Optional<ExternalSessionConfiguration> maybeExternalSessionConfiguration = sessionService.findExternalSessionConfigurationByClientName("SBAC");
        verify(restTemplate).getForObject(url, ExternalSessionConfiguration.class);

        assertThat(maybeExternalSessionConfiguration).isNotPresent();
    }

    @Test(expected = RestClientException.class)
    public void shouldThrowIfStatusNotNotFoundFetchingExternalSessionConfigurationByClientName() {
        String url = BASE_URL + "/external-config/SBAC";
        when(restTemplate.getForObject(url, ExternalSessionConfiguration.class)).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        sessionService.findExternalSessionConfigurationByClientName("SBAC");
    }

    @Test
    public void shouldReturnSessionAssessment() {
        UUID sessionId = UUID.randomUUID();

        String url = UriComponentsBuilder
            .fromHttpUrl(String.format("%s/%s/assessment/%s", BASE_URL, sessionId, "(SBAC) ELA 11 2015 - 2016"))
            .toUriString();

        SessionAssessment sessionAssessment = new SessionAssessment(sessionId, "ELA 11", "(SBAC) ELA 11 2015 - 2016");
        when(restTemplate.getForObject(url, SessionAssessment.class)).thenReturn(sessionAssessment);
        Optional<SessionAssessment> maybeSessionAssessment = sessionService.findSessionAssessment(sessionId, "(SBAC) ELA 11 2015 - 2016");
        verify(restTemplate).getForObject(url, SessionAssessment.class);

        assertThat(maybeSessionAssessment.get()).isEqualTo(sessionAssessment);
    }

    @Test
    public void shouldReturnEmptySessionAssessmentWhenNotFound() {
        UUID sessionId = UUID.randomUUID();

        String url = UriComponentsBuilder
            .fromHttpUrl(String.format("%s/%s/assessment/%s", BASE_URL, sessionId, "(SBAC) ELA 11 2015 - 2016"))
            .toUriString();

        when(restTemplate.getForObject(url, SessionAssessment.class)).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
        Optional<SessionAssessment> maybeSessionAssessment = sessionService.findSessionAssessment(sessionId, "(SBAC) ELA 11 2015 - 2016");
        verify(restTemplate).getForObject(url, SessionAssessment.class);

        assertThat(maybeSessionAssessment).isNotPresent();
    }

    @Test(expected = RestClientException.class)
    public void shouldThrowIfStatusNotNotFoundFetchingSessionAssessment() {
        UUID sessionId = UUID.randomUUID();
        String url = UriComponentsBuilder
            .fromHttpUrl(String.format("%s/%s/assessment/%s", BASE_URL, sessionId, "(SBAC) ELA 11 2015 - 2016"))
            .toUriString();
        when(restTemplate.getForObject(url, SessionAssessment.class)).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        sessionService.findSessionAssessment(sessionId, "(SBAC) ELA 11 2015 - 2016");
    }
}
