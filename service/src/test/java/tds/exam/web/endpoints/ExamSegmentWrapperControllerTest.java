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

import tds.common.web.exceptions.NotFoundException;
import tds.exam.services.ExamSegmentWrapperService;
import tds.exam.wrapper.ExamSegmentWrapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExamSegmentWrapperControllerTest {
    @Mock
    private ExamSegmentWrapperService mockExamSegmentWrapperService;

    private ExamSegmentWrapperController examSegmentWrapperController;

    @Before
    public void setUp() {
        examSegmentWrapperController = new ExamSegmentWrapperController(mockExamSegmentWrapperService);
    }

    @Test(expected = NotFoundException.class)
    public void shouldThrowNotFoundWhenExamSegmentWrappersCannotBeFound() {
        UUID examId = UUID.randomUUID();

        when(mockExamSegmentWrapperService.findAllExamSegments(examId)).thenReturn(Collections.emptyList());

        examSegmentWrapperController.findExamSegmentWrappersForExam(examId, null);
    }

    @Test
    public void shouldReturnExamSegmentWrappersByExamId() {
        UUID examId = UUID.randomUUID();
        ExamSegmentWrapper wrapper = mock(ExamSegmentWrapper.class);

        when(mockExamSegmentWrapperService.findAllExamSegments(examId)).thenReturn(Collections.singletonList(wrapper));

        ResponseEntity<List<ExamSegmentWrapper>> response = examSegmentWrapperController.findExamSegmentWrappersForExam(examId, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly(wrapper);
    }

    @Test (expected = NotFoundException.class)
    public void shouldThrowNotFoundWhenExamSegmentWrappersCannotBeFoundAtPagePosition() {
        UUID examId = UUID.randomUUID();

        when(mockExamSegmentWrapperService.findExamSegmentWithPageAtPosition(examId, 1)).thenReturn(Optional.empty());

        examSegmentWrapperController.findExamSegmentWrappersForExam(examId, 1);
    }

    @Test
    public void shouldReturnExamSegmentWrappersByExamIdAndPagePosition() {
        UUID examId = UUID.randomUUID();
        ExamSegmentWrapper wrapper = mock(ExamSegmentWrapper.class);

        when(mockExamSegmentWrapperService.findExamSegmentWithPageAtPosition(examId,1)).thenReturn(Optional.of(wrapper));

        ResponseEntity<List<ExamSegmentWrapper>> response = examSegmentWrapperController.findExamSegmentWrappersForExam(examId, 1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly(wrapper);
    }

    @Test(expected = NotFoundException.class)
    public void shouldThrowNotFoundIfExamSegmentWrapperCannotBeFoundAtExamIdAndSegmentPosition() {
        UUID examId = UUID.randomUUID();

        when(mockExamSegmentWrapperService.findExamSegment(examId, 1)).thenReturn(Optional.empty());
        examSegmentWrapperController.findExamSegmentWrapperForExamAndSegmentPosition(examId, 1, null);
    }

    @Test
    public void shouldReturnExamSegmentWrapperAtExamIdAndSegmentPosition() {
        UUID examId = UUID.randomUUID();
        ExamSegmentWrapper wrapper = mock(ExamSegmentWrapper.class);

        when(mockExamSegmentWrapperService.findExamSegment(examId, 1)).thenReturn(Optional.of(wrapper));
        ResponseEntity<ExamSegmentWrapper> response = examSegmentWrapperController.findExamSegmentWrapperForExamAndSegmentPosition(examId, 1, null);

        verify(mockExamSegmentWrapperService).findExamSegment(examId, 1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(wrapper);
    }

    @Test(expected = NotFoundException.class)
    public void shouldThrowNotFoundIfExamSegmentWrapperCannotBeFoundAtExamIdAndSegmentPositionAndPage() {
        UUID examId = UUID.randomUUID();

        when(mockExamSegmentWrapperService.findExamSegmentWithPageAtPosition(examId, 1, 2)).thenReturn(Optional.empty());
        examSegmentWrapperController.findExamSegmentWrapperForExamAndSegmentPosition(examId, 1, 2);
    }

    @Test
    public void shouldReturnExamSegmentWrapperAtExamIdAndSegmentPositionAndPage() {
        UUID examId = UUID.randomUUID();
        ExamSegmentWrapper wrapper = mock(ExamSegmentWrapper.class);

        when(mockExamSegmentWrapperService.findExamSegmentWithPageAtPosition(examId, 1, 2)).thenReturn(Optional.of(wrapper));
        ResponseEntity<ExamSegmentWrapper> response = examSegmentWrapperController.findExamSegmentWrapperForExamAndSegmentPosition(examId, 1, 2);

        verify(mockExamSegmentWrapperService).findExamSegmentWithPageAtPosition(examId, 1, 2);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(wrapper);
    }
}