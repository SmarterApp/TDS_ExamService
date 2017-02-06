package tds.exam.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import tds.accommodation.Accommodation;
import tds.assessment.Assessment;
import tds.assessment.AssessmentWindow;
import tds.common.cache.CacheType;
import tds.exam.configuration.ExamServiceProperties;
import tds.exam.services.AssessmentService;
import tds.session.ExternalSessionConfiguration;

import static tds.exam.configuration.SupportApplicationConfiguration.ASSESSMENT_APP_CONTEXT;

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
    @Cacheable(CacheType.LONG_TERM)
    public Optional<Assessment> findAssessment(final String clientName, final String key) {
        URI uri =
            UriComponentsBuilder
                .fromHttpUrl(String.format("%s/%s/%s/%s",
                    examServiceProperties.getAssessmentUrl(),
                    clientName,
                    ASSESSMENT_APP_CONTEXT,
                    key)).build().toUri();

        Optional<Assessment> maybeAssessment = Optional.empty();
        try {
            final Assessment assessment = restTemplate.getForObject(uri, Assessment.class);
            maybeAssessment = Optional.of(assessment);
        } catch (HttpClientErrorException hce) {
            if (hce.getStatusCode() != HttpStatus.NOT_FOUND) {
                throw hce;
            }
        }

        return maybeAssessment;
    }

    @Override
    @Cacheable(CacheType.MEDIUM_TERM)
    public List<AssessmentWindow> findAssessmentWindows(String clientName,
                                                        String assessmentId,
                                                        long studentId,
                                                        ExternalSessionConfiguration configuration) {
        UriComponentsBuilder builder =
            UriComponentsBuilder
                .fromHttpUrl(String.format("%s/%s/%s/%s/windows/student/%d",
                    examServiceProperties.getAssessmentUrl(),
                    clientName,
                    ASSESSMENT_APP_CONTEXT,
                    assessmentId,
                    studentId));

        builder.queryParam("shiftWindowStart", configuration.getShiftWindowStart());
        builder.queryParam("shiftWindowEnd", configuration.getShiftWindowEnd());
        builder.queryParam("shiftFormStart", configuration.getShiftFormStart());
        builder.queryParam("shiftFormEnd", configuration.getShiftFormEnd());

        ResponseEntity<List<AssessmentWindow>> responseEntity = restTemplate.exchange(builder.build().toUri(),
            HttpMethod.GET, null, new ParameterizedTypeReference<List<AssessmentWindow>>() {
            });

        return responseEntity.getBody();
    }

    @Override
    public List<Accommodation> findAssessmentAccommodationsByAssessmentKey(final String clientName, final String assessmentKey) {
        UriComponentsBuilder builder =
            UriComponentsBuilder
                .fromHttpUrl(String.format("%s/%s/%s/accommodations",
                    examServiceProperties.getAssessmentUrl(),
                    clientName,
                    ASSESSMENT_APP_CONTEXT))
                .queryParam("assessmentKey", assessmentKey);

        ResponseEntity<List<Accommodation>> responseEntity = restTemplate.exchange(builder.build().toUri(),
            HttpMethod.GET, null, new ParameterizedTypeReference<List<Accommodation>>() {
            });

        return responseEntity.getBody();
    }

    @Override
    public List<Accommodation> findAssessmentAccommodationsByAssessmentId(String clientName, String assessmentId) {
        UriComponentsBuilder builder =
            UriComponentsBuilder
                .fromHttpUrl(String.format("%s/%s/%s/accommodations",
                    examServiceProperties.getAssessmentUrl(),
                    clientName,
                    ASSESSMENT_APP_CONTEXT))
                .queryParam("assessmentId", assessmentId);


        ResponseEntity<List<Accommodation>> responseEntity = restTemplate.exchange(builder.build().toUri(),
            HttpMethod.GET, null, new ParameterizedTypeReference<List<Accommodation>>() {
            });

        return responseEntity.getBody();
    }

}
