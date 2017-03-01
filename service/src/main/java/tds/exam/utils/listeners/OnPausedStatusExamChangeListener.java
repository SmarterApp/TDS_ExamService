package tds.exam.utils.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import tds.common.Response;
import tds.common.entity.utils.ChangeListener;
import tds.common.util.Preconditions;
import tds.exam.Exam;
import tds.exam.ExamSegment;
import tds.exam.ExamStatusCode;
import tds.exam.services.ExamSegmentService;

/**
 * Listener to apply business rules when an {@link tds.exam.Exam}'s status is set to "paused"
 */
@Component
public class OnPausedStatusExamChangeListener implements ChangeListener<Exam> {
    private final ExamSegmentService examSegmentService;

    @Autowired
    public OnPausedStatusExamChangeListener(final ExamSegmentService examSegmentService) {
        this.examSegmentService = examSegmentService;
    }

    @Override
    @Transactional
    public void accept(final Exam oldExam, final Exam newExam) {
        Preconditions.checkNotNull(oldExam, "oldExam cannot be null");
        Preconditions.checkNotNull(newExam, "newExam cannot be null");

        // If the status has not changed between exam instances or the status has not already been set to "paused" on
        // the new version of the exam, exit
        if (oldExam.getStatus().equals(newExam.getStatus())
            || !newExam.getStatus().getCode().equals(ExamStatusCode.STATUS_PAUSED)) {
            return;
        }

        // CommonDLL#_OnStatus_Paused_SP, line 1493: Update the exam to indicate this segment is not permeable if the
        // segment was previously permeable and the restore permeable condition is either "segment" or "paused".  Legacy
        // code sets isPermeable to -1, which means false.
        // Omit CommonDLL#_OnStatus_Paused_SP, line 1489 - 1493: If the segment's getRestorePermeableCondition is set to
        // "segment" or "paused", then it is not equal to "completed" thus the update to the segment should happen.
        Response<ExamSegment> segmentResponse = examSegmentService.findByExamIdAndSegmentPosition(newExam.getId(),
            newExam.getCurrentSegmentPosition());

        if (segmentResponse.getData().isPresent()) {
            ExamSegment segment = segmentResponse.getData().get();

            if (segment.isPermeable()
                && (segment.getRestorePermeableCondition().equalsIgnoreCase("segment")
                || segment.getRestorePermeableCondition().equalsIgnoreCase("paused"))) {
                examSegmentService.update(ExamSegment.Builder
                    .fromSegment(segment)
                    .withPermeable(false)
                    .withRestorePermeableCondition(null)
                    .build());
            }
        }

        // Omit CommonDLL#_OnStatus_Paused_SP, lines 1501 - 1508: not imported. audit records do not need to be
        // inserted; the exam_event and exam_segment_event tables provide an audit history of the changes made to the
        // Exam and ExamSegment respectively.
    }
}
