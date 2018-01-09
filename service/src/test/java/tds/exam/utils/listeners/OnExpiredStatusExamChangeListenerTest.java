package tds.exam.utils.listeners;

import org.joda.time.Instant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import tds.common.EntityUpdate;
import tds.exam.Exam;
import tds.exam.ExamStatusCode;
import tds.exam.builder.ExamBuilder;
import tds.exam.services.MessagingService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(MockitoJUnitRunner.class)
public class OnExpiredStatusExamChangeListenerTest {
    @Mock
    private MessagingService messagingService;

    private OnExpiredStatusExamChangeListener changeListener;

    @Before
    public void setUp() {
        changeListener = new OnExpiredStatusExamChangeListener(messagingService);
    }

    @Test
    public void shouldSendTRTMessage() {
        Exam newExam = new ExamBuilder().withStatus(new ExamStatusCode(ExamStatusCode.STATUS_EXPIRED), Instant.now()).build();
        Exam oldExam = new ExamBuilder().withStatus(new ExamStatusCode(ExamStatusCode.STATUS_STARTED), Instant.now()).build();

        changeListener.accept(new EntityUpdate<>(oldExam, newExam));
        verify(messagingService).sendExamCompletion(newExam.getId());
    }

    @Test
    public void shouldDoNothingIfStatusHasNotChanged() {
        Exam newExam = new ExamBuilder().withStatus(new ExamStatusCode(ExamStatusCode.STATUS_EXPIRED), Instant.now()).build();
        Exam oldExam = new ExamBuilder().withStatus(new ExamStatusCode(ExamStatusCode.STATUS_EXPIRED), Instant.now()).build();

        changeListener.accept(new EntityUpdate<>(oldExam, newExam));
        verifyZeroInteractions(messagingService);
    }

    @Test
    public void shouldDoNothingIfStatusNotExpired() {
        Exam newExam = new ExamBuilder().withStatus(new ExamStatusCode(ExamStatusCode.STATUS_SUBMITTED), Instant.now()).build();
        Exam oldExam = new ExamBuilder().withStatus(new ExamStatusCode(ExamStatusCode.STATUS_EXPIRED), Instant.now()).build();

        changeListener.accept(new EntityUpdate<>(oldExam, newExam));
        verifyZeroInteractions(messagingService);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIfUpdatedEntityNull() {
        Exam exam = new ExamBuilder().withStatus(new ExamStatusCode(ExamStatusCode.STATUS_EXPIRED), Instant.now()).build();

        changeListener.accept(new EntityUpdate<>(exam, null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIfExistingEntityNull() {
        Exam exam = new ExamBuilder().withStatus(new ExamStatusCode(ExamStatusCode.STATUS_EXPIRED), Instant.now()).build();

        changeListener.accept(new EntityUpdate<>(null, exam));
    }
}