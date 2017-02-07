package tds.exam.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

import tds.common.cache.CacheType;
import tds.config.ClientSystemFlag;
import tds.exam.configuration.ExamServiceProperties;
import tds.exam.services.ConfigService;

import static tds.exam.configuration.SupportApplicationConfiguration.CONFIG_APP_CONTEXT;

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
    @Cacheable(CacheType.LONG_TERM)
    public Optional<ClientSystemFlag> findClientSystemFlag(String clientName, String auditObject) {
        UriComponentsBuilder builder =
            UriComponentsBuilder
                .fromHttpUrl(String.format("%s/%s/client-system-flags/%s/%s",
                    examServiceProperties.getConfigUrl(),
                    CONFIG_APP_CONTEXT,
                    clientName,
                    auditObject));

        Optional<ClientSystemFlag> maybeClientSystemFlag = Optional.empty();
        try {
            final ClientSystemFlag clientSystemFlag = restTemplate.getForObject(builder.build().toUri(), ClientSystemFlag.class);
            maybeClientSystemFlag = Optional.of(clientSystemFlag);
        } catch (HttpClientErrorException hce) {
            if (hce.getStatusCode() != HttpStatus.NOT_FOUND) {
                throw hce;
            }
        }

        return maybeClientSystemFlag;
    }
}
