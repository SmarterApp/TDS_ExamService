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
import tds.common.web.exceptions.NotFoundException;
import tds.common.web.resources.NoContentResponseResource;
import tds.exam.ExamSegment;
import tds.exam.services.ExamSegmentService;
import tds.exam.wrapper.ExamSegmentWrapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExamSegmentControllerTest {
    @Mock
    private ExamSegmentService mockExamSegmentService;

    private ExamSegmentController examSegmentController;

    @Before
    public void setUp() {
        examSegmentController = new ExamSegmentController(mockExamSegmentService);
    }

    @Test
    public void shouldCheckIfSegmentIsComplete() {
        UUID examId = UUID.randomUUID();

        when(mockExamSegmentService.checkIfSegmentsCompleted(examId)).thenReturn(true);

        ResponseEntity<Boolean> response = examSegmentController.checkIfSegmentsCompleted(examId);

        verify(mockExamSegmentService).checkIfSegmentsCompleted(examId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isTrue();
    }

    @Test
    public void shouldExitSegment() {
        UUID examId = UUID.randomUUID();

        when(mockExamSegmentService.exitSegment(examId, 1)).thenReturn(Optional.empty());

        ResponseEntity<NoContentResponseResource> response = examSegmentController.exitSegment(examId, 1);

        verify(mockExamSegmentService).exitSegment(examId, 1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    public void shouldFailToExitSegment() {
        UUID examId = UUID.randomUUID();

        when(mockExamSegmentService.exitSegment(examId, 1)).thenReturn(Optional.of(new ValidationError("code","fail")));

        ResponseEntity<NoContentResponseResource> response = examSegmentController.exitSegment(examId, 1);

        verify(mockExamSegmentService).exitSegment(examId, 1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    public void shouldGetSegments() {
        UUID examId = UUID.randomUUID();
        ExamSegment examSegment = new ExamSegment.Builder().build();

        when(mockExamSegmentService.findExamSegments(examId)).thenReturn(Collections.singletonList(examSegment));

        ResponseEntity<List<ExamSegment>> responseEntity = examSegmentController.getExamSegments(examId);

        verify(mockExamSegmentService).findExamSegments(examId);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).containsExactly(examSegment);
    }

    @Test(expected = NotFoundException.class)
    public void shouldThrowNotFoundWhenExamSegmentWrappersCannotBeFound() {
        UUID examId = UUID.randomUUID();

        when(mockExamSegmentService.findAllExamSegments(examId)).thenReturn(Collections.emptyList());

        examSegmentController.findExamSegmentWrappersForExam(examId);
    }

    @Test
    public void shouldReturnExamSegmentWrappersByExamId() {
        UUID examId = UUID.randomUUID();
        ExamSegmentWrapper wrapper = mock(ExamSegmentWrapper.class);

        when(mockExamSegmentService.findAllExamSegments(examId)).thenReturn(Collections.singletonList(wrapper));

        ResponseEntity<List<ExamSegmentWrapper>> response = examSegmentController.findExamSegmentWrappersForExam(examId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly(wrapper);
    }

    @Test (expected = NotFoundException.class)
    public void shouldThrowNotFoundIfExamSegmentWrapperCannotBeFoundAtExamIdAndSegmentPosition() {
        UUID examId = UUID.randomUUID();

        when(mockExamSegmentService.findExamSegment(examId, 1)).thenReturn(Optional.empty());
        examSegmentController.findExamSegmentWrapperForExamAndSegmentPosition(examId, 1, null);
    }

    @Test
    public void shouldReturnExamSegmentWrapperAtExamIdAndSegmentPosition() {
        UUID examId = UUID.randomUUID();
        ExamSegmentWrapper wrapper = mock(ExamSegmentWrapper.class);

        when(mockExamSegmentService.findExamSegment(examId, 1)).thenReturn(Optional.of(wrapper));
        ResponseEntity<ExamSegmentWrapper> response = examSegmentController.findExamSegmentWrapperForExamAndSegmentPosition(examId, 1, null);

        verify(mockExamSegmentService).findExamSegment(examId, 1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(wrapper);
    }

    @Test (expected = NotFoundException.class)
    public void shouldThrowNotFoundIfExamSegmentWrapperCannotBeFoundAtExamIdAndSegmentPositionAndPage() {
        UUID examId = UUID.randomUUID();

        when(mockExamSegmentService.findExamSegmentWithPageAtPosition(examId, 1, 2)).thenReturn(Optional.empty());
        examSegmentController.findExamSegmentWrapperForExamAndSegmentPosition(examId, 1, 2);
    }

    @Test
    public void shouldReturnExamSegmentWrapperAtExamIdAndSegmentPositionAndPage() {
        UUID examId = UUID.randomUUID();
        ExamSegmentWrapper wrapper = mock(ExamSegmentWrapper.class);

        when(mockExamSegmentService.findExamSegmentWithPageAtPosition(examId, 1, 2)).thenReturn(Optional.of(wrapper));
        ResponseEntity<ExamSegmentWrapper> response = examSegmentController.findExamSegmentWrapperForExamAndSegmentPosition(examId, 1, 2);

        verify(mockExamSegmentService).findExamSegmentWithPageAtPosition(examId, 1, 2);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(wrapper);
    }
}