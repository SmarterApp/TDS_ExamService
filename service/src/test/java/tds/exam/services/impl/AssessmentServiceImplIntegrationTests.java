package tds.exam.services.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import tds.assessment.Assessment;
import tds.assessment.Segment;
import tds.common.Algorithm;
import tds.exam.configuration.ExamServiceProperties;
import tds.exam.health.ServicesHealthIndicator;
import tds.exam.services.AssessmentService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(properties = "tds.cache.enabled = true")
public class AssessmentServiceImplIntegrationTests {
    @MockBean
    private RestTemplate mockRestTemplate;

    @MockBean
    private ExamServiceProperties properties;

    @MockBean
    private ServicesHealthIndicator servicesHealthIndicator;

    @Autowired
    private AssessmentService assessmentService;

    @Test
    public void shouldReturnCachedAssessment() throws URISyntaxException {


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

        URI url = new URI("http://localhost:8080/clientname/assessments/key");

        when(properties.getAssessmentUrl()).thenReturn("http://localhost:8080");
        when(mockRestTemplate.getForObject(url, Assessment.class)).thenReturn(assessment);
        Optional<Assessment> maybeAssessment1 = assessmentService.findAssessment("clientname", "key");
        Optional<Assessment> maybeAssessment2 = assessmentService.findAssessment("clientname", "key");
        verify(mockRestTemplate, times(1)).getForObject(url, Assessment.class);
        assertThat(maybeAssessment1).isEqualTo(maybeAssessment2);
    }
}
