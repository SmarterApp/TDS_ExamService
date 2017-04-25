package tds.exam.web.endpoints;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import tds.common.Response;
import tds.common.ValidationError;
import tds.common.web.resources.NoContentResponseResource;
import tds.exam.ExamInfo;
import tds.exam.ExamItem;
import tds.exam.ExamItemResponse;
import tds.exam.ExamPage;
import tds.exam.builder.ExamItemBuilder;
import tds.exam.builder.ExamItemResponseBuilder;
import tds.exam.builder.ExamPageBuilder;
import tds.exam.services.ExamItemSelectionService;
import tds.exam.services.ExamItemService;
import tds.student.sql.data.OpportunityItem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExamItemControllerTest {
    @Mock
    private ExamItemService mockExamItemService;

    @Mock
    private ExamItemSelectionService mockExamItemSelectionService;

    private ExamItemController examItemController;

    @Before
    public void setUp() {
        examItemController = new ExamItemController(mockExamItemService, mockExamItemSelectionService);
    }

    @Test
    public void shouldInsertExamItemResponses() {
        ExamInfo mockExamInfo = new ExamInfo(UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID());
        ExamItem mockExamItem = new ExamItemBuilder().build();
        List<ExamItem> mockExamItems = Collections.singletonList(mockExamItem);
        ExamPage mockNextExamPage = new ExamPageBuilder()
            .withExamId(mockExamInfo.getExamId())
            .withPagePosition(2)
            .withExamItems(mockExamItems)
            .build();


        when(mockExamItemService.insertResponses(isA(UUID.class),
            isA(Integer.class),
            any(ExamItemResponse[].class)))
            .thenReturn(new Response<>(mockNextExamPage));

        ExamItemResponse[] responses = new ExamItemResponse[]{new ExamItemResponseBuilder().build()};

        ResponseEntity<Response<ExamPage>> result = examItemController.insertResponses(mockExamInfo.getExamId(),
            1,
            responses);
        verify(mockExamItemService).insertResponses(isA(UUID.class), isA(Integer.class), any(ExamItemResponse[].class));

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getData().isPresent()).isTrue();
        assertThat(result.getBody().getError().isPresent()).isFalse();
    }

    @Test
    public void shouldMarkItemForReview() {
        final UUID examId = UUID.randomUUID();
        final int position = 6;
        final boolean mark = true;

        when(mockExamItemService.markForReview(examId, position, mark)).thenReturn(Optional.empty());
        ResponseEntity<NoContentResponseResource> response = examItemController.markItemForReview(examId, position, mark);
        verify(mockExamItemService).markForReview(examId, position, mark);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    public void shouldFailToMarkItemForReview() {
        final UUID examId = UUID.randomUUID();
        final int position = 6;
        final boolean mark = true;

        when(mockExamItemService.markForReview(examId, position, mark)).thenReturn(Optional.of(new ValidationError("some", "error")));
        ResponseEntity<NoContentResponseResource> response = examItemController.markItemForReview(examId, position, mark);
        verify(mockExamItemService).markForReview(examId, position, mark);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    public void shouldReturnPageGroup() {
        UUID examId = UUID.randomUUID();
        OpportunityItem item = new OpportunityItem();
        when(mockExamItemSelectionService.createNextPageGroup(examId, 1)).thenReturn(Collections.singletonList(item));

        ResponseEntity<List<OpportunityItem>> response = examItemController.getNextItemGroup(examId, 1);

        verify(mockExamItemSelectionService).createNextPageGroup(examId, 1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly(item);
    }
}
