package tds.exam.web.endpoints;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import tds.exam.ExamPage;
import tds.exam.builder.ExamPageBuilder;
import tds.exam.services.ExamPageService;

import static org.assertj.core.api.Assertions.assertThat;
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
        int pageNumber = 1;

        ExamPage examPage = new ExamPageBuilder()
            .withExamId(examId)
            .build();

        when(mockExamPageService.getPage(examId, pageNumber))
            .thenReturn(examPage);

        ResponseEntity<ExamPage> result = controller.getPage(examId, pageNumber);
        verify(mockExamPageService).getPage(examId, pageNumber);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(examPage);
    }

    @Test
    public void shouldGetManyExamPagesWithItems() {
        final UUID examId = UUID.randomUUID();
        final UUID firstExamPageId = UUID.randomUUID();
        final UUID secondExamPageId = UUID.randomUUID();

        final ExamPage firstExamPage = new ExamPageBuilder()
            .withId(firstExamPageId)
            .withExamId(examId)
            .build();
        final ExamPage secondExamPage = new ExamPageBuilder()
            .withId(secondExamPageId)
            .withExamId(examId)
            .build();

        final List<ExamPage> examPages = Arrays.asList(firstExamPage, secondExamPage);

        when(mockExamPageService.findAllPagesWithItems(examId))
            .thenReturn(examPages);

        ResponseEntity<List<ExamPage>> result = controller.getAllPagesWithItems(examId);
        verify(mockExamPageService).findAllPagesWithItems(examId);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).containsExactly(firstExamPage, secondExamPage);
    }
}
