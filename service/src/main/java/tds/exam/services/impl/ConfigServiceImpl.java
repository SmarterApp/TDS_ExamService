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

import java.util.List;
import java.util.Optional;

import tds.common.cache.CacheType;
import tds.config.Accommodation;
import tds.config.AssessmentWindow;
import tds.config.ClientSystemFlag;
import tds.exam.configuration.ExamServiceProperties;
import tds.exam.services.ConfigService;
import tds.session.ExternalSessionConfiguration;

/**
 * Service for retrieving data from the Config Session Microservice
 */
@Service
class ConfigServiceImpl implements ConfigService {
    private final RestTemplate restTemplate;
    private final ExamServiceProperties examServiceProperties;

    @Autowired
    public ConfigServiceImpl(RestTemplate restTemplate, ExamServiceProperties examServiceProperties) {
        this.restTemplate = restTemplate;
        this.examServiceProperties = examServiceProperties;
    }

    @Override
    @Cacheable(CacheType.MEDIUM_TERM)
    public List<AssessmentWindow> findAssessmentWindows(String clientName,
                                                        String assessmentId,
                                                        long studentId,
                                                        ExternalSessionConfiguration configuration) {

        UriComponentsBuilder builder =
            UriComponentsBuilder
                .fromHttpUrl(String.format("%s/assessment-windows/%s/%s/student/%d",
                    examServiceProperties.getConfigUrl(),
                    clientName,
                    assessmentId,
                    studentId));

        builder.queryParam("shiftWindowStart", configuration.getShiftWindowStart());
        builder.queryParam("shiftWindowEnd", configuration.getShiftWindowEnd());
        builder.queryParam("shiftFormStart", configuration.getShiftFormStart());
        builder.queryParam("shiftFormEnd", configuration.getShiftFormEnd());

        ResponseEntity<List<AssessmentWindow>> responseEntity = restTemplate.exchange(builder.toUriString(),
            HttpMethod.GET, null, new ParameterizedTypeReference<List<AssessmentWindow>>() {
            });

        return responseEntity.getBody();
    }

    @Override
    @Cacheable(CacheType.LONG_TERM)
    public Optional<ClientSystemFlag> findClientSystemFlag(String clientName, String auditObject) {
        UriComponentsBuilder builder =
            UriComponentsBuilder
                .fromHttpUrl(String.format("%s/client-system-flags/%s/%s", examServiceProperties.getConfigUrl(), clientName, auditObject));

        Optional<ClientSystemFlag> maybeClientSystemFlag = Optional.empty();
        try {
            final ClientSystemFlag clientSystemFlag = restTemplate.getForObject(builder.toUriString(), ClientSystemFlag.class);
            maybeClientSystemFlag = Optional.of(clientSystemFlag);
        } catch (HttpClientErrorException hce) {
            if (hce.getStatusCode() != HttpStatus.NOT_FOUND) {
                throw hce;
            }
        }

        return maybeClientSystemFlag;
    }

    @Override
    public List<Accommodation> findAssessmentAccommodationsByAssessmentKey(final String clientName, final String assessmentKey) {
        UriComponentsBuilder builder =
            UriComponentsBuilder
                .fromHttpUrl(String.format("%s/%s/accommodations/%s", examServiceProperties.getConfigUrl(), clientName, assessmentKey));

        ResponseEntity<List<Accommodation>> responseEntity = restTemplate.exchange(builder.toUriString(),
            HttpMethod.GET, null, new ParameterizedTypeReference<List<Accommodation>>() {
            });

        return responseEntity.getBody();
    }

    @Override
    public List<Accommodation> findAssessmentAccommodationsByAssessmentId(String clientName, String assessmentId) {
        UriComponentsBuilder builder =
            UriComponentsBuilder
                .fromHttpUrl(String.format("%s/%s/accommodations?assessmentId=%s", examServiceProperties.getConfigUrl(), clientName, assessmentId));

        ResponseEntity<List<Accommodation>> responseEntity = restTemplate.exchange(builder.toUriString(),
            HttpMethod.GET, null, new ParameterizedTypeReference<List<Accommodation>>() {
            });

        return responseEntity.getBody();
    }
}
