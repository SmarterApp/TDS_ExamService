/***************************************************************************************************
 * Copyright 2017 Regents of the University of California. Licensed under the Educational
 * Community License, Version 2.0 (the “license”); you may not use this file except in
 * compliance with the License. You may obtain a copy of the license at
 *
 * https://opensource.org/licenses/ECL-2.0
 *
 * Unless required under applicable law or agreed to in writing, software distributed under the
 * License is distributed in an “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for specific language governing permissions
 * and limitations under the license.
 **************************************************************************************************/

package tds.exam.utils.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import tds.common.EntityUpdate;
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
    public void accept(EntityUpdate<Exam> examEntityUpdate) {
        Exam oldExam = examEntityUpdate.getExistingEntity();
        Exam newExam = examEntityUpdate.getUpdatedEntity();

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
        Optional<ExamSegment> maybeExamSegment = examSegmentService.findByExamIdAndSegmentPosition(newExam.getId(),
            newExam.getCurrentSegmentPosition());

        if (maybeExamSegment.isPresent()) {
            ExamSegment segment = maybeExamSegment.get();

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
