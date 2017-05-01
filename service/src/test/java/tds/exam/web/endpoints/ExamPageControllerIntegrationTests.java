package tds.exam.web.endpoints;

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

import tds.exam.ExamInfo;
import tds.exam.ExamItem;
import tds.exam.ExamPage;
import tds.exam.WebMvcControllerIntegrationTest;
import tds.exam.builder.ExamItemBuilder;
import tds.exam.builder.ExamPageBuilder;
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
        List<ExamItem> mockExamItems = Collections.singletonList(mockExamItem);

        ExamPage mockExamPage = new ExamPageBuilder()
            .withExamItems(mockExamItems)
            .build();

        UUID examId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        UUID browserId = UUID.randomUUID();

        when(mockExamPageService.getPage(examId, mockExamPage.getPagePosition()))
            .thenReturn(mockExamPage);

        http.perform(get("/exam/{id}/page/{position}", examId, mockExamPage.getPagePosition())
            .contentType(MediaType.APPLICATION_JSON)
            .param("sessionId", sessionId.toString())
            .param("browserId", browserId.toString()))
            .andExpect(status().isOk())

            .andExpect(jsonPath("id", is(mockExamPage.getId().toString())))
            .andExpect(jsonPath("pagePosition", is(mockExamPage.getPagePosition())))
            .andExpect(jsonPath("segmentKey", is(mockExamPage.getSegmentKey())))
            .andExpect(jsonPath("segmentId", is(mockExamPage.getSegmentId())))
            .andExpect(jsonPath("segmentPosition", is(mockExamPage.getSegmentPosition())))
            .andExpect(jsonPath("itemGroupKey", is(mockExamPage.getItemGroupKey())))
            .andExpect(jsonPath("groupItemsRequired", is(mockExamPage.isGroupItemsRequired())))
            .andExpect(jsonPath("examId", is(mockExamPage.getExamId().toString())))
            .andExpect(jsonPath("examItems").isArray())
            .andExpect(jsonPath("examItems[0].id", is((mockExamItem.getId().toString()))))
            .andExpect(jsonPath("examItems[0].examPageId", is((mockExamItem.getExamPageId().toString()))))
            .andExpect(jsonPath("examItems[0].assessmentItemBankKey", is((int) mockExamItem.getAssessmentItemBankKey())))
            .andExpect(jsonPath("examItems[0].assessmentItemKey", is((int) mockExamItem.getAssessmentItemKey())))
            .andExpect(jsonPath("examItems[0].itemType", is(mockExamItem.getItemType())))
            .andExpect(jsonPath("examItems[0].position", is(mockExamItem.getPosition())))
            .andExpect(jsonPath("examItems[0].required", is(mockExamItem.isRequired())))
            .andExpect(jsonPath("examItems[0].fieldTest", is(mockExamItem.isFieldTest())))
            .andExpect(jsonPath("examItems[0].itemFilePath", is(mockExamItem.getItemFilePath())))
            .andExpect(jsonPath("examItems[0].stimulusFilePath", is(mockExamItem.getStimulusFilePath().get())));

        verify(mockExamPageService).getPage(examId, mockExamPage.getPagePosition());
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
}
