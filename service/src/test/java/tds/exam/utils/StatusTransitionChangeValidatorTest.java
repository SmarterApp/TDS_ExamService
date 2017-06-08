package tds.exam.utils;

import org.joda.time.Instant;
import org.junit.Test;

import java.util.Optional;

import tds.common.ValidationError;
import tds.exam.Exam;
import tds.exam.ExamStatusCode;
import tds.exam.builder.ExamBuilder;
import tds.exam.utils.statusvalidators.StatusTransitionChangeValidator;

import static org.assertj.core.api.Assertions.assertThat;

public class StatusTransitionChangeValidatorTest {
    private final StatusTransitionChangeValidator statusTransitionChangeValidator = new StatusTransitionChangeValidator();

    @Test
    public void shouldReturnFalseForInvalidTransitionFromPausedToStarted() {
        // cannot go straight from paused -> started
        final Exam pausedExam = new ExamBuilder()
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_PAUSED), Instant.now())
            .build();
        final ExamStatusCode startedStatus = new ExamStatusCode(ExamStatusCode.STATUS_STARTED);

        Optional<ValidationError> maybeValidationError = statusTransitionChangeValidator.validate(pausedExam, startedStatus);
        assertThat(maybeValidationError).isPresent();
    }

    @Test
    public void shouldReturnTrueForValidTransitionsFromPausedToStarted() {
        // go from paused -> pending (approval) -> approved -> started

        // Start with a PAUSED exam that wants to go to PENDING...
        Exam exam = new ExamBuilder()
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_PAUSED), Instant.now())
            .build();
        ExamStatusCode intendedStatus = new ExamStatusCode(ExamStatusCode.STATUS_PENDING);

        Optional<ValidationError> maybePausedToPendingError = statusTransitionChangeValidator.validate(exam, intendedStatus);
        assertThat(maybePausedToPendingError).isNotPresent();

        // ... the exam is updated to PENDING and wants to be APPROVED...
        exam = new ExamBuilder()
            .withStatus(intendedStatus, Instant.now())
            .build();
        intendedStatus = new ExamStatusCode(ExamStatusCode.STATUS_APPROVED);

        Optional<ValidationError> maybePendingToApprovedError = statusTransitionChangeValidator.validate(exam, intendedStatus);
        assertThat(maybePendingToApprovedError).isNotPresent();

        // ... the exam is updated to APPROVED and wants to be STARTED (which should work)
        exam = new ExamBuilder()
            .withStatus(intendedStatus, Instant.now())
            .build();
        intendedStatus = new ExamStatusCode(ExamStatusCode.STATUS_STARTED);

        Optional<ValidationError> maybeApprovedToStartedError = statusTransitionChangeValidator.validate(exam, intendedStatus);
        assertThat(maybeApprovedToStartedError).isNotPresent();
    }

    @Test
    public void shouldReturnFalseForTryingToStartDeniedExam() {
        // Start with a PAUSED exam that wants to go to PENDING...
        Exam exam = new ExamBuilder()
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_PAUSED), Instant.now())
            .build();
        ExamStatusCode intendedStatus = new ExamStatusCode(ExamStatusCode.STATUS_PENDING);

        Optional<ValidationError> maybePausedToPendingError = statusTransitionChangeValidator.validate(exam, intendedStatus);
        assertThat(maybePausedToPendingError).isNotPresent();

        // ... then the exam is updated to PENDING and wants to be DENIED...
        exam = new ExamBuilder()
            .withStatus(intendedStatus, Instant.now())
            .build();
        intendedStatus = new ExamStatusCode(ExamStatusCode.STATUS_DENIED);

        Optional<ValidationError> maybePendingToDeniedError = statusTransitionChangeValidator.validate(exam, intendedStatus);
        assertThat(maybePendingToDeniedError).isNotPresent();

        // ... the DENIED exam tries to move to STARTED (which should fail)
        exam = new ExamBuilder()
            .withStatus(intendedStatus, Instant.now())
            .build();
        intendedStatus = new ExamStatusCode(ExamStatusCode.STATUS_STARTED);

        Optional<ValidationError> maybeDeniedToStartedError = statusTransitionChangeValidator.validate(exam, intendedStatus);
        assertThat(maybeDeniedToStartedError).isPresent();
    }
}
