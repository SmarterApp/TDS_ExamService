package tds.exam.web.endpoints;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.Instant;
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

import tds.common.Response;
import tds.common.ValidationError;
import tds.exam.ExamInfo;
import tds.exam.ExamItem;
import tds.exam.ExamItemResponse;
import tds.exam.ExamPage;
import tds.exam.WebMvcControllerIntegrationTest;
import tds.exam.builder.ExamItemBuilder;
import tds.exam.builder.ExamItemResponseBuilder;
import tds.exam.builder.ExamItemResponseScoreBuilder;
import tds.exam.builder.ExamPageBuilder;
import tds.exam.services.ExamItemSelectionService;
import tds.exam.services.ExamItemService;
import tds.student.sql.data.OpportunityItem;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
    public void shouldInsertResponse() throws Exception {
        ExamInfo examInfo = new ExamInfo(UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID());
        int pagePosition = 1;
        ExamItemResponse response = new ExamItemResponseBuilder().build();
        UUID mockExamItemId = UUID.randomUUID();
        ExamItemResponse mockExamItemResponse = new ExamItemResponseBuilder()
            .withExamItemId(mockExamItemId)
            .withScore(new ExamItemResponseScoreBuilder().build())
            .build();
        ExamItem mockExamItem = new ExamItemBuilder()
            .withId(mockExamItemId)
            .withStimulusFilePath("/path/to/stimulus/187-1234.xml")
            .withResponse(mockExamItemResponse)
            .build();
        List<ExamItem> mockExamItems = Collections.singletonList(mockExamItem);
        ExamPage mockNextExamPage = new ExamPageBuilder()
            .withExamId(examInfo.getExamId())
            .withPagePosition(2)
            .withCreatedAt(Instant.now().minus(20000L))
            .withExamItems(mockExamItems)
            .build();

        when(mockExamItemService.insertResponses(isA(UUID.class),
            isA(Integer.class),
            any(ExamItemResponse[].class)))
            .thenReturn(new Response<>(mockNextExamPage));

        String examItemResponseJson = new ObjectMapper().writeValueAsString(Collections.singletonList(response));

        MvcResult result = http.perform(post("/exam/{id}/page/{position}/responses", examInfo.getExamId(), pagePosition)
            .contentType(MediaType.APPLICATION_JSON)
            .content(examItemResponseJson)
            .param("sessionId", examInfo.getSessionId().toString())
            .param("browserId", examInfo.getBrowserId().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("data").isNotEmpty())
            .andExpect(jsonPath("data.id", is(mockNextExamPage.getId().toString())))
            .andExpect(jsonPath("data.pagePosition", is(mockNextExamPage.getPagePosition())))
            .andExpect(jsonPath("data.segmentKey", is(mockNextExamPage.getSegmentKey())))
            .andExpect(jsonPath("data.segmentId", is(mockNextExamPage.getSegmentId())))
            .andExpect(jsonPath("data.segmentPosition", is(mockNextExamPage.getSegmentPosition())))
            .andExpect(jsonPath("data.itemGroupKey", is(mockNextExamPage.getItemGroupKey())))
            .andExpect(jsonPath("data.groupItemsRequired", is(mockNextExamPage.isGroupItemsRequired())))
            .andExpect(jsonPath("data.examId", is(mockNextExamPage.getExamId().toString())))
            .andExpect(jsonPath("data.examItems").isArray())
            .andExpect(jsonPath("data.examItems[0].id", is(mockExamItemId.toString())))
            .andExpect(jsonPath("data.examItems[0].examPageId", is(mockNextExamPage.getId().toString())))
            .andExpect(jsonPath("data.examItems[0].itemKey", is(mockExamItem.getItemKey())))
            .andExpect(jsonPath("data.examItems[0].assessmentItemBankKey", is((int) mockExamItem.getAssessmentItemBankKey())))
            .andExpect(jsonPath("data.examItems[0].assessmentItemKey", is((int) mockExamItem.getAssessmentItemKey())))
            .andExpect(jsonPath("data.examItems[0].itemType", is(mockExamItem.getItemType())))
            .andExpect(jsonPath("data.examItems[0].position", is(mockExamItem.getPosition())))
            .andExpect(jsonPath("data.examItems[0].required", is(mockExamItem.isRequired())))
            .andExpect(jsonPath("data.examItems[0].fieldTest", is(mockExamItem.isFieldTest())))
            .andExpect(jsonPath("data.examItems[0].itemFilePath", is(mockExamItem.getItemFilePath())))
            .andExpect(jsonPath("data.examItems[0].stimulusFilePath", is(mockExamItem.getStimulusFilePath().get())))
            .andExpect(jsonPath("data.examItems[0].response").exists())
            .andExpect(jsonPath("data.examItems[0].response.id", is((int) mockExamItemResponse.getId())))
            .andExpect(jsonPath("data.examItems[0].response.examItemId", is(mockExamItemResponse.getExamItemId().toString())))
            .andExpect(jsonPath("data.examItems[0].response.response", is(mockExamItemResponse.getResponse())))
            .andExpect(jsonPath("data.examItems[0].response.sequence", is(mockExamItemResponse.getSequence())))
            .andExpect(jsonPath("data.examItems[0].response.valid", is(mockExamItemResponse.isValid())))
            .andExpect(jsonPath("data.examItems[0].response.selected", is(mockExamItemResponse.isSelected())))
            .andExpect(jsonPath("data.examItems[0].response.score").doesNotExist())
            .andExpect(jsonPath("data.createdAt", is(mockNextExamPage.getCreatedAt().toString())))
            .andReturn();

        JavaType type = objectMapper.getTypeFactory().constructParametricType(Response.class, ExamPage.class);
        objectMapper.readValue(result.getResponse().getContentAsByteArray(), type);
    }

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
