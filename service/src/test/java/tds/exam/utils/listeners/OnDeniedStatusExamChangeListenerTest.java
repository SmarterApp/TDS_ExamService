package tds.exam.utils.listeners;

import org.joda.time.Instant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import tds.common.entity.utils.ChangeListener;
import tds.exam.Exam;
import tds.exam.ExamStatusCode;
import tds.exam.builder.ExamBuilder;
import tds.exam.services.ExamAccommodationService;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class OnDeniedStatusExamChangeListenerTest {
    private ChangeListener<Exam> onDeniedStatusExamChangeListener;

    @Mock
    private ExamAccommodationService mockExamAccommodationService;

    @Before
    public void setup() {
        onDeniedStatusExamChangeListener = new OnDeniedStatusExamChangeListener(mockExamAccommodationService);
    }

    @Test
    public void shouldCallDenyAccommodationsIfExamHasBeenUpdatedToDenied() {
        Exam exam = new ExamBuilder().build();
        Exam deniedExam = new Exam.Builder()
            .fromExam(exam)
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_DENIED), Instant.now())
            .build();

        onDeniedStatusExamChangeListener.accept(exam, deniedExam);
        verify(mockExamAccommodationService).denyAccommodations(exam.getId());
    }

    @Test
    public void shouldNotCallDenyIfExamIsNotDenied() {
        Exam exam = new ExamBuilder().build();
        Exam deniedExam = new Exam.Builder()
            .fromExam(exam)
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_APPROVED), Instant.now())
            .build();

        onDeniedStatusExamChangeListener.accept(exam, deniedExam);
        verify(mockExamAccommodationService, never()).denyAccommodations(exam.getId());
    }
}
