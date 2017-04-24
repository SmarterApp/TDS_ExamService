package tds.exam.web.endpoints;

import org.joda.time.Instant;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import tds.common.Response;
import tds.common.ValidationError;
import tds.exam.ExamInfo;
import tds.exam.ExamItem;
import tds.exam.ExamItemResponse;
import tds.exam.ExamPage;
import tds.exam.WebMvcControllerIntegrationTest;
import tds.exam.builder.ExamItemBuilder;
import tds.exam.builder.ExamPageBuilder;
import tds.exam.error.ValidationErrorCode;
import tds.exam.services.ExamPageService;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcControllerIntegrationTest(controllers = ExamPageController.class)
public class ExamPageControllerIntegrationTests {
    @Autowired
    private MockMvc http;

    @MockBean
    private ExamPageService mockExamPageService;

    @Test
    public void shouldGetAnExamPage() throws Exception {
        ExamItem mockExamItem = new ExamItemBuilder()
            .withStimulusFilePath("/path/to/stimulus/187-1234.xml")
            .build();
        List<ExamItem> mockExamItems = Arrays.asList(mockExamItem);

        ExamPage mockExamPage = new ExamPageBuilder()
            .withExamItems(mockExamItems)
            .build();
        ExamInfo examInfo = new ExamInfo(mockExamPage.getExamId(),
            UUID.randomUUID(),
            UUID.randomUUID());

        when(mockExamPageService.getPage(isA(UUID.class), isA(Integer.class)))
            .thenReturn(new Response<>(mockExamPage));

        http.perform(get("/exam/{id}/page/{position}", mockExamPage.getExamId(), mockExamPage.getPagePosition())
            .contentType(MediaType.APPLICATION_JSON)
            .param("sessionId", examInfo.getSessionId().toString())
            .param("browserId", examInfo.getBrowserId().toString()))
            .andExpect(status().isOk())

            .andExpect(jsonPath("data.id", is(mockExamPage.getId().toString())))
            .andExpect(jsonPath("data.pagePosition", is(mockExamPage.getPagePosition())))
            .andExpect(jsonPath("data.segmentKey", is(mockExamPage.getExamSegmentKey())))
            .andExpect(jsonPath("data.segmentId", is(mockExamPage.getSegmentId())))
            .andExpect(jsonPath("data.segmentPosition", is(mockExamPage.getSegmentPosition())))
            .andExpect(jsonPath("data.itemGroupKey", is(mockExamPage.getItemGroupKey())))
            .andExpect(jsonPath("data.groupItemsRequired", is(mockExamPage.isGroupItemsRequired())))
            .andExpect(jsonPath("data.examId", is(mockExamPage.getExamId().toString())))
            .andExpect(jsonPath("data.examItems").isArray())
            .andExpect(jsonPath("data.examItems[0].id", is((mockExamItem.getId().toString()))))
            .andExpect(jsonPath("data.examItems[0].examPageId", is((mockExamItem.getExamPageId().toString()))))
            .andExpect(jsonPath("data.examItems[0].assessmentItemBankKey", is((int) mockExamItem.getAssessmentItemBankKey())))
            .andExpect(jsonPath("data.examItems[0].assessmentItemKey", is((int) mockExamItem.getAssessmentItemKey())))
            .andExpect(jsonPath("data.examItems[0].itemType", is(mockExamItem.getItemType())))
            .andExpect(jsonPath("data.examItems[0].position", is(mockExamItem.getPosition())))
            .andExpect(jsonPath("data.examItems[0].required", is(mockExamItem.isRequired())))
            .andExpect(jsonPath("data.examItems[0].fieldTest", is(mockExamItem.isFieldTest())))
            .andExpect(jsonPath("data.examItems[0].itemFilePath", is(mockExamItem.getItemFilePath())))
            .andExpect(jsonPath("data.examItems[0].stimulusFilePath", is(mockExamItem.getStimulusFilePath().get())));

        verify(mockExamPageService).getPage(mockExamPage.getExamId(), mockExamPage.getPagePosition());
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
        ExamInfo examInfo = new ExamInfo(examPage.getExamId(),
            UUID.randomUUID(),
            UUID.randomUUID());

        when(mockExamPageService.getPage(isA(UUID.class), isA(Integer.class)))
            .thenReturn(new Response<>(examPage));

        http.perform(get("/exam/{id}/page/{position}", examPage.getExamId(), examPage.getPagePosition())
            .contentType(MediaType.APPLICATION_JSON)
            .param("sessionId", examInfo.getSessionId().toString())
            .param("browserId", examInfo.getBrowserId().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("data").isNotEmpty())
            .andExpect(jsonPath("data.id", is(examPage.getId().toString())))
            .andExpect(jsonPath("data.pagePosition", is(examPage.getPagePosition())))
            .andExpect(jsonPath("data.segmentKey", is(examPage.getExamSegmentKey())))
            .andExpect(jsonPath("data.segmentId", is(examPage.getSegmentId())))
            .andExpect(jsonPath("data.segmentPosition", is(examPage.getSegmentPosition())))
            .andExpect(jsonPath("data.itemGroupKey", is(examPage.getItemGroupKey())))
            .andExpect(jsonPath("data.groupItemsRequired", is(examPage.isGroupItemsRequired())))
            .andExpect(jsonPath("data.examId", is(examPage.getExamId().toString())))
            .andExpect(jsonPath("data.examItems").isArray())
            .andExpect(jsonPath("data.examItems[0].id", is((examItem.getId().toString()))))
            .andExpect(jsonPath("data.examItems[0].examPageId", is((examItem.getExamPageId().toString()))))
            .andExpect(jsonPath("data.examItems[0].assessmentItemBankKey", is((int) examItem.getAssessmentItemBankKey())))
            .andExpect(jsonPath("data.examItems[0].assessmentItemKey", is((int) examItem.getAssessmentItemKey())))
            .andExpect(jsonPath("data.examItems[0].itemType", is(examItem.getItemType())))
            .andExpect(jsonPath("data.examItems[0].position", is(examItem.getPosition())))
            .andExpect(jsonPath("data.examItems[0].required", is(examItem.isRequired())))
            .andExpect(jsonPath("data.examItems[0].fieldTest", is(examItem.isFieldTest())))
            .andExpect(jsonPath("data.examItems[0].itemFilePath", is(examItem.getItemFilePath())))
            .andExpect(jsonPath("data.examItems[0].stimulusFilePath", is(examItem.getStimulusFilePath().get())))
            .andExpect(jsonPath("data.examItems[0].response.response", is(response.getResponse())))
            .andExpect(jsonPath("data.examItems[0].response.valid", is(response.isValid())))
            .andExpect(jsonPath("data.examItems[0].response.examItemId", is(response.getExamItemId().toString())));

        verify(mockExamPageService).getPage(examPage.getExamId(), examPage.getPagePosition());
    }

    @Test
    public void shouldGetTwoPagesWithItems() throws Exception {
        final UUID examId = UUID.randomUUID();
        final UUID firstExamPageId = UUID.randomUUID();
        final UUID secondExamPageId = UUID.randomUUID();

        final ExamItem firstExamItem = new ExamItemBuilder()
            .withExamPageId(firstExamPageId)
            .withStimulusFilePath("/path/to/stimulus/187-1234.xml")
            .build();
        final ExamItem secondExamItem = new ExamItemBuilder()
            .withExamPageId(secondExamPageId)
            .withStimulusFilePath("/path/to/stimulus/187-5678.xml")
            .build();

        final ExamPage firstExamPage = new ExamPageBuilder()
            .withId(firstExamPageId)
            .withExamId(examId)
            .withExamItems(Collections.singletonList(firstExamItem))
            .build();
        final ExamPage secondExamPage = new ExamPageBuilder()
            .withId(secondExamPageId)
            .withExamId(examId)
            .withExamItems(Collections.singletonList(secondExamItem))
            .build();

        final ExamInfo examInfo = new ExamInfo(firstExamPage.getExamId(),
            UUID.randomUUID(),
            UUID.randomUUID());

        when(mockExamPageService.findAllPagesWithItems(isA(UUID.class)))
            .thenReturn(Arrays.asList(firstExamPage, secondExamPage));

        http.perform(get("/exam/{id}/page", examId)
            .contentType(MediaType.APPLICATION_JSON)
            .param("sessionId", examInfo.getSessionId().toString())
            .param("browserId", examInfo.getBrowserId().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].id", is(firstExamPageId.toString())))
            .andExpect(jsonPath("$[0].examId", is(examId.toString())))
            .andExpect(jsonPath("$[0].examItems", hasSize(1)))
            .andExpect(jsonPath("$[0].examItems[0].examPageId", is(firstExamPageId.toString())))
            .andExpect(jsonPath("$[0].examItems[0].itemFilePath", is(firstExamItem.getItemFilePath())))
            .andExpect(jsonPath("$[1].id", is(secondExamPageId.toString())))
            .andExpect(jsonPath("$[1].examId", is(examId.toString())))
            .andExpect(jsonPath("$[1].examItems", hasSize(1)))
            .andExpect(jsonPath("$[1].examItems[0].examPageId", is(secondExamPageId.toString())))
            .andExpect(jsonPath("$[1].examItems[0].stimulusFilePath", is(secondExamItem.getStimulusFilePath().get())));
    }

    @Test
    public void shouldNotGetAnExamPageForAClosedSession() throws Exception {
        ExamInfo examInfo = new ExamInfo(UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID());

        when(mockExamPageService.getPage(isA(UUID.class), isA(Integer.class)))
            .thenReturn(new Response<>(new ValidationError(ValidationErrorCode.EXAM_APPROVAL_SESSION_CLOSED, "session is closed")));

        http.perform(get("/exam/{id}/page/{position}", examInfo.getExamId(), 1)
            .contentType(MediaType.APPLICATION_JSON)
            .param("sessionId", examInfo.getSessionId().toString())
            .param("browserId", examInfo.getBrowserId().toString()))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("error").isNotEmpty())
            .andExpect(jsonPath("error.code", is("sessionClosed")))
            .andExpect(jsonPath("error.message", is("session is closed")));

        verify(mockExamPageService).getPage(examInfo.getExamId(), 1);
    }
}
