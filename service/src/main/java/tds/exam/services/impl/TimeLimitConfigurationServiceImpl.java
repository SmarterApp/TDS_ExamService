package tds.exam.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import tds.config.TimeLimitConfiguration;
import tds.exam.configuration.ExamServiceProperties;
import tds.exam.services.TimeLimitConfigurationService;

import java.util.Optional;

@Service
class TimeLimitConfigurationServiceImpl implements TimeLimitConfigurationService {
    private static final Logger LOG = LoggerFactory.getLogger(SessionServiceImpl.class);

    private final RestTemplate restTemplate;
    private final ExamServiceProperties examServiceProperties;

    public TimeLimitConfigurationServiceImpl(RestTemplate restTemplate, ExamServiceProperties examServiceProperties) {
        this.restTemplate = restTemplate;
        this.examServiceProperties = examServiceProperties;
    }

    @Override
    public Optional<TimeLimitConfiguration> findTimeLimitConfiguration(String clientName, String assessmentId) {
        UriComponentsBuilder uriBuilder =
                UriComponentsBuilder
                        .fromHttpUrl(String.format("%s/%s/%s", examServiceProperties.getConfigUrl(), clientName, assessmentId));

        Optional<TimeLimitConfiguration> maybeTimeLimitConfig = Optional.empty();
        try {
            final TimeLimitConfiguration timeLimitConfiguration =
                    restTemplate.getForObject(uriBuilder.toUriString(), TimeLimitConfiguration.class);
            maybeTimeLimitConfig = Optional.of(timeLimitConfiguration);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() != HttpStatus.NOT_FOUND) {
                throw e;
            }
        }

        return maybeTimeLimitConfig;
    }
}
