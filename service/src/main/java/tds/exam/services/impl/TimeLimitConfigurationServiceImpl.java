package tds.exam.services.impl;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

import tds.config.TimeLimitConfiguration;
import tds.exam.configuration.ExamServiceProperties;
import tds.exam.services.TimeLimitConfigurationService;

import static tds.exam.configuration.SupportApplicationConfiguration.CONFIG_APP_CONTEXT;

@Service
class TimeLimitConfigurationServiceImpl implements TimeLimitConfigurationService {

    private final RestTemplate restTemplate;
    private final ExamServiceProperties examServiceProperties;

    public TimeLimitConfigurationServiceImpl(final RestTemplate restTemplate, final ExamServiceProperties examServiceProperties) {
        this.restTemplate = restTemplate;
        this.examServiceProperties = examServiceProperties;
    }

    @Override
    public Optional<TimeLimitConfiguration> findTimeLimitConfiguration(final String clientName, final String assessmentId) {
        UriComponentsBuilder uriBuilder =
                UriComponentsBuilder
                    .fromHttpUrl(String.format("%s/%s/time-limits/%s/%s",
                        examServiceProperties.getConfigUrl(),
                        CONFIG_APP_CONTEXT,
                        clientName,
                        assessmentId));

        Optional<TimeLimitConfiguration> maybeTimeLimitConfig = Optional.empty();
        try {
            final TimeLimitConfiguration timeLimitConfiguration =
                    restTemplate.getForObject(uriBuilder.build().toUri(), TimeLimitConfiguration.class);
            maybeTimeLimitConfig = Optional.of(timeLimitConfiguration);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() != HttpStatus.NOT_FOUND) {
                throw e;
            }
        }

        return maybeTimeLimitConfig;
    }
}
