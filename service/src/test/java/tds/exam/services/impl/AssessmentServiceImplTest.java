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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import tds.accommodation.Accommodation;
import tds.assessment.Algorithm;
import tds.assessment.Assessment;
import tds.assessment.AssessmentWindow;
import tds.assessment.Segment;
import tds.exam.builder.ExternalSessionConfigurationBuilder;
import tds.exam.configuration.ExamServiceProperties;
import tds.exam.services.AssessmentService;
import tds.session.ExternalSessionConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.GET;
import static tds.exam.configuration.SupportApplicationConfiguration.ASSESSMENT_APP_CONTEXT;

public class AssessmentServiceImplTest {
    private static final String BASE_URL = "http://localhost:8080/";
    private RestTemplate restTemplate;
    private AssessmentService assessmentService;

    @Before
    public void setUp() {
        restTemplate = mock(RestTemplate.class);
        ExamServiceProperties properties = new ExamServiceProperties();
        properties.setAssessmentUrl(BASE_URL);
        assessmentService = new AssessmentServiceImpl(restTemplate, properties);
    }

    @Test
    public void shouldFindSetOfAdminSubjectsByKey() {
        List<Segment> segments = new ArrayList<>();
        Segment segment = new Segment("segkey", Algorithm.FIXED_FORM);
        segment.setSegmentId("segid");
        segment.setAssessmentKey("key");
        segment.setStartAbility(0);
        segments.add(segment);

        Assessment assessment = new Assessment();
        assessment.setKey("key");
        assessment.setAssessmentId("assessmentId");
        assessment.setSegments(segments);
        assessment.setSelectionAlgorithm(Algorithm.VIRTUAL);
        assessment.setStartAbility(100);

        when(restTemplate.getForObject(BASE_URL + "clientname/assessments/key", Assessment.class)).thenReturn(assessment);
        Optional<Assessment> maybeAssessment = assessmentService.findAssessment("clientname", "key");
        verify(restTemplate).getForObject(BASE_URL + "clientname/assessments/key", Assessment.class);

        assertThat(maybeAssessment.get()).isEqualTo(assessment);
    }

    @Test
    public void shouldReturnEmptyWhenSetOfAdminSubjectNotFound() {
        when(restTemplate.getForObject(isA(String.class), isA(Class.class))).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
        Optional<Assessment> maybeAssessment = assessmentService.findAssessment("clientname", "key");

        assertThat(maybeAssessment).isNotPresent();
    }

    @Test(expected = RestClientException.class)
    public void shouldThrowIfStatusNotNotFoundWhenUnexpectedErrorFindingSetOfAdminSubject() {
        when(restTemplate.getForObject(isA(String.class), isA(Class.class))).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        assessmentService.findAssessment("clientname", "key");
    }

    @Test
    public void shouldFindAssessmentWindows() {
        AssessmentWindow window = new AssessmentWindow.Builder().build();
        String url = UriComponentsBuilder
            .fromHttpUrl(String.format("%s/%s/%s/%s/windows/student/%d",
                BASE_URL,
                "SBAC_PT",
                ASSESSMENT_APP_CONTEXT,
                "ELA 11",
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

        when(restTemplate.exchange(url, GET, null, new ParameterizedTypeReference<List<AssessmentWindow>>() {
        }))
            .thenReturn(entity);

        List<AssessmentWindow> windows = assessmentService.findAssessmentWindows("SBAC_PT", "ELA 11", 23, config);

        assertThat(windows).containsExactly(window);
    }

    @Test
    public void shouldFindAssessmentAccommodationsByKey() {
        Accommodation accommodation = new Accommodation.Builder().build();
        ResponseEntity<List<Accommodation>> entity = new ResponseEntity<>(Collections.singletonList(accommodation), HttpStatus.OK);

        String url = UriComponentsBuilder
            .fromHttpUrl(String.format("%s/SBAC/assessments/accommodations", BASE_URL))
            .queryParam("assessmentKey", "key")
            .toUriString();

        when(restTemplate.exchange(url, GET, null, new ParameterizedTypeReference<List<Accommodation>>() {
        })).thenReturn(entity);

        List<Accommodation> accommodations = assessmentService.findAssessmentAccommodationsByAssessmentKey("SBAC", "key");

        assertThat(accommodations).containsExactly(accommodation);
    }

    @Test
    public void shouldFindAssessmentAccommodationsById() {
        Accommodation accommodation = new Accommodation.Builder().build();
        ResponseEntity<List<Accommodation>> entity = new ResponseEntity<>(Collections.singletonList(accommodation), HttpStatus.OK);

        String url = UriComponentsBuilder
            .fromHttpUrl(String.format("%s/SBAC/assessments/accommodations", BASE_URL))
            .queryParam("assessmentId", "id")
            .toUriString();

        when(restTemplate.exchange(url, GET, null, new ParameterizedTypeReference<List<Accommodation>>() {
        })).thenReturn(entity);

        List<Accommodation> accommodations = assessmentService.findAssessmentAccommodationsByAssessmentId("SBAC", "id");

        assertThat(accommodations).containsExactly(accommodation);
    }
}
