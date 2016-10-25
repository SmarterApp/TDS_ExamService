package tds.exam.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

import tds.config.AssessmentWindow;
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
                .fromHttpUrl(String.format("%s/%s/%s", examServiceProperties.getConfigUrl(), clientName, assessmentId));

        Optional<ClientTestProperty> clientTestPropertyOptional = Optional.empty();
        try {
            final ClientTestProperty clientTestProperty = restTemplate.getForObject(builder.toUriString(), ClientTestProperty.class);
            clientTestPropertyOptional = Optional.of(clientTestProperty);
        } catch (HttpClientErrorException hce) {
            if(hce.getStatusCode() != HttpStatus.NOT_FOUND) {
                throw hce;
            }
        }

        return clientTestPropertyOptional;
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

        if(configuration.getShiftWindowEnd() != 0) {

        }

        return null;
    }
}
