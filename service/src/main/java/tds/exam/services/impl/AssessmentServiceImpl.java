package tds.exam.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

import tds.assessment.Assessment;
import tds.exam.configuration.ExamServiceProperties;
import tds.exam.services.AssessmentService;

@Service
class AssessmentServiceImpl implements AssessmentService {
    private final RestTemplate restTemplate;
    private final ExamServiceProperties examServiceProperties;

    @Autowired
    public AssessmentServiceImpl(RestTemplate restTemplate, ExamServiceProperties examServiceProperties) {
        this.restTemplate = restTemplate;
        this.examServiceProperties = examServiceProperties;
    }

    @Override
    public Optional<Assessment> findAssessmentByKey(final String clientName, final String key) {
        UriComponentsBuilder builder =
            UriComponentsBuilder
                .fromHttpUrl(String.format("%s/%s/assessments/%s", examServiceProperties.getAssessmentUrl(), clientName, key));

        Optional<Assessment> maybeAssessment = Optional.empty();
        try {
            final Assessment assessment = restTemplate.getForObject(builder.toUriString(), Assessment.class);
            maybeAssessment = Optional.of(assessment);
        } catch (HttpClientErrorException hce) {
            if (hce.getStatusCode() != HttpStatus.NOT_FOUND) {
                throw hce;
            }
        }

        return maybeAssessment;
    }
}
