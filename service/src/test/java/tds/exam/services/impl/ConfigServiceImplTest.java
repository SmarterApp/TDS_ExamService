package tds.exam.services.impl;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import tds.config.Accommodation;
import tds.config.AssessmentWindow;
import tds.config.ClientSystemFlag;
import tds.config.ClientTestProperty;
import tds.exam.builder.ExternalSessionConfigurationBuilder;
import tds.exam.configuration.ExamServiceProperties;
import tds.exam.services.ConfigService;
import tds.session.ExternalSessionConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.GET;

/**
 * Class for testing the {@link ConfigService}
 */
public class ConfigServiceImplTest {
    private static final String CLIENT_NAME = "CLIENT_TEST";
    private static final String ASSESSMENT_ID = "assessment-id-1";
    private static final String BASE_URL = "http://localhost:8080/config";
    private static final String ATTRIBUTE_OBJECT = "AnonymousTestee";

    private RestTemplate restTemplate;
    private ConfigService configService;

    @Before
    public void setUp() {
        restTemplate = mock(RestTemplate.class);
        ExamServiceProperties properties = new ExamServiceProperties();
        properties.setConfigUrl(BASE_URL);
        configService = new ConfigServiceImpl(restTemplate, properties);
    }

    @Test
    public void shouldFindClientTestPropertyByKey() {
        ClientTestProperty clientTestProperty = new ClientTestProperty.Builder()
                .withClientName(CLIENT_NAME)
                .withAssessmentId(ASSESSMENT_ID)
                .build();

        when(restTemplate.getForObject(String.format("%s/client-test-properties/%s/%s", BASE_URL, CLIENT_NAME, ASSESSMENT_ID), ClientTestProperty.class)).thenReturn(clientTestProperty);
        Optional<ClientTestProperty> maybeClientTestProperty = configService.findClientTestProperty(CLIENT_NAME, ASSESSMENT_ID);

        assertThat(maybeClientTestProperty.get()).isEqualTo(clientTestProperty);
    }

    @Test
    public void shouldReturnEmptyWhenClientTestPropertyNotFound() {
        when(restTemplate.getForObject(String.format("%s/client-test-properties/%s/%s", BASE_URL, CLIENT_NAME, ASSESSMENT_ID), ClientTestProperty.class)).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
        Optional<ClientTestProperty> maybeClientTestProperty = configService.findClientTestProperty(CLIENT_NAME, ASSESSMENT_ID);

        assertThat(maybeClientTestProperty).isNotPresent();
    }

    @Test (expected = RestClientException.class)
    public void shouldThrowIfStatusNotNotFoundWhenUnexpectedErrorFindingClientTestProperty() {
        when(restTemplate.getForObject(String.format("%s/client-test-properties/%s/%s", BASE_URL, CLIENT_NAME, ASSESSMENT_ID), ClientTestProperty.class)).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        configService.findClientTestProperty(CLIENT_NAME, ASSESSMENT_ID);
    }

    @Test
    public void shouldFindAssessmentWindows() {
        AssessmentWindow window = new AssessmentWindow.Builder().build();
        String url = UriComponentsBuilder
            .fromHttpUrl(String.format("%s/assessment-windows/%s/%s/session-type/%d/student/%d",
                BASE_URL,
                "SBAC_PT",
                "ELA 11",
                0,
                23))
            .queryParam("shiftWindowStart", 1)
            .queryParam("shiftWindowEnd", 2)
            .queryParam("shiftFormStart", 10)
            .queryParam("shiftFormEnd", 11)
            .toUriString();

        ExternalSessionConfiguration config = new ExternalSessionConfigurationBuilder()
            .withShiftWindowStart(1)
            .withShiftWindowEnd(2)
            .withShiftFormStart(10)
            .withShiftFormEnd(11)
            .build();

        ResponseEntity<List<AssessmentWindow>> entity = new ResponseEntity<>(Collections.singletonList(window), HttpStatus.OK);

        when(restTemplate.exchange(url, GET, null, new ParameterizedTypeReference<List<AssessmentWindow>>() {}))
            .thenReturn(entity);

        List<AssessmentWindow> windows = configService.findAssessmentWindows("SBAC_PT", "ELA 11", 0, 23, config);

        assertThat(windows).containsExactly(window);
    }


    @Test
    public void shouldFindClientSystemFlag() {
        ClientSystemFlag flag = new ClientSystemFlag.Builder().withAuditObject(ATTRIBUTE_OBJECT).build();

        when(restTemplate.getForObject(String.format("%s/client-system-flags/%s/%s", BASE_URL, CLIENT_NAME, ATTRIBUTE_OBJECT), ClientSystemFlag.class)).thenReturn(flag);
        Optional<ClientSystemFlag> maybeClientSystemFlag = configService.findClientSystemFlag(CLIENT_NAME, ATTRIBUTE_OBJECT);

        assertThat(maybeClientSystemFlag.get()).isEqualTo(flag);
    }

    @Test
    public void shouldReturnEmptyWhenClientSystemFlagNotFound() {
        when(restTemplate.getForObject(String.format("%s/client-system-flags/%s/%s", BASE_URL, CLIENT_NAME, ATTRIBUTE_OBJECT), ClientSystemFlag.class)).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
        Optional<ClientSystemFlag> maybeClientSystemFlag = configService.findClientSystemFlag(CLIENT_NAME, ATTRIBUTE_OBJECT);

        assertThat(maybeClientSystemFlag).isNotPresent();
    }

    @Test (expected = RestClientException.class)
    public void shouldThrowIfStatusNotNotFoundWhenUnexpectedErrorFindingClientSystemFlag() {
        when(restTemplate.getForObject(String.format("%s/client-system-flags/%s/%s", BASE_URL, CLIENT_NAME, ATTRIBUTE_OBJECT), ClientSystemFlag.class)).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        configService.findClientSystemFlag(CLIENT_NAME, ATTRIBUTE_OBJECT);
    }

    @Test
    public void shouldFindAssessmentAccommodations() {
        Accommodation accommodation = new Accommodation.Builder().build();
        ResponseEntity<List<Accommodation>> entity = new ResponseEntity<>(Collections.singletonList(accommodation), HttpStatus.OK);

        when(restTemplate.exchange(String.format("%s/accommodations/key", BASE_URL), GET, null, new ParameterizedTypeReference<List<Accommodation>>() {}))
            .thenReturn(entity);

        List<Accommodation> accommodations = configService.findAssessmentAccommodations("key");

        assertThat(accommodations).containsExactly(accommodation);
    }
}
