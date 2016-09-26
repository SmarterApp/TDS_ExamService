package tds.exam.services.impl;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import tds.assessment.SetOfAdminSubject;
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
        SetOfAdminSubject subject = new SetOfAdminSubject("key", "assessmentId", true, "virtual");

        when(restTemplate.getForObject("http://localhost:8080/assessments/key", SetOfAdminSubject.class)).thenReturn(subject);
        Optional<SetOfAdminSubject> maybeSetOfSubject = assessmentService.findSetOfAdminSubjectByKey("key");
        verify(restTemplate).getForObject("http://localhost:8080/assessments/key", SetOfAdminSubject.class);

        assertThat(maybeSetOfSubject.get()).isEqualTo(subject);
    }

    @Test
    public void shouldReturnEmptyWhenSetOfAdminSubjectNotFound() {
        when(restTemplate.getForObject("http://localhost:8080/assessments/key", SetOfAdminSubject.class)).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
        Optional<SetOfAdminSubject> maybeSetOfSubject = assessmentService.findSetOfAdminSubjectByKey("key");
        verify(restTemplate).getForObject("http://localhost:8080/assessments/key", SetOfAdminSubject.class);

        assertThat(maybeSetOfSubject).isNotPresent();
    }

    @Test (expected = RestClientException.class)
    public void shouldThrowIfStatusNotNotFoundWhenUnexpectedErrorFindingSetOfAdminSubject() {
        when(restTemplate.getForObject("http://localhost:8080/assessments/key", SetOfAdminSubject.class)).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        assessmentService.findSetOfAdminSubjectByKey("key");
    }
}
