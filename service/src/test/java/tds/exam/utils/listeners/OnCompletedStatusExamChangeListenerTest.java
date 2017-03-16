package tds.exam.utils.listeners;

import org.joda.time.Instant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import tds.common.entity.utils.ChangeListener;
import tds.exam.Exam;
import tds.exam.ExamSegment;
import tds.exam.ExamStatusCode;
import tds.exam.ExamStatusStage;
import tds.exam.builder.ExamBuilder;
import tds.exam.builder.ExamSegmentBuilder;
import tds.exam.models.FieldTestItemGroup;
import tds.exam.services.ExamSegmentService;
import tds.exam.services.ExamineeService;
import tds.exam.services.FieldTestService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OnCompletedStatusExamChangeListenerTest {
    @Mock
    private ExamSegmentService mockExamSegmentService;

    @Mock
    private FieldTestService mockFieldTestService;

    @Mock
    private ExamineeService mockExamineeService;

    @Captor
    private ArgumentCaptor<ExamSegment> examSegmentsArgumentCaptor;

    private ChangeListener<Exam> onCompletedExamStatusChangeListener;

    @Before
    public void setUp() {
        onCompletedExamStatusChangeListener = new OnCompletedStatusExamChangeListener(mockExamSegmentService,
            mockFieldTestService,
            mockExamineeService);
    }

    @Test
    public void shouldUpdateFieldTestItemGroupsWhenAnExamIsSetToCompleted() {
        Exam oldExam = new ExamBuilder().build();
        Exam newExam = new ExamBuilder()
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_COMPLETED, ExamStatusStage.IN_PROGRESS), Instant.now())
            .build();
        ExamSegment mockSegment = new ExamSegmentBuilder()
            .withExamId(newExam.getId())
            .withIsPermeable(true)
            .withSegmentId("segment1")
            .build();
        ExamSegment mockSegment2 = new ExamSegmentBuilder()
            .withExamId(newExam.getId())
            .withIsPermeable(false)
            .withSegmentId("segment2")
            .build();
        ExamSegment mockSegment3 = new ExamSegmentBuilder()
            .withExamId(newExam.getId())
            .withIsPermeable(true)
            .withSegmentId("segment3")
            .build();
        FieldTestItemGroup mockFirstFtItemGroup = new FieldTestItemGroup.Builder()
            .withExamId(newExam.getId())
            .withGroupId("item-group-id")
            .withGroupKey("item-group-key")
            .withBlockId("A")
            .withPositionAdministered(1)
            .withAdministeredAt(java.time.Instant.now().minus(60L, ChronoUnit.SECONDS))
            .withLanguageCode("ENU")
            .withPosition(2)
            .withSegmentId(mockSegment.getSegmentId())
            .withSegmentKey(mockSegment.getSegmentKey())
            .withItemCount(1)
            .withSessionId(UUID.randomUUID())
            .build();
        FieldTestItemGroup mockSecondFtItemGroup = new FieldTestItemGroup.Builder()
            .withExamId(newExam.getId())
            .withGroupId("item-group-id-2")
            .withGroupKey("item-group-key-2")
            .withBlockId("B")
            .withLanguageCode("ENU")
            .withPosition(3)
            .withPositionAdministered(12)
            .withAdministeredAt(java.time.Instant.now().minus(60L, ChronoUnit.SECONDS))
            .withSegmentId(mockSegment.getSegmentId())
            .withSegmentKey(mockSegment.getSegmentKey())
            .withItemCount(2)
            .withSessionId(UUID.randomUUID())
            .build();

        when(mockExamSegmentService.findExamSegments(newExam.getId()))
            .thenReturn(Arrays.asList(mockSegment, mockSegment2, mockSegment3));
        when(mockFieldTestService.findUsageInExam(newExam.getId()))
            .thenReturn(Arrays.asList(mockFirstFtItemGroup, mockSecondFtItemGroup));

        onCompletedExamStatusChangeListener.accept(oldExam, newExam);
        verify(mockExamSegmentService).findExamSegments(newExam.getId());
        verify(mockFieldTestService).findUsageInExam(newExam.getId());
        verify(mockExamSegmentService).update(examSegmentsArgumentCaptor.capture());

        List<ExamSegment> examSegments = examSegmentsArgumentCaptor.getAllValues();

        //ExamSegment[] examSegments = examSegmentsArgumentCaptor.getValue();
        assertThat(examSegments).hasSize(2);
        assertThat(examSegments.get(0).isPermeable()).isFalse();
        assertThat(examSegments.get(0).getSegmentId()).isEqualTo("segment1");
        assertThat(examSegments.get(1).isPermeable()).isFalse();
        assertThat(examSegments.get(1).getSegmentId()).isEqualTo("segment3");
    }

    @Test
    public void shouldNotUpdateFieldTestItemGroupsIfNoneAreAdministeredInThisExam() {
        Exam oldExam = new ExamBuilder().build();
        Exam newExam = new ExamBuilder()
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_COMPLETED, ExamStatusStage.IN_PROGRESS), Instant.now())
            .build();
        ExamSegment mockSegment = new ExamSegmentBuilder()
            .withExamId(newExam.getId())
            .build();

        when(mockExamSegmentService.findExamSegments(newExam.getId()))
            .thenReturn(Arrays.asList(mockSegment));
        when(mockFieldTestService.findUsageInExam(newExam.getId()))
            .thenReturn(Collections.emptyList());

        onCompletedExamStatusChangeListener.accept(oldExam, newExam);
        verify(mockExamSegmentService).findExamSegments(newExam.getId());
        verify(mockFieldTestService).findUsageInExam(newExam.getId());
    }

    @Test
    public void shouldDoNothingWhenOldExamAndNewExamHaveTheSameStatus() {
        Exam oldExam = new ExamBuilder().build();
        Exam newExam = new ExamBuilder().build();

        onCompletedExamStatusChangeListener.accept(oldExam, newExam);
        verifyZeroInteractions(mockExamSegmentService);
        verifyZeroInteractions(mockExamineeService);
        verifyZeroInteractions(mockFieldTestService);
    }

    @Test
    public void shouldDoNothingWhenNewExamStatusIsNotSetToCompleted() {
        Exam oldExam = new ExamBuilder()
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_APPROVED, ExamStatusStage.OPEN), Instant.now())
            .build();
        Exam newExam = new ExamBuilder()
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_STARTED, ExamStatusStage.IN_PROGRESS), Instant.now())
            .build();

        onCompletedExamStatusChangeListener.accept(oldExam, newExam);
        verifyZeroInteractions(mockExamSegmentService);
        verifyZeroInteractions(mockExamineeService);
        verifyZeroInteractions(mockFieldTestService);
    }

    @Test
    public void shouldSucceedWhenExamSegmentsCannotBeFound() {
        Exam oldExam = new ExamBuilder().build();
        Exam newExam = new ExamBuilder()
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_COMPLETED, ExamStatusStage.IN_PROGRESS), Instant.now())
            .build();

        when(mockExamSegmentService.findExamSegments(newExam.getId()))
            .thenReturn(new ArrayList<>());

        onCompletedExamStatusChangeListener.accept(oldExam, newExam);
        verify(mockExamSegmentService).findExamSegments(any(UUID.class));
    }
}
