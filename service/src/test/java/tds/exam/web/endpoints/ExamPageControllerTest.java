package tds.exam.web.endpoints;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import tds.common.Response;
import tds.exam.ExamInfo;
import tds.exam.ExamItem;
import tds.exam.ExamPage;
import tds.exam.builder.ExamItemBuilder;
import tds.exam.builder.ExamPageBuilder;
import tds.exam.services.ExamPageService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExamPageControllerTest {
    @Mock
    private ExamPageService mockExamPageService;

    private ExamPageController controller;

    @Before
    public void setUp() {
        controller = new ExamPageController(mockExamPageService);
    }

    @Test
    public void shouldGetAnExamPageWithItems() {
        UUID examId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        UUID browserId = UUID.randomUUID();
        int pageNumber = 1;

        ExamInfo mockExamInfo = new ExamInfo(examId, sessionId, browserId);

        ExamItem mockFirstExamItem = new ExamItemBuilder()
            .withExamPageId(ExamPageBuilder.DEFAULT_ID)
            .build();
        ExamItem mockSecondExamItem = new ExamItemBuilder()
            .withExamPageId(ExamPageBuilder.DEFAULT_ID)
            .withItemKey("187-5678")
            .withAssessmentItemKey(5678L)
            .build();

        List<ExamItem> mockExamItems = Arrays.asList(mockFirstExamItem, mockSecondExamItem);

        Response<ExamPage> mockExamPageResponse = new Response<>(new ExamPageBuilder()
            .withExamId(examId)
            .withExamItems(mockExamItems)
            .build());

        ArgumentCaptor<ExamInfo> approvalRequestArgumentCaptor = ArgumentCaptor.forClass(ExamInfo.class);
        when(mockExamPageService.getPage(isA(ExamInfo.class), isA(Integer.class)))
            .thenReturn(mockExamPageResponse);

        ResponseEntity<Response<ExamPage>> result = controller.getPage(examId, pageNumber, sessionId, browserId);
        verify(mockExamPageService).getPage(approvalRequestArgumentCaptor.capture(), isA(Integer.class));

        assertThat(approvalRequestArgumentCaptor.getValue()).isEqualTo(mockExamInfo);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getData().isPresent()).isTrue();
        assertThat(result.getBody().getError().isPresent()).isFalse();
    }
}
