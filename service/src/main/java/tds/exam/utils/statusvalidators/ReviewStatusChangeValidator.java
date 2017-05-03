package tds.exam.utils.statusvalidators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import tds.exam.Exam;
import tds.exam.ExamStatusCode;
import tds.exam.services.ExamSegmentService;
import tds.exam.utils.ExamStatusChangeValidator;
import tds.exam.utils.StatusTransitionValidator;

/**
 * A {@link tds.exam.utils.ExamStatusChangeValidator} for verifying that an {@link tds.exam.Exam} can transition to the
 * "review" status.
 */
@Component
public class ReviewStatusChangeValidator implements ExamStatusChangeValidator {
    private final ExamSegmentService examSegmentService;

    @Autowired
    public ReviewStatusChangeValidator(final ExamSegmentService examSegmentService) {
        this.examSegmentService = examSegmentService;
    }

    @Override
    public boolean validate(final Exam exam, final ExamStatusCode intendedStatus) {
        if (!intendedStatus.getCode().equals(ExamStatusCode.STATUS_REVIEW)) {
            return true;
        }

        return examSegmentService.checkIfSegmentsCompleted(exam.getId())
            && StatusTransitionValidator.isValidTransition(exam.getStatus().getCode(), intendedStatus.getCode());
    }
}
