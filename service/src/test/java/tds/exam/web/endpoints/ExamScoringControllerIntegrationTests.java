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

package tds.exam.web.endpoints;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tds.exam.WebMvcControllerIntegrationTest;
import tds.score.model.ExamInstance;
import tds.score.services.ItemScoringService;
import tds.student.sql.data.ItemResponseUpdate;
import tds.student.sql.data.ItemResponseUpdateStatus;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcControllerIntegrationTest(controllers = ExamScoringController.class)
public class ExamScoringControllerIntegrationTests {

    @Autowired
    private MockMvc http;

    @MockBean
    private ItemScoringService mockItemScoringService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void itShouldUpdateExamScores() throws Exception {
        final UUID examId = UUID.randomUUID();
        final UUID sessionId = UUID.randomUUID();
        final UUID browserId = UUID.randomUUID();
        final String clientName = "clientName";
        final Float pageDuration = 1.23f;
        final List<ItemResponseUpdate> updates = singletonList(random(ItemResponseUpdate.class));
        final ItemResponseUpdateStatus response = random(ItemResponseUpdateStatus.class);
        final List<ItemResponseUpdateStatus> responses = singletonList(response);

        when(mockItemScoringService.updateResponses(any(ExamInstance.class), anyListOf(ItemResponseUpdate.class), eq(pageDuration)))
            .thenReturn(responses);

        final MvcResult result = http.perform(MockMvcRequestBuilders.put(new URI("/exam/" + examId.toString() + "/scores/responses"))
            .param("sessionId", sessionId.toString())
            .param("browserId", browserId.toString())
            .param("clientName", clientName)
            .param("pageDuration", pageDuration.toString())
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updates)))

            .andExpect(status().is(OK.value()))
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].*", hasSize(4)))
            .andExpect(jsonPath("$[0].Position", equalTo(response.getPosition())))
            .andExpect(jsonPath("$[0].Status", equalTo(response.getStatus())))
            .andExpect(jsonPath("$[0].Reason", equalTo(response.getReason())))
            .andExpect(jsonPath("$[0].DbLatency", equalTo(response.getDbLatency())))
            .andReturn();

        final List<ItemResponseUpdateStatus> deserializedResponse = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            new TypeReference<List<ItemResponseUpdateStatus>>() {});
        assertThat(deserializedResponse).hasSize(1);
    }
}