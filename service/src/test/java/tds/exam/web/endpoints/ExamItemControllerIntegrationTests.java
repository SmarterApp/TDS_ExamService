package tds.exam.web.endpoints;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import tds.common.Response;
import tds.common.configuration.JacksonObjectMapperConfiguration;
import tds.common.web.advice.ExceptionAdvice;
import tds.exam.ApprovalRequest;
import tds.exam.ExamItem;
import tds.exam.ExamItemResponse;
import tds.exam.ExamPage;
import tds.exam.builder.ExamItemBuilder;
import tds.exam.builder.ExamItemResponseBuilder;
import tds.exam.builder.ExamItemResponseScoreBuilder;
import tds.exam.builder.ExamPageBuilder;
import tds.exam.services.ExamItemService;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(ExamItemController.class)
@Import({ExceptionAdvice.class, JacksonObjectMapperConfiguration.class})
public class ExamItemControllerIntegrationTests {
    @Autowired
    private MockMvc http;

    @MockBean
    private ExamItemService mockExamItemService;

    @Test
    public void shouldInsertResponse() throws Exception {
        ApprovalRequest approvalRequest = new ApprovalRequest(UUID.randomUUID(),
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
        List<ExamItem> mockExamItems = Arrays.asList(mockExamItem);
        ExamPage mockNextExamPage = new ExamPageBuilder()
            .withExamId(approvalRequest.getExamId())
            .withPagePosition(2)
            .withExamItems(mockExamItems)
            .build();

        when(mockExamItemService.insertResponses(isA(ApprovalRequest.class),
            isA(Integer.class),
            any(ExamItemResponse[].class)))
            .thenReturn(new Response<>(mockNextExamPage));

        String examItemResponseJson = new ObjectMapper().writeValueAsString(Arrays.asList(response));

        http.perform(post("/exam/{id}/page/{position}/responses", approvalRequest.getExamId(), pagePosition)
            .contentType(MediaType.APPLICATION_JSON)
            .content(examItemResponseJson)
            .param("sessionId", approvalRequest.getSessionId().toString())
            .param("browserId", approvalRequest.getBrowserId().toString()))
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
            .andExpect(jsonPath("data.examItems[0].markedForReview", is(mockExamItem.isMarkedForReview())))
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
            .andExpect(jsonPath("data.createdAt", is(mockNextExamPage.getCreatedAt().toString())));
    }
}
