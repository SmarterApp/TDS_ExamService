package tds.exam.utils.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import tds.common.entity.utils.ChangeListener;
import tds.common.util.Preconditions;
import tds.common.web.exceptions.NotFoundException;
import tds.exam.Exam;
import tds.exam.ExamSegment;
import tds.exam.ExamStatusCode;
import tds.exam.ExamineeContext;
import tds.exam.models.FieldTestItemGroup;
import tds.exam.repositories.ExamSegmentCommandRepository;
import tds.exam.repositories.ExamSegmentQueryRepository;
import tds.exam.repositories.FieldTestItemGroupCommandRepository;
import tds.exam.repositories.FieldTestItemGroupQueryRepository;
import tds.exam.services.ExamineeService;

/**
 * Listener to apply business rules when an {@link tds.exam.Exam}'s status is set to "completed"
 */
@Component
public class OnCompletedStatusExamChangeListener implements ChangeListener<Exam> {
    private final ExamSegmentQueryRepository examSegmentQueryRepository;
    private final ExamSegmentCommandRepository examSegmentCommandRepository;
    private final FieldTestItemGroupCommandRepository fieldTestItemGroupCommandRepository;
    private final FieldTestItemGroupQueryRepository fieldTestItemGroupQueryRepository;
    private final ExamineeService examineeService;

    @Autowired
    public OnCompletedStatusExamChangeListener(final ExamSegmentCommandRepository examSegmentCommandRepository,
                                               final ExamSegmentQueryRepository examSegmentQueryRepository,
                                               final FieldTestItemGroupCommandRepository fieldTestItemGroupCommandRepository,
                                               final FieldTestItemGroupQueryRepository fieldTestItemGroupQueryRepository,
                                               final ExamineeService examineeService) {
        this.examSegmentCommandRepository = examSegmentCommandRepository;
        this.examSegmentQueryRepository = examSegmentQueryRepository;
        this.fieldTestItemGroupCommandRepository = fieldTestItemGroupCommandRepository;
        this.fieldTestItemGroupQueryRepository = fieldTestItemGroupQueryRepository;
        this.examineeService = examineeService;
    }

    @Override
    @Transactional
    public void accept(final Exam oldExam, final Exam newExam) {
        Preconditions.checkNotNull(oldExam, "oldExam cannot be null");
        Preconditions.checkNotNull(newExam, "newExam cannot be null");

        // If the status has not changed between exam instances or the status has not already been set to "completed" on
        // the new version of the exam, exit
        if (oldExam.getStatus().equals(newExam.getStatus())
            || !newExam.getStatus().getCode().equals(ExamStatusCode.STATUS_COMPLETED)) {
            return;
        }

        // CommonDLL#_OnStatus_Completed_SP, line 1425: Update the exam to indicate this segment is not permeable.
        // Legacy code sets isPermeable to -1
        ExamSegment segment = examSegmentQueryRepository.findByExamIdAndSegmentPosition(newExam.getId(),
            newExam.getCurrentSegmentPosition())
            .orElseThrow(() -> new NotFoundException(String.format("Could not find an exam segment for exam id %s and segment position %d", newExam.getId(), newExam.getCurrentSegmentPosition())));

        examSegmentCommandRepository.update(new ExamSegment.Builder()
            .fromSegment(segment)
            .withPermeable(false)
            .build());

        // CommonDLL#_OnStatus_Completed_SP, line 1430: insert the final version of the student's attributes and
        // relationships
        examineeService.insertAttributesAndRelationships(newExam, ExamineeContext.FINAL);

        // CommonDLL#_OnStatus_Completed_SP, line 1431: not importing CommonDLL#_RecordBPSatisfaction_SP.  The
        // tables/queries that are referenced in that method are only used by the loader stored procedures, schema
        // creation/modification scripts and/or SimDLL.java (which is related to the simulator).

        // CommonDLL#_OnStatus_Completed_SP, lines 1445 - 1453: Find all the field test items that were administered
        // during an exam and record their usage.
        List<FieldTestItemGroup> fieldTestItemGroupsToUpdate =
            fieldTestItemGroupQueryRepository.findUsageInExam(newExam.getId());

        if (fieldTestItemGroupsToUpdate.size() > 0) {
            fieldTestItemGroupCommandRepository.update(fieldTestItemGroupsToUpdate.toArray(new FieldTestItemGroup[fieldTestItemGroupsToUpdate.size()]));
        }

        // TODO:  Submit to TIS for scoring (CommonDLL#_OnStatus_Completed_SP, line 1433 - 1434), which changes the exam's status to "submitted"
    }
}
