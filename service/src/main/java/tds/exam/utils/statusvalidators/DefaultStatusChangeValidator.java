package tds.exam.utils.statusvalidators;

import org.springframework.stereotype.Component;

import tds.exam.Exam;
import tds.exam.ExamStatusCode;
import tds.exam.utils.ExamStatusChangeValidator;
import tds.exam.utils.StatusTransitionValidator;

@Component
public class DefaultStatusChangeValidator implements ExamStatusChangeValidator {
    @Override
    public boolean validate(final Exam exam, final ExamStatusCode intendedStatus) {
        return StatusTransitionValidator.isValidTransition(exam.getStatus().getCode(), intendedStatus.getCode());
    }
}
