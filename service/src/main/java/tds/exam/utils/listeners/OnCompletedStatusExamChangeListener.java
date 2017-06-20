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

import java.util.List;
import java.util.stream.Collectors;

import tds.common.entity.utils.ChangeListener;
import tds.common.util.Preconditions;
import tds.exam.Exam;
import tds.exam.ExamSegment;
import tds.exam.ExamStatusCode;
import tds.exam.ExamineeContext;
import tds.exam.models.FieldTestItemGroup;
import tds.exam.services.ExamSegmentService;
import tds.exam.services.ExamineeService;
import tds.exam.services.FieldTestService;
import tds.exam.services.MessagingService;

/**
 * Listener to apply business rules when an {@link tds.exam.Exam}'s status is set to "completed"
 */
@Component
public class OnCompletedStatusExamChangeListener implements ChangeListener<Exam> {
    private final ExamSegmentService examSegmentService;
    private final FieldTestService fieldTestService;
    private final ExamineeService examineeService;
    private final MessagingService messagingService;

    @Autowired
    public OnCompletedStatusExamChangeListener(final ExamSegmentService examSegmentService,
                                               final FieldTestService fieldTestService,
                                               final ExamineeService examineeService,
                                               final MessagingService messagingService) {
        this.examSegmentService = examSegmentService;
        this.fieldTestService = fieldTestService;
        this.examineeService = examineeService;
        this.messagingService = messagingService;
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

        // CommonDLL#_OnStatus_Completed_SP, line 1425: Update the exam to indicate this segment is not permeable,
        // meaning the segment cannot be accessed/visited again.  Legacy code sets isPermeable to -1 for all segments
        final List<ExamSegment> examSegments = examSegmentService.findExamSegments(newExam.getId());

        if (!examSegments.isEmpty()) {
            final List<ExamSegment> filteredSegments = examSegments.stream()
                .filter(ExamSegment::isPermeable)
                .map(examSegment -> ExamSegment.Builder
                    .fromSegment(examSegment)
                    .withPermeable(false)
                    .build()
                )
                .collect(Collectors.toList());

            examSegmentService.update(filteredSegments.toArray(new ExamSegment[filteredSegments.size()]));
        }

        // CommonDLL#_OnStatus_Completed_SP, line 1430: insert the final version of the student's attributes and
        // relationships
        examineeService.insertAttributesAndRelationships(newExam, ExamineeContext.FINAL);

        // CommonDLL#_OnStatus_Completed_SP, line 1431: not importing CommonDLL#_RecordBPSatisfaction_SP.  The
        // tables/queries that are referenced in that method are only used by the loader stored procedures, schema
        // creation/modification scripts and/or SimDLL.java (which is related to the simulator).

        // CommonDLL#_OnStatus_Completed_SP, lines 1445 - 1453: Find all the field test items that were administered
        // during an exam and record their usage.
        final List<FieldTestItemGroup> fieldTestItemGroupsToUpdate = fieldTestService.findUsageInExam(newExam.getId());
        if (!fieldTestItemGroupsToUpdate.isEmpty()) {
            fieldTestService.update(fieldTestItemGroupsToUpdate.toArray(new FieldTestItemGroup[fieldTestItemGroupsToUpdate.size()]));
        }

        // Publish the submitted exam to the Messaging backend, allowing other services to continue processing it.
        // Submit for scoring (CommonDLL#_OnStatus_Completed_SP, line 1433 - 1434), which changes the exam's status to "submitted"
        // Guests are not sent to TIS
        if(newExam.getStudentId() > 0) {
            messagingService.sendExamCompletion(newExam.getId());
        }
    }
}
