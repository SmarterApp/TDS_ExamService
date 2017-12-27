package tds.exam.utils.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import tds.common.EntityUpdate;
import tds.common.entity.utils.ChangeListener;
import tds.common.util.Preconditions;
import tds.exam.Exam;
import tds.exam.ExamStatusCode;
import tds.exam.services.MessagingService;

@Component
public class OnExpiredStatusExamChangeListener implements ChangeListener<Exam> {
    private final MessagingService messagingService;

    @Autowired
    public OnExpiredStatusExamChangeListener(final MessagingService messagingService) {
        this.messagingService = messagingService;
    }

    @Override
    public void accept(final EntityUpdate<Exam> entityUpdate) {
        Exam oldExam = entityUpdate.getExistingEntity();
        Exam newExam = entityUpdate.getUpdatedEntity();

        Preconditions.checkNotNull(oldExam, "oldExam cannot be null");
        Preconditions.checkNotNull(newExam, "newExam cannot be null");

        // If the status has not changed between exam instances or the status has not already been set to "completed" on
        // the new version of the exam, exit
        if (oldExam.getStatus().equals(newExam.getStatus())
            || !newExam.getStatus().getCode().equals(ExamStatusCode.STATUS_EXPIRED)) {
            return;
        }


        messagingService.sendExamCompletion(newExam.getId());
    }
}
