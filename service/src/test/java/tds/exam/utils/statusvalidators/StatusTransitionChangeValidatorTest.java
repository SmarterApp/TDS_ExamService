package tds.exam.utils.statusvalidators;

import org.joda.time.Instant;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import tds.common.ValidationError;
import tds.exam.Exam;
import tds.exam.ExamStatusCode;
import tds.exam.builder.ExamBuilder;
import tds.exam.error.ValidationErrorCode;
import tds.exam.utils.ExamStatusChangeValidator;

import static org.assertj.core.api.Assertions.assertThat;

public class StatusTransitionChangeValidatorTest {
    private ExamStatusChangeValidator defaultExamStatusChangeValidator;

    @Before
    public void setUp() {
        defaultExamStatusChangeValidator = new StatusTransitionChangeValidator();
    }

    @Test
    public void shouldReturnTrueForAValidStatusTransition() {
        final Exam exam = new ExamBuilder()
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_PENDING), Instant.now())
            .build();
        final ExamStatusCode newStatus = new ExamStatusCode(ExamStatusCode.STATUS_APPROVED);

        final Optional<ValidationError> maybeValidationError = defaultExamStatusChangeValidator.validate(exam, newStatus);

        assertThat(maybeValidationError).isNotPresent();
    }

    @Test
    public void shouldReturnFalesForAnInvalidStatusTransition() {
        final Exam exam = new ExamBuilder()
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_DENIED), Instant.now())
            .build();
        final ExamStatusCode newStatus = new ExamStatusCode(ExamStatusCode.STATUS_STARTED);

        final Optional<ValidationError> maybeValidationError = defaultExamStatusChangeValidator.validate(exam, newStatus);

        assertThat(maybeValidationError).isPresent();
        ValidationError error = maybeValidationError.get();
        assertThat(error.getCode()).isEqualTo(ValidationErrorCode.EXAM_STATUS_TRANSITION_FAILURE);
        assertThat(error.getMessage()).isEqualTo(String.format("Transitioning exam status from %s to %s is not allowed",
            exam.getStatus().getCode(),
            newStatus.getCode()));
    }
}
