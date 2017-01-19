package tds.exam.utils;

import org.junit.Test;

import tds.exam.ExamStatusCode;

import static org.assertj.core.api.Assertions.assertThat;

public class StatusTransitionValidatorTest {

    @Test
    public void shouldReturnFalseForInvalidTransitionFromPausedToStarted() {
        // cannot go straight from paused -> started
        boolean isValid = StatusTransitionValidator.isValidTransition(ExamStatusCode.STATUS_PAUSED, ExamStatusCode.STATUS_STARTED);
        assertThat(isValid).isFalse();
    }

    @Test
    public void shouldReturnTrueForValidTransitionsFromPausedToStarted() {
        // go from paused -> pending (approval) -> approved -> started
        boolean isValid = StatusTransitionValidator.isValidTransition(ExamStatusCode.STATUS_PAUSED, ExamStatusCode.STATUS_PENDING);
        assertThat(isValid).isTrue();
        isValid = StatusTransitionValidator.isValidTransition(ExamStatusCode.STATUS_PENDING, ExamStatusCode.STATUS_APPROVED);
        assertThat(isValid).isTrue();
        isValid = StatusTransitionValidator.isValidTransition(ExamStatusCode.STATUS_APPROVED, ExamStatusCode.STATUS_STARTED);
        assertThat(isValid).isTrue();
    }

    @Test
    public void shouldReturnFalseForTryingToStartDeniedExam() {
        boolean isValid = StatusTransitionValidator.isValidTransition(ExamStatusCode.STATUS_PAUSED, ExamStatusCode.STATUS_PENDING);
        assertThat(isValid).isTrue();
        isValid = StatusTransitionValidator.isValidTransition(ExamStatusCode.STATUS_PENDING, ExamStatusCode.STATUS_DENIED);
        assertThat(isValid).isTrue();
        isValid = StatusTransitionValidator.isValidTransition(ExamStatusCode.STATUS_DENIED, ExamStatusCode.STATUS_STARTED);
        assertThat(isValid).isFalse();
    }
}
