package tds.exam.utils.statusvalidators;

import org.joda.time.Instant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import tds.common.ValidationError;
import tds.exam.Exam;
import tds.exam.ExamStatusCode;
import tds.exam.builder.ExamBuilder;
import tds.exam.services.ExamSegmentService;
import tds.exam.utils.ExamStatusChangeValidator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CompletedStatusChangeValidatorTest {
    private ExamStatusChangeValidator validator;

    @Mock
    private ExamSegmentService mockExamSegmentService;

    @Before
    public void setUp() {
        validator = new CompletedStatusChangeValidator(mockExamSegmentService);
    }

    @Test
    public void shouldValidateTransitioningToReviewStatusFromAValidPreviousStatus() {
        final Exam exam = new ExamBuilder()
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_STARTED), Instant.now())
            .build();
        final ExamStatusCode newStatus = new ExamStatusCode(ExamStatusCode.STATUS_REVIEW);

        when(mockExamSegmentService.checkIfSegmentsCompleted(exam.getId()))
            .thenReturn(true);

        final Optional<ValidationError> maybeValidationError = validator.validate(exam, newStatus);

        assertThat(maybeValidationError).isNotPresent();
    }

    @Test
    public void shouldReturnValidationErrorMessageWhenTheExamIsNotComplete() {
        final Exam exam = new ExamBuilder()
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_STARTED), Instant.now())
            .build();
        final ExamStatusCode newStatus = new ExamStatusCode(ExamStatusCode.STATUS_REVIEW);

        when(mockExamSegmentService.checkIfSegmentsCompleted(exam.getId()))
            .thenReturn(false);

        final Optional<ValidationError> maybeValidationError = validator.validate(exam, newStatus);

        assertThat(maybeValidationError).isPresent();
    }
}
