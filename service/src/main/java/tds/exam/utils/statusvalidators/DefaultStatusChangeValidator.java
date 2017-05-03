package tds.exam.utils.statusvalidators;

import org.springframework.stereotype.Component;

import java.util.Optional;

import tds.common.ValidationError;
import tds.exam.Exam;
import tds.exam.ExamStatusCode;
import tds.exam.error.ValidationErrorCode;
import tds.exam.utils.ExamStatusChangeValidator;
import tds.exam.utils.StatusTransitionValidator;

@Component
public class DefaultStatusChangeValidator implements ExamStatusChangeValidator {
    @Override
    public Optional<ValidationError> validate(final Exam exam, final ExamStatusCode intendedStatus) {
        if (!StatusTransitionValidator.isValidTransition(exam.getStatus().getCode(), intendedStatus.getCode())) {
            return Optional.of(new ValidationError(ValidationErrorCode.EXAM_STATUS_TRANSITION_FAILURE,
                String.format("Transitioning exam status from %s to %s is not allowed",
                    exam.getStatus().getCode(),
                    intendedStatus.getCode())));
        }

        return Optional.empty();
    }
}
