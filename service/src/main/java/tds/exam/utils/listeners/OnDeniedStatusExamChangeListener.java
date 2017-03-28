package tds.exam.utils.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import tds.common.entity.utils.ChangeListener;
import tds.common.util.Preconditions;
import tds.exam.Exam;
import tds.exam.ExamStatusCode;
import tds.exam.services.ExamAccommodationService;

/**
 * Listener to apply business rules when an {@link tds.exam.Exam}'s status is set to "denied"
 */
@Component
public class OnDeniedStatusExamChangeListener implements ChangeListener<Exam> {
    private final ExamAccommodationService examAccommodationService;

    @Autowired
    public OnDeniedStatusExamChangeListener(final ExamAccommodationService examAccommodationService) {
        this.examAccommodationService = examAccommodationService;
    }

    @Override
    public void accept(final Exam oldExam, final Exam newExam) {
        Preconditions.checkNotNull(oldExam, "oldExam cannot be null");
        Preconditions.checkNotNull(newExam, "newExam cannot be null");

        // If the status has not changed between exam instances or the status has not already been set to "denied" on
        // the new version of the exam, exit
        if (oldExam.getStatus().equals(newExam.getStatus())
            || !newExam.getStatus().getCode().equals(ExamStatusCode.STATUS_DENIED)) {
            return;
        }

        examAccommodationService.denyAccommodations(newExam.getId(), newExam.getChangedAt());
    }
}
