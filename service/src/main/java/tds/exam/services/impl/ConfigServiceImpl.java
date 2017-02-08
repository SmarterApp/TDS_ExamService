package tds.exam.services.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.text.MessageFormat;
import java.util.HashMap;
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

    @Override
    public String getSystemMessage(String clientName, String messageKey, String languageCode, String context, Object[] replacements) {
        return getSystemMessage(clientName, messageKey, languageCode, context, null, null, replacements);
    }

    @Override
    public String getSystemMessage(String clientName, String messageKey, String languageCode, String context, String subject, String grade, Object[] replacements) {
        UriComponentsBuilder builder =
            UriComponentsBuilder
                .fromHttpUrl(String.format("%s/%s/messages/%s/%s/%s/%s",
                    examServiceProperties.getConfigUrl(),
                    CONFIG_APP_CONTEXT,
                    clientName,
                    context,
                    messageKey,
                    languageCode))
            .queryParam("grade", grade)
            .queryParam("subject", subject);

        String messageTemplate;
        try {
            messageTemplate = restTemplate.getForObject(builder.buildAndExpand().toUri(), String.class);
        } catch (HttpClientErrorException hce) {
            // If there is an HTTP error we can return the messageKey which is the english message version, since it is
            //  better to return a message in English than error out.  This is a suitable recovery.
            return messageKey;
        }

        if (StringUtils.isEmpty(messageTemplate)) {
            return messageKey;
        }

        /*
        Note: The messages in the database use {0}, {1}, {2}, etc. as the placeholders.
        */
        return MessageFormat.format(messageTemplate, replacements);
    }
}
