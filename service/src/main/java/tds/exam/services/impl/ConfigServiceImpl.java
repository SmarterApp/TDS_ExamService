package tds.exam.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import tds.config.ClientTestProperty;
import tds.exam.configuration.ExamServiceProperties;
import tds.exam.services.ConfigService;

import java.util.Optional;

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
}
