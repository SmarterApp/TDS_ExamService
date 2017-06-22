/***************************************************************************************************
 * Copyright 2017 Regents of the University of California. Licensed under the Educational
 * Community License, Version 2.0 (the “license”); you may not use this file except in
 * compliance with the License. You may obtain a copy of the license at
 *
 * https://opensource.org/licenses/ECL-2.0
 *
 * Unless required under applicable law or agreed to in writing, software distributed under the
 * License is distributed in an “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for specific language governing permissions
 * and limitations under the license.
 **************************************************************************************************/

package tds.exam.services.impl;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.Optional;

import tds.config.ClientSystemFlag;
import tds.exam.configuration.ExamServiceProperties;
import tds.exam.services.ConfigService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static tds.exam.configuration.SupportApplicationConfiguration.CONFIG_APP_CONTEXT;

/**
 * Class for testing the {@link ConfigService}
 */
public class ConfigServiceImplTest {
    private static final String CLIENT_NAME = "CLIENT_TEST";
    private static final String BASE_URL = "http://localhost:8080";
    private static final String ATTRIBUTE_OBJECT = "AnonymousTestee";
    private static final String MESSAGE_KEY = "Replacement message";
    private static final String MESSAGE_TEXT = "Replace {0} and {1}.";
    private static final String LANGUAGE_CODE = "ENU";
    private static final String CONTEXT = "_ContextMethod";

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
    public void shouldFindClientSystemFlag() throws URISyntaxException {
        ClientSystemFlag flag = new ClientSystemFlag.Builder().withAuditObject(ATTRIBUTE_OBJECT).build();

        URI url = new URI(String.format("%s/%s/client-system-flags/%s/%s", BASE_URL, CONFIG_APP_CONTEXT, CLIENT_NAME, ATTRIBUTE_OBJECT));

        when(restTemplate.getForObject(url, ClientSystemFlag.class)).thenReturn(flag);
        Optional<ClientSystemFlag> maybeClientSystemFlag = configService.findClientSystemFlag(CLIENT_NAME, ATTRIBUTE_OBJECT);

        assertThat(maybeClientSystemFlag.get()).isEqualTo(flag);
    }

    @Test
    public void shouldReturnEmptyWhenClientSystemFlagNotFound() throws URISyntaxException {
        URI url = new URI(String.format("%s/%s/client-system-flags/%s/%s", BASE_URL, CONFIG_APP_CONTEXT, CLIENT_NAME, ATTRIBUTE_OBJECT));

        when(restTemplate.getForObject(url, ClientSystemFlag.class)).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
        Optional<ClientSystemFlag> maybeClientSystemFlag = configService.findClientSystemFlag(CLIENT_NAME, ATTRIBUTE_OBJECT);

        assertThat(maybeClientSystemFlag).isNotPresent();
    }

    @Test(expected = RestClientException.class)
    public void shouldThrowIfStatusNotNotFoundWhenUnexpectedErrorFindingClientSystemFlag() throws URISyntaxException {
        URI url = new URI(String.format("%s/%s/client-system-flags/%s/%s", BASE_URL, CONFIG_APP_CONTEXT, CLIENT_NAME, ATTRIBUTE_OBJECT));
        when(restTemplate.getForObject(url, ClientSystemFlag.class)).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        configService.findClientSystemFlag(CLIENT_NAME, ATTRIBUTE_OBJECT);
    }

    @Test
    public void shouldFindSystemMessage() throws URISyntaxException {
        Object[] replacements = new Object[] {1, "2"};
        String expectedMessage = MessageFormat.format(MESSAGE_TEXT, replacements);

        UriComponentsBuilder builder =
            UriComponentsBuilder
                .fromHttpUrl(String.format("%s/%s/messages/%s/%s/%s/%s",
                    BASE_URL,
                    CONFIG_APP_CONTEXT,
                    CLIENT_NAME,
                    CONTEXT,
                    MESSAGE_KEY,
                    LANGUAGE_CODE))
                .queryParam("grade", null)
                .queryParam("subject", null);

        when(restTemplate.getForObject(builder.build().toUri(), String.class)).thenReturn(MESSAGE_TEXT);
        String systemMessage = configService.getFormattedMessage(CLIENT_NAME, CONTEXT, MESSAGE_KEY, LANGUAGE_CODE, replacements);

        assertThat(systemMessage).isEqualTo(expectedMessage);
    }

    @Test
    public void shouldReturnMessageKeyWhenMessageKeyNotFound() throws URISyntaxException {
        UriComponentsBuilder builder =
            UriComponentsBuilder
                .fromHttpUrl(String.format("%s/%s/messages/%s/%s/%s/%s",
                    BASE_URL,
                    CONFIG_APP_CONTEXT,
                    CLIENT_NAME,
                    CONTEXT,
                    MESSAGE_KEY,
                    LANGUAGE_CODE))
                .queryParam("grade", null)
                .queryParam("subject", null);

        when(restTemplate.getForObject(builder.build().toUri(), String.class)).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
        String systemMessage = configService.getFormattedMessage(CLIENT_NAME, CONTEXT, MESSAGE_KEY, LANGUAGE_CODE, new Object[] {1, "2"});

        assertThat(systemMessage).isEqualTo(MESSAGE_KEY);
    }
}
