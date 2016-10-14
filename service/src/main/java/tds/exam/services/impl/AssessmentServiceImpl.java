package tds.exam.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

import tds.assessment.SetOfAdminSubject;
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
    public Optional<SetOfAdminSubject> findSetOfAdminSubjectByKey(String key) {
        UriComponentsBuilder builder =
            UriComponentsBuilder
                .fromHttpUrl(String.format("%s/%s", examServiceProperties.getAssessmentUrl(), key));

        Optional<SetOfAdminSubject> maybeSetOfAdminSubject = Optional.empty();
        try {
            final SetOfAdminSubject setOfAdminSubject = restTemplate.getForObject(builder.toUriString(), SetOfAdminSubject.class);
            maybeSetOfAdminSubject = Optional.of(setOfAdminSubject);
        } catch (HttpClientErrorException hce) {
            if (hce.getStatusCode() != HttpStatus.NOT_FOUND) {
                throw hce;
            }
        }

        return maybeSetOfAdminSubject;
    }
}
