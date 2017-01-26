package tds.exam.web.endpoints;

import org.joda.time.Instant;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
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
import tds.common.ValidationError;
import tds.common.configuration.JacksonObjectMapperConfiguration;
import tds.common.web.advice.ExceptionAdvice;
import tds.exam.ApprovalRequest;
import tds.exam.ExamItem;
import tds.exam.ExamItemResponse;
import tds.exam.ExamPage;
import tds.exam.builder.ExamItemBuilder;
import tds.exam.builder.ExamPageBuilder;
import tds.exam.error.ValidationErrorCode;
import tds.exam.services.ExamPageService;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(ExamPageController.class)
@Import({ExceptionAdvice.class, JacksonObjectMapperConfiguration.class})
public class ExamPageControllerIntegrationTests {
    @Autowired
    private MockMvc http;

    @MockBean
    private ExamPageService mockExamPageService;

    @Test
    public void shouldGetAnExamPage() throws Exception {
        ExamItem examItem = new ExamItemBuilder()
            .withStimulusFilePath("/path/to/stimulus/187-1234.xml")
            .build();
        List<ExamItem> examItems = Arrays.asList(examItem);

        ExamPage examPage = new ExamPageBuilder()
            .withExamItems(examItems)
            .build();
        ApprovalRequest approvalRequest = new ApprovalRequest(examPage.getExamId(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            "UNIT_TEST");

        ArgumentCaptor<ApprovalRequest> approvalRequestArgumentCaptor = ArgumentCaptor.forClass(ApprovalRequest.class);
        when(mockExamPageService.getPage(isA(ApprovalRequest.class), isA(Integer.class)))
            .thenReturn(new Response<>(examPage));

        http.perform(get("/exam/{id}/page/{position}", examPage.getExamId(), examPage.getPagePosition())
            .contentType(MediaType.APPLICATION_JSON)
            .param("sessionId", approvalRequest.getSessionId().toString())
            .param("browserId", approvalRequest.getBrowserId().toString())
            .param("clientName", approvalRequest.getClientName()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("data").isNotEmpty())
            .andExpect(jsonPath("data.id", is((int) examPage.getId())))
            .andExpect(jsonPath("data.pagePosition", is(examPage.getPagePosition())))
            .andExpect(jsonPath("data.segmentKey", is(examPage.getSegmentKey())))
            .andExpect(jsonPath("data.segmentId", is(examPage.getSegmentId())))
            .andExpect(jsonPath("data.segmentPosition", is(examPage.getSegmentPosition())))
            .andExpect(jsonPath("data.itemGroupKey", is(examPage.getItemGroupKey())))
            .andExpect(jsonPath("data.groupItemsRequired", is(examPage.isGroupItemsRequired())))
            .andExpect(jsonPath("data.examItems").isArray())
            .andExpect(jsonPath("data.examItems[0].id", is(((int) examItem.getId()))))
            .andExpect(jsonPath("data.examItems[0].examPageId", is(((int) examItem.getExamPageId()))))
            .andExpect(jsonPath("data.examItems[0].assessmentItemBankKey", is((int) examItem.getAssessmentItemBankKey())))
            .andExpect(jsonPath("data.examItems[0].assessmentItemKey", is((int) examItem.getAssessmentItemKey())))
            .andExpect(jsonPath("data.examItems[0].itemType", is(examItem.getItemType())))
            .andExpect(jsonPath("data.examItems[0].position", is(examItem.getPosition())))
            .andExpect(jsonPath("data.examItems[0].required", is(examItem.isRequired())))
            .andExpect(jsonPath("data.examItems[0].selected", is(examItem.isSelected())))
            .andExpect(jsonPath("data.examItems[0].markedForReview", is(examItem.isMarkedForReview())))
            .andExpect(jsonPath("data.examItems[0].fieldTest", is(examItem.isFieldTest())))
            .andExpect(jsonPath("data.examItems[0].itemFilePath", is(examItem.getItemFilePath())))
            .andExpect(jsonPath("data.examItems[0].stimulusFilePath", is(examItem.getStimulusFilePath().get())));

        verify(mockExamPageService).getPage(approvalRequestArgumentCaptor.capture(), isA(Integer.class));
    }

    @Test
    public void shouldGetAnExamPageWithAnItemThatHasAResponse() throws Exception {
        ExamItemResponse response = new ExamItemResponse.Builder()
            .withResponse("response text")
            .withValid(true)
            .withCreatedAt(Instant.now().minus(20000))
            .withExamItemId(ExamItemBuilder.EXAM_ITEM_DEFAULT_ID)
            .build();
        ExamItem examItem = new ExamItemBuilder().withStimulusFilePath("foo/bar")
            .withResponse(response)
            .build();
        List<ExamItem> examItems = Arrays.asList(examItem);

        ExamPage examPage = new ExamPageBuilder()
            .withExamItems(examItems)
            .build();
        ApprovalRequest approvalRequest = new ApprovalRequest(examPage.getExamId(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            "UNIT_TEST");

        ArgumentCaptor<ApprovalRequest> approvalRequestArgumentCaptor = ArgumentCaptor.forClass(ApprovalRequest.class);
        when(mockExamPageService.getPage(isA(ApprovalRequest.class), isA(Integer.class)))
            .thenReturn(new Response<>(examPage));

        http.perform(get("/exam/{id}/page/{position}", examPage.getExamId(), examPage.getPagePosition())
            .contentType(MediaType.APPLICATION_JSON)
            .param("sessionId", approvalRequest.getSessionId().toString())
            .param("browserId", approvalRequest.getBrowserId().toString())
            .param("clientName", approvalRequest.getClientName()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("data").isNotEmpty())
            .andExpect(jsonPath("data.id", is((int) examPage.getId())))
            .andExpect(jsonPath("data.pagePosition", is(examPage.getPagePosition())))
            .andExpect(jsonPath("data.segmentKey", is(examPage.getSegmentKey())))
            .andExpect(jsonPath("data.segmentId", is(examPage.getSegmentId())))
            .andExpect(jsonPath("data.segmentPosition", is(examPage.getSegmentPosition())))
            .andExpect(jsonPath("data.itemGroupKey", is(examPage.getItemGroupKey())))
            .andExpect(jsonPath("data.groupItemsRequired", is(examPage.isGroupItemsRequired())))
            .andExpect(jsonPath("data.examItems").isArray())
            .andExpect(jsonPath("data.examItems[0].id", is(((int) examItem.getId()))))
            .andExpect(jsonPath("data.examItems[0].examPageId", is(((int) examItem.getExamPageId()))))
            .andExpect(jsonPath("data.examItems[0].assessmentItemBankKey", is((int) examItem.getAssessmentItemBankKey())))
            .andExpect(jsonPath("data.examItems[0].assessmentItemKey", is((int) examItem.getAssessmentItemKey())))
            .andExpect(jsonPath("data.examItems[0].itemType", is(examItem.getItemType())))
            .andExpect(jsonPath("data.examItems[0].position", is(examItem.getPosition())))
            .andExpect(jsonPath("data.examItems[0].required", is(examItem.isRequired())))
            .andExpect(jsonPath("data.examItems[0].selected", is(examItem.isSelected())))
            .andExpect(jsonPath("data.examItems[0].markedForReview", is(examItem.isMarkedForReview())))
            .andExpect(jsonPath("data.examItems[0].fieldTest", is(examItem.isFieldTest())))
            .andExpect(jsonPath("data.examItems[0].itemFilePath", is(examItem.getItemFilePath())))
            .andExpect(jsonPath("data.examItems[0].stimulusFilePath", is(examItem.getStimulusFilePath().get())))
            .andExpect(jsonPath("data.examItems[0].response.response", is(response.getResponse())))
            .andExpect(jsonPath("data.examItems[0].response.valid", is(response.isValid())))
            .andExpect(jsonPath("data.examItems[0].response.examItemId", is((int) response.getExamItemId())));

        verify(mockExamPageService).getPage(approvalRequestArgumentCaptor.capture(), isA(Integer.class));
    }

    @Test
    public void shouldNotGetAnExamPageForAClosedSession() throws Exception {
        ApprovalRequest approvalRequest = new ApprovalRequest(UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            "UNIT_TEST");

        ArgumentCaptor<ApprovalRequest> approvalRequestArgumentCaptor = ArgumentCaptor.forClass(ApprovalRequest.class);
        when(mockExamPageService.getPage(isA(ApprovalRequest.class), isA(Integer.class)))
            .thenReturn(new Response<>(new ValidationError(ValidationErrorCode.EXAM_APPROVAL_SESSION_CLOSED, "session is closed")));

        http.perform(get("/exam/{id}/page/{position}", approvalRequest.getExamId(), 1)
            .contentType(MediaType.APPLICATION_JSON)
            .param("sessionId", approvalRequest.getSessionId().toString())
            .param("browserId", approvalRequest.getBrowserId().toString())
            .param("clientName", approvalRequest.getClientName()))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("error").isNotEmpty())
            .andExpect(jsonPath("error.code", is("sessionClosed")))
            .andExpect(jsonPath("error.message", is("session is closed")));

        verify(mockExamPageService).getPage(approvalRequestArgumentCaptor.capture(), isA(Integer.class));
    }
}
