package tds.exam.services.impl;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import tds.config.ClientSystemFlag;
import tds.exam.configuration.ExamServiceProperties;
import tds.exam.services.ConfigService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Class for testing the {@link ConfigService}
 */
public class ConfigServiceImplTest {
    private static final String CLIENT_NAME = "CLIENT_TEST";
    private static final String BASE_URL = "http://localhost:8080/config";
    private static final String ATTRIBUTE_OBJECT = "AnonymousTestee";

    private RestTemplate restTemplate;
    private ConfigService configService;

    @Before
    public void setUp() {
        restTemplate = mock(RestTemplate.class);
        ExamServiceProperties properties = new ExamServiceProperties();
        properties.setConfigUrl(BASE_URL);
        configService = new ConfigServiceImpl(restTemplate, properties);
    }

    @Test
    public void shouldFindClientSystemFlag() {
        ClientSystemFlag flag = new ClientSystemFlag.Builder().withAuditObject(ATTRIBUTE_OBJECT).build();

        when(restTemplate.getForObject(String.format("%s/client-system-flags/%s/%s", BASE_URL, CLIENT_NAME, ATTRIBUTE_OBJECT), ClientSystemFlag.class)).thenReturn(flag);
        Optional<ClientSystemFlag> maybeClientSystemFlag = configService.findClientSystemFlag(CLIENT_NAME, ATTRIBUTE_OBJECT);

        assertThat(maybeClientSystemFlag.get()).isEqualTo(flag);
    }

    @Test
    public void shouldReturnEmptyWhenClientSystemFlagNotFound() {
        when(restTemplate.getForObject(String.format("%s/client-system-flags/%s/%s", BASE_URL, CLIENT_NAME, ATTRIBUTE_OBJECT), ClientSystemFlag.class)).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
        Optional<ClientSystemFlag> maybeClientSystemFlag = configService.findClientSystemFlag(CLIENT_NAME, ATTRIBUTE_OBJECT);

        assertThat(maybeClientSystemFlag).isNotPresent();
    }

    @Test(expected = RestClientException.class)
    public void shouldThrowIfStatusNotNotFoundWhenUnexpectedErrorFindingClientSystemFlag() {
        when(restTemplate.getForObject(String.format("%s/client-system-flags/%s/%s", BASE_URL, CLIENT_NAME, ATTRIBUTE_OBJECT), ClientSystemFlag.class)).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        configService.findClientSystemFlag(CLIENT_NAME, ATTRIBUTE_OBJECT);
    }


}
