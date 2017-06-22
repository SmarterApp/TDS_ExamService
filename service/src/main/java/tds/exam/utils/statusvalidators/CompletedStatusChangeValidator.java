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

package tds.exam.utils.statusvalidators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

import tds.common.ValidationError;
import tds.exam.Exam;
import tds.exam.ExamStatusCode;
import tds.exam.error.ValidationErrorCode;
import tds.exam.services.ExamItemService;
import tds.exam.services.ExamSegmentService;
import tds.exam.utils.ExamStatusChangeValidator;

/**
 * A {@link tds.exam.utils.ExamStatusChangeValidator} for verifying that an {@link tds.exam.Exam} can transition to the
 * "review" or "completed" status.
 */
@Component
public class CompletedStatusChangeValidator implements ExamStatusChangeValidator {
    private final ExamSegmentService examSegmentService;

    @Autowired
    public CompletedStatusChangeValidator(final ExamSegmentService examSegmentService) {
        this.examSegmentService = examSegmentService;
    }

    @Override
    public Optional<ValidationError> validate(final Exam exam, final ExamStatusCode intendedStatus) {
        if (!intendedStatus.getCode().equals(ExamStatusCode.STATUS_REVIEW)
            && !intendedStatus.getCode().equals(ExamStatusCode.STATUS_COMPLETED)) {
            return Optional.empty();
        }

        if (!examSegmentService.checkIfSegmentsCompleted(exam.getId())) {
            return Optional.of(new ValidationError(ValidationErrorCode.EXAM_INCOMPLETE,
                "Cannot move exam to 'review' status because some segments are incomplete"));
        }

        return Optional.empty();
    }
}
