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

package tds.exam.services.scoring;

import TDS.Shared.Exceptions.ReturnStatusException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import tds.exam.configuration.ExamServiceProperties;
import tds.score.services.ScoreConfigService;
import tds.student.sql.data.ItemScoringConfig;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.GET;

@RunWith(MockitoJUnitRunner.class)
public class ScoreConfigServiceImplTest {
    private ScoreConfigService scoreConfigService;

    @Mock
    private RestTemplate mockRestTemplate;

    @Before
    public void setUp() {
        ExamServiceProperties examServiceProperties = new ExamServiceProperties();
        examServiceProperties.setConfigUrl("http://localhost:8080");

        scoreConfigService = new ScoreConfigServiceImpl(mockRestTemplate, examServiceProperties);
    }

    @Test
    public void shouldReturnListOfScoreConfigurations() throws URISyntaxException, ReturnStatusException {
        ItemScoringConfig scoringConfig = new ItemScoringConfig();
        ResponseEntity<List<ItemScoringConfig>> entity = new ResponseEntity<>(Collections.singletonList(scoringConfig), HttpStatus.OK);

        URI uri = new URI("http://localhost:8080/config/SBAC_PT/scoring");

        when(mockRestTemplate.exchange(uri, GET, null, new ParameterizedTypeReference<List<ItemScoringConfig>>() {
        })).thenReturn(entity);

        assertThat(scoreConfigService.findItemScoreConfigs("SBAC_PT")).containsExactly(scoringConfig);
    }

    @Test (expected = ReturnStatusException.class)
    public void shouldThrowReturnStatusExceptionWhenRestTemplateThrows() throws URISyntaxException, ReturnStatusException {
        URI uri = new URI("http://localhost:8080/config/SBAC_PT/scoring");

        when(mockRestTemplate.exchange(uri, GET, null, new ParameterizedTypeReference<List<ItemScoringConfig>>() {
        })).thenThrow(new RestClientException("Fail"));

        scoreConfigService.findItemScoreConfigs("SBAC_PT");
    }
}