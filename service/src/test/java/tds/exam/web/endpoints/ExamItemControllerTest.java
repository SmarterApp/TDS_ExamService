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

import tds.common.ValidationError;
import tds.common.web.resources.NoContentResponseResource;
import tds.exam.services.ExamItemSelectionService;
import tds.exam.services.ExamItemService;
import tds.student.sql.data.OpportunityItem;

import static org.assertj.core.api.Assertions.assertThat;
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
        when(mockExamItemSelectionService.createNextPageGroup(examId, 1, 2)).thenReturn(Collections.singletonList(item));

        ResponseEntity<List<OpportunityItem>> response = examItemController.getNextItemGroup(examId, 1, 2);

        verify(mockExamItemSelectionService).createNextPageGroup(examId, 1, 2);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly(item);
    }
}
