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

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import tds.common.ValidationError;
import tds.exam.WebMvcControllerIntegrationTest;
import tds.exam.services.ExamItemSelectionService;
import tds.exam.services.ExamItemService;
import tds.student.sql.data.OpportunityItem;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcControllerIntegrationTest(controllers = ExamItemController.class)
public class ExamItemControllerIntegrationTests {
    @Autowired
    private MockMvc http;

    @MockBean
    private ExamItemService mockExamItemService;

    @MockBean
    private ExamItemSelectionService mockExamItemSelectionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void shouldMarkItemForReviewSuccessfully() throws Exception {
        final UUID examId = UUID.randomUUID();
        final int position = 3;
        final boolean mark = true;

        when(mockExamItemService.markForReview(examId, position, mark)).thenReturn(Optional.empty());

        http.perform(put("/exam/{id}/item/{position}/review", examId, position)
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(mark)))
            .andExpect(status().isNoContent());

        verify(mockExamItemService).markForReview(examId, position, mark);
    }

    @Test
    public void shouldFailToMarkItemForReview() throws Exception {
        final UUID examId = UUID.randomUUID();
        final int position = 3;
        final boolean mark = true;

        when(mockExamItemService.markForReview(examId, position, mark)).thenReturn(Optional.of(new ValidationError("Some", "error")));

        http.perform(put("/exam/{id}/item/{position}/review", examId, position)
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(mark)))
            .andExpect(status().isUnprocessableEntity());

        verify(mockExamItemService).markForReview(examId, position, mark);
    }

    @Test
    public void shouldReturnPageGroup() throws Exception {
        OpportunityItem item = new OpportunityItem();
        item.setItemKey(1234);
        item.setBankKey(187);
        item.setSegment(1);
        item.setSegmentID("segmentId");
        item.setItemFile("itemFile");
        item.setIsVisible(true);
        item.setIsRequired(true);
        item.setIsPrintable(false);
        item.setGroupItemsRequired(1);
        item.setGroupID("groupId");
        item.setValue("value");
        item.setStimulusFile("stimulusFile");
        item.setPage(1);
        item.setFormat("format");
        item.setSequence(1);
        item.setIsSelected(true);

        UUID examId = UUID.randomUUID();
        when(mockExamItemSelectionService.createNextPageGroup(isA(UUID.class), isA(Integer.class), isA(Integer.class)))
            .thenReturn(Collections.singletonList(item));

        MvcResult response = http.perform(post("/exam/{examId}/item?lastPagePosition={lastPagePosition}&lastItemPosition={lastItemPosition}", examId, 1, 2)
            .contentType(MediaType.APPLICATION_JSON)
            .content(""))
            .andExpect(status().isOk())
            .andReturn();

        JavaType type = objectMapper.getTypeFactory().constructParametricType(List.class, OpportunityItem.class);
        List<OpportunityItem> items = objectMapper.readValue(response.getResponse().getContentAsByteArray(), type);
        assertThat(items).hasSize(1);

        OpportunityItem itemResponse = items.get(0);
        assertThat(itemResponse.getItemKey()).isEqualTo(item.getItemKey());
        assertThat(itemResponse.getBankKey()).isEqualTo(item.getBankKey());
        assertThat(itemResponse.getDateCreated()).isEqualTo(item.getDateCreated());
        assertThat(itemResponse.getFormat()).isEqualTo(item.getFormat());
        assertThat(itemResponse.getGroupID()).isEqualTo(item.getGroupID());
        assertThat(itemResponse.getItemFile()).isEqualTo(item.getItemFile());
        assertThat(itemResponse.isVisible()).isEqualTo(item.isVisible());
        assertThat(itemResponse.isRequired()).isEqualTo(item.isRequired());
        assertThat(itemResponse.isPrintable()).isEqualTo(item.isPrintable());
        assertThat(itemResponse.getGroupItemsRequired()).isEqualTo(item.getGroupItemsRequired());
        assertThat(itemResponse.getGroupID()).isEqualTo(item.getGroupID());
        assertThat(itemResponse.getValue()).isEqualTo(item.getValue());
        assertThat(itemResponse.getStimulusFile()).isEqualTo(item.getStimulusFile());
        assertThat(itemResponse.getPage()).isEqualTo(item.getPage());
        assertThat(itemResponse.getSequence()).isEqualTo(item.getSequence());
        assertThat(itemResponse.getIsSelected()).isEqualTo(item.getIsSelected());
        assertThat(itemResponse.getSegmentID()).isEqualTo("segmentId");
    }
}
