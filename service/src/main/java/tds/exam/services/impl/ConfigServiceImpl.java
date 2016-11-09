package tds.exam.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

import tds.config.Accommodation;
import tds.config.AssessmentWindow;
import tds.config.ClientSystemFlag;
import tds.config.ClientTestProperty;
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

    /**
     * @inheritDoc
     */
    @Override
    public Optional<ClientTestProperty> findClientTestProperty(final String clientName, final String assessmentId) {
        UriComponentsBuilder builder =
            UriComponentsBuilder
                .fromHttpUrl(String.format("%s/client-test-properties/%s/%s", examServiceProperties.getConfigUrl(), clientName, assessmentId));

        Optional<ClientTestProperty> maybeClientTestProperty = Optional.empty();
        try {
            final ClientTestProperty clientTestProperty = restTemplate.getForObject(builder.toUriString(), ClientTestProperty.class);
            maybeClientTestProperty = Optional.of(clientTestProperty);
        } catch (HttpClientErrorException hce) {
            if (hce.getStatusCode() != HttpStatus.NOT_FOUND) {
                throw hce;
            }
        }

        return maybeClientTestProperty;
    }

    @Override
    public AssessmentWindow[] findAssessmentWindows(String clientName,
                                                    String assessmentId,
                                                    int sessionType,
                                                    long studentId,
                                                    ExternalSessionConfiguration configuration) {

        UriComponentsBuilder builder =
            UriComponentsBuilder
                .fromHttpUrl(String.format("%s/assessment-windows/%s/%s/session-type/%d/student/%d",
                    examServiceProperties.getConfigUrl(),
                    clientName,
                    assessmentId,
                    sessionType,
                    studentId));

        builder.queryParam("shiftWindowStart", configuration.getShiftWindowStart());
        builder.queryParam("shiftWindowEnd", configuration.getShiftWindowEnd());
        builder.queryParam("shiftFormStart", configuration.getShiftFormStart());
        builder.queryParam("shiftFormEnd", configuration.getShiftFormEnd());

        return restTemplate.getForObject(builder.toUriString(), AssessmentWindow[].class);
    }

    @Override
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
    public Accommodation[] findAssessmentAccommodations(String assessmentKey) {
        UriComponentsBuilder builder =
            UriComponentsBuilder
                .fromHttpUrl(String.format("%s/accommodations/%s", examServiceProperties.getConfigUrl(), assessmentKey));

        return restTemplate.getForObject(builder.toUriString(), Accommodation[].class);
    }
}
