package tds.exam.utils.listeners;

import org.joda.time.Instant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import tds.common.Response;
import tds.common.entity.utils.ChangeListener;
import tds.common.web.exceptions.NotFoundException;
import tds.exam.Exam;
import tds.exam.ExamSegment;
import tds.exam.ExamStatusCode;
import tds.exam.ExamStatusStage;
import tds.exam.builder.ExamBuilder;
import tds.exam.builder.ExamSegmentBuilder;
import tds.exam.services.ExamSegmentService;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OnPausedStatusExamChangeListenerTest {
    @Mock
    private ExamSegmentService mockExamSegmentService;

    private ChangeListener<Exam> onPausedStatusExamChangeListener;

    @Before
    public void setUp() {
        onPausedStatusExamChangeListener = new OnPausedStatusExamChangeListener(mockExamSegmentService);
    }

    @Test
    public void shouldUpdateExamSegmentWhenNewExamStatusIsSetToPaused() {
        Exam oldExam = new ExamBuilder().build();
        Exam newExam = new ExamBuilder()
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_PAUSED, ExamStatusStage.IN_USE), Instant.now())
            .build();
        ExamSegment mockSegment = new ExamSegmentBuilder()
            .withExamId(newExam.getId())
            .withIsPermeable(true)
            .withRestorePermeableCondition("segment")
            .build();

        when(mockExamSegmentService.findByExamIdAndSegmentPosition(newExam.getId(),
            newExam.getCurrentSegmentPosition()))
            .thenReturn(new Response<>(mockSegment));

        onPausedStatusExamChangeListener.accept(oldExam, newExam);
        verify(mockExamSegmentService).findByExamIdAndSegmentPosition(newExam.getId(),
            newExam.getCurrentSegmentPosition());
        verify(mockExamSegmentService).update(any(ExamSegment.class));
    }

    @Test
    public void shouldDoNothingWhenOldExamAndNewExamHaveSameStatus() {
        Exam oldExam = new ExamBuilder().build();
        Exam newExam = new ExamBuilder().build();

        onPausedStatusExamChangeListener.accept(oldExam, newExam);
        verifyZeroInteractions(mockExamSegmentService);
    }

    @Test
    public void shouldDoNothingWhenNewExamStatusIsNotSetToPaused() {
        Exam oldExam = new ExamBuilder()
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_APPROVED, ExamStatusStage.OPEN), Instant.now())
            .build();
        Exam newExam = new ExamBuilder()
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_STARTED, ExamStatusStage.IN_PROGRESS), Instant.now())
            .build();

        onPausedStatusExamChangeListener.accept(oldExam, newExam);
        verifyZeroInteractions(mockExamSegmentService);
    }

    @Test
    public void shouldNotUpdateExamSegmentIfExamSegmentIsNotPermeable() {
        Exam oldExam = new ExamBuilder().build();
        Exam newExam = new ExamBuilder()
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_PAUSED, ExamStatusStage.INACTIVE), Instant.now())
            .build();
        ExamSegment mockSegment = new ExamSegmentBuilder()
            .withExamId(newExam.getId())
            .withIsPermeable(false)
            .build();

        when(mockExamSegmentService.findByExamIdAndSegmentPosition(newExam.getId(),
            newExam.getCurrentSegmentPosition()))
            .thenReturn(new Response<>(mockSegment));

        onPausedStatusExamChangeListener.accept(oldExam, newExam);
        verify(mockExamSegmentService).findByExamIdAndSegmentPosition(newExam.getId(),
            newExam.getCurrentSegmentPosition());
        verify(mockExamSegmentService, never()).update(anyVararg());
    }

    @Test
    public void shouldNotUpdateExamSegmentIfExamSegmentRestorePermeableConditionIsNotSetToSegmentOrPaused() {
        Exam oldExam = new ExamBuilder().build();
        Exam newExam = new ExamBuilder()
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_PAUSED, ExamStatusStage.INACTIVE), Instant.now())
            .build();
        ExamSegment mockSegment = new ExamSegmentBuilder()
            .withExamId(newExam.getId())
            .withIsPermeable(true)
            .withRestorePermeableCondition("unit test") // should only update segment when this is "segment" or "paused"
            .build();

        when(mockExamSegmentService.findByExamIdAndSegmentPosition(newExam.getId(),
            newExam.getCurrentSegmentPosition()))
            .thenReturn(new Response<>(mockSegment));

        onPausedStatusExamChangeListener.accept(oldExam, newExam);
        verify(mockExamSegmentService).findByExamIdAndSegmentPosition(newExam.getId(),
            newExam.getCurrentSegmentPosition());
        verify(mockExamSegmentService, never()).update(anyVararg());
    }

    @Test(expected = NotFoundException.class)
    public void shouldThrowNotFoundExceptionWhenExamSegmentCannotBeFound() {
        Exam oldExam = new ExamBuilder().build();
        Exam newExam = new ExamBuilder()
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_PAUSED, ExamStatusStage.INACTIVE), Instant.now())
            .build();

        when(mockExamSegmentService.findByExamIdAndSegmentPosition(newExam.getId(),
            newExam.getCurrentSegmentPosition()))
            .thenThrow(new NotFoundException("Could not find exam segment"));

        onPausedStatusExamChangeListener.accept(oldExam, newExam);
        verify(mockExamSegmentService, never()).update(anyVararg());
    }
}
