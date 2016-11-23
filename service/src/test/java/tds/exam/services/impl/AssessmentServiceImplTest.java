package tds.exam.services.impl;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import tds.assessment.Algorithm;
import tds.assessment.Assessment;
import tds.assessment.Segment;
import tds.exam.configuration.ExamServiceProperties;
import tds.exam.services.AssessmentService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AssessmentServiceImplTest {
    private RestTemplate restTemplate;
    private AssessmentService assessmentService;

    @Before
    public void setUp() {
        restTemplate = mock(RestTemplate.class);
        ExamServiceProperties properties = new ExamServiceProperties();
        properties.setAssessmentUrl("http://localhost:8080/assessments");
        assessmentService = new AssessmentServiceImpl(restTemplate, properties);
    }

    @Test
    public void shouldFindSetOfAdminSubjectsByKey() {
        List<Segment> segments = new ArrayList<>();
        Segment segment = new Segment("segkey");
        segment.setSegmentId("segid");
        segment.setAssessmentKey("key");
        segment.setSelectionAlgorithm(Algorithm.FIXED_FORM);
        segment.setStartAbility(0);
        segments.add(segment);

        Assessment assessment = new Assessment();
        assessment.setKey("key");
        assessment.setAssessmentId("assessmentId");
        assessment.setSegments(segments);
        assessment.setSelectionAlgorithm(Algorithm.FIXED_FORM.VIRTUAL);
        assessment.setStartAbility(100);

        when(restTemplate.getForObject("http://localhost:8080/assessments/key", Assessment.class)).thenReturn(assessment);
        Optional<Assessment> maybeAssessment = assessmentService.findAssessmentByKey("key");
        verify(restTemplate).getForObject("http://localhost:8080/assessments/key", Assessment.class);

        assertThat(maybeAssessment.get()).isEqualTo(assessment);
    }

    @Test
    public void shouldReturnEmptyWhenSetOfAdminSubjectNotFound() {
        when(restTemplate.getForObject("http://localhost:8080/assessments/key", Assessment.class)).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
        Optional<Assessment> maybeAssessment = assessmentService.findAssessmentByKey("key");
        verify(restTemplate).getForObject("http://localhost:8080/assessments/key", Assessment.class);

        assertThat(maybeAssessment).isNotPresent();
    }

    @Test (expected = RestClientException.class)
    public void shouldThrowIfStatusNotNotFoundWhenUnexpectedErrorFindingSetOfAdminSubject() {
        when(restTemplate.getForObject("http://localhost:8080/assessments/key", Assessment.class)).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        assessmentService.findAssessmentByKey("key");
    }
}
