package tds.exam.services.impl;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

import tds.config.AssessmentWindow;
import tds.config.ClientTestProperty;
import tds.exam.builder.ExternalSessionConfigurationBuilder;
import tds.exam.configuration.ExamServiceProperties;
import tds.exam.services.ConfigService;
import tds.session.ExternalSessionConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Class for testing the {@link ConfigService}
 */
public class ConfigServiceImplTest {
    private static final String CLIENT_NAME = "CLIENT_TEST";
    private static final String ASSESSMENT_ID = "assessment-id-1";
    private static final String BASE_URL = "http://localhost:8080/config";

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
                .withMaxOpportunities(100)
                .withPrefetch(2)
                .withIsSelectable(false)
                .withLabel("Test Label")
                .withSubjectName("ELA")
                .withInitialAbilityBySubject(true)
                .withAccommodationFamily("myFamily")
                .withSortOrder(2)
                .withRtsFormField("field")
                .withRequireRtsWindow(false)
                .withRtsModeField("mode")
                .withRequireRtsMode(false)
                .withRequireRtsModeWindow(false)
                .withDeleteUnansweredItems(false)
                .withAbilitySlope(5D)
                .withAbilityIntercept(3D)
                .withValidateCompleteness(false)
                .withGradeText("03")
                .build();

        when(restTemplate.getForObject(String.format("%s/%s/%s", BASE_URL, CLIENT_NAME, ASSESSMENT_ID), ClientTestProperty.class)).thenReturn(clientTestProperty);
        Optional<ClientTestProperty> maybeClientTestProperty = configService.findClientTestProperty(CLIENT_NAME, ASSESSMENT_ID);

        assertThat(maybeClientTestProperty.get()).isEqualTo(clientTestProperty);
    }

    @Test
    public void shouldReturnEmptyWhenClientTestPropertyNotFound() {
        when(restTemplate.getForObject(String.format("%s/%s/%s", CLIENT_NAME, ASSESSMENT_ID), ClientTestProperty.class)).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
        Optional<ClientTestProperty> maybeSetOfSubject = configService.findClientTestProperty(CLIENT_NAME, ASSESSMENT_ID);

        assertThat(maybeSetOfSubject).isNotPresent();
    }

    @Test (expected = RestClientException.class)
    public void shouldThrowIfStatusNotNotFoundWhenUnexpectedErrorFindingSetOfAdminSubject() {
        when(restTemplate.getForObject(String.format("%s/%s/%s", BASE_URL, CLIENT_NAME, ASSESSMENT_ID), ClientTestProperty.class)).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
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

        when(restTemplate.getForObject(url, AssessmentWindow[].class)).thenReturn(new AssessmentWindow[]{window});
        AssessmentWindow[] windows = configService.findAssessmentWindows("SBAC_PT", "ELA 11", 0, 23, config);

        assertThat(windows).containsExactly(window);
    }
}
