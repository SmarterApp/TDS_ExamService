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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import tds.config.TimeLimitConfiguration;
import tds.exam.configuration.ExamServiceProperties;
import tds.exam.services.TimeLimitConfigurationService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static tds.exam.configuration.SupportApplicationConfiguration.CONFIG_APP_CONTEXT;

@RunWith(MockitoJUnitRunner.class)
public class TimeLimitConfigurationServiceImplTest {
    private static final String BASE_URL = "http://localhost:8080";
    private TimeLimitConfigurationService timeLimitConfigurationService;

    @Mock
    private RestTemplate restTemplate;

    @Before
    public void setUp() {
        ExamServiceProperties properties = new ExamServiceProperties();
        properties.setConfigUrl(BASE_URL);
        timeLimitConfigurationService = new TimeLimitConfigurationServiceImpl(restTemplate, properties);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void shouldFindTimeLimitConfiguration() throws URISyntaxException {
        TimeLimitConfiguration timeLimitConfiguration = new TimeLimitConfiguration.Builder().build();
        URI url = new URI(String.format("%s/%s/time-limits/%s/%s", BASE_URL, CONFIG_APP_CONTEXT, "client", "assessmentId"));
        when(restTemplate.getForObject(url, TimeLimitConfiguration.class)).thenReturn(timeLimitConfiguration);
        Optional<TimeLimitConfiguration> maybeTimeLimit = timeLimitConfigurationService.findTimeLimitConfiguration("client", "assessmentId");
        verify(restTemplate).getForObject(url, TimeLimitConfiguration.class);

        assertThat(maybeTimeLimit.get()).isEqualTo(timeLimitConfiguration);
    }

    @Test
    public void shouldReturnEmptyTimeLimitConfiguration() throws URISyntaxException {
        URI url = new URI(String.format("%s/%s/time-limits/%s/%s", BASE_URL, CONFIG_APP_CONTEXT, "client", "assessmentId"));
        when(restTemplate.getForObject(url, TimeLimitConfiguration.class)).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
        Optional<TimeLimitConfiguration> maybeTimeLimit = timeLimitConfigurationService.findTimeLimitConfiguration("client", "assessmentId");
        verify(restTemplate).getForObject(url, TimeLimitConfiguration.class);

        assertThat(maybeTimeLimit.isPresent()).isFalse();
    }
}