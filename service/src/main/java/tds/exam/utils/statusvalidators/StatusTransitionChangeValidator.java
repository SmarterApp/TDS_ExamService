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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import tds.common.ValidationError;
import tds.exam.Exam;
import tds.exam.ExamStatusCode;
import tds.exam.error.ValidationErrorCode;
import tds.exam.utils.ExamStatusChangeValidator;

@Component
public class StatusTransitionChangeValidator implements ExamStatusChangeValidator {
    // Build a map of current states -> valid next states
    // This logic is contained in the legacy application in CommonDLL._IsValidStatusTransition_FN()
    private static final Map<String, Set<String>> stateMap = ImmutableMap.<String, Set<String>>builder()
        .put(ExamStatusCode.STATUS_PENDING.toLowerCase(), Sets.newHashSet(
            ExamStatusCode.STATUS_INITIALIZING,
            ExamStatusCode.STATUS_PENDING,
            ExamStatusCode.STATUS_DENIED,
            ExamStatusCode.STATUS_APPROVED,
            ExamStatusCode.STATUS_PAUSED,
            ExamStatusCode.STATUS_EXPIRED,
            ExamStatusCode.STATUS_INVALIDATED,
            ExamStatusCode.STATUS_FORCE_COMPLETED
        ))
        .put(ExamStatusCode.STATUS_SUSPENDED.toLowerCase(), Sets.newHashSet(
            ExamStatusCode.STATUS_SUSPENDED,
            ExamStatusCode.STATUS_DENIED,
            ExamStatusCode.STATUS_APPROVED,
            ExamStatusCode.STATUS_PAUSED,
            ExamStatusCode.STATUS_EXPIRED,
            ExamStatusCode.STATUS_INVALIDATED,
            ExamStatusCode.STATUS_FORCE_COMPLETED
        ))
        .put(ExamStatusCode.STATUS_STARTED.toLowerCase(), Sets.newHashSet(
            ExamStatusCode.STATUS_STARTED,
            ExamStatusCode.STATUS_PAUSED,
            ExamStatusCode.STATUS_REVIEW,
            ExamStatusCode.STATUS_COMPLETED,
            ExamStatusCode.STATUS_EXPIRED,
            ExamStatusCode.STATUS_INVALIDATED,
            ExamStatusCode.STATUS_SEGMENT_ENTRY.toLowerCase(),
            ExamStatusCode.STATUS_SEGMENT_EXIT.toLowerCase(),
            ExamStatusCode.STATUS_FORCE_COMPLETED
        ))
        .put(ExamStatusCode.STATUS_APPROVED.toLowerCase(), Sets.newHashSet(
            ExamStatusCode.STATUS_APPROVED,
            ExamStatusCode.STATUS_PENDING,
            ExamStatusCode.STATUS_STARTED,
            ExamStatusCode.STATUS_PAUSED,
            ExamStatusCode.STATUS_EXPIRED,
            ExamStatusCode.STATUS_INVALIDATED,
            ExamStatusCode.STATUS_FORCE_COMPLETED
        ))
        .put(ExamStatusCode.STATUS_REVIEW.toLowerCase(), Sets.newHashSet(
            ExamStatusCode.STATUS_REVIEW,
            ExamStatusCode.STATUS_COMPLETED,
            ExamStatusCode.STATUS_PAUSED,
            ExamStatusCode.STATUS_EXPIRED,
            ExamStatusCode.STATUS_INVALIDATED,
            ExamStatusCode.STATUS_FORCE_COMPLETED,
            ExamStatusCode.STATUS_SEGMENT_ENTRY.toLowerCase(),
            ExamStatusCode.STATUS_SEGMENT_EXIT.toLowerCase()
        ))
        .put(ExamStatusCode.STATUS_PAUSED.toLowerCase(), Sets.newHashSet(
            ExamStatusCode.STATUS_PAUSED,
            ExamStatusCode.STATUS_PENDING,
            ExamStatusCode.STATUS_SUSPENDED,
            ExamStatusCode.STATUS_EXPIRED,
            ExamStatusCode.STATUS_INVALIDATED,
            ExamStatusCode.STATUS_FORCE_COMPLETED
        ))
        .put(ExamStatusCode.STATUS_DENIED.toLowerCase(), Sets.newHashSet(
            ExamStatusCode.STATUS_DENIED,
            ExamStatusCode.STATUS_PENDING,
            ExamStatusCode.STATUS_SUSPENDED,
            ExamStatusCode.STATUS_PAUSED,
            ExamStatusCode.STATUS_EXPIRED,
            ExamStatusCode.STATUS_INVALIDATED,
            ExamStatusCode.STATUS_FORCE_COMPLETED
        ))
        .put(ExamStatusCode.STATUS_COMPLETED.toLowerCase(), Sets.newHashSet(
            ExamStatusCode.STATUS_COMPLETED,
            ExamStatusCode.STATUS_SCORED,
            ExamStatusCode.STATUS_SUBMITTED,
            ExamStatusCode.STATUS_INVALIDATED
        ))
        .put(ExamStatusCode.STATUS_SCORED.toLowerCase(), Sets.newHashSet(
            ExamStatusCode.STATUS_RESCORED,
            ExamStatusCode.STATUS_SUBMITTED,
            ExamStatusCode.STATUS_INVALIDATED
        ))
        .put(ExamStatusCode.STATUS_SUBMITTED.toLowerCase(), Sets.newHashSet(
            ExamStatusCode.STATUS_RESCORED,
            ExamStatusCode.STATUS_REPORTED,
            ExamStatusCode.STATUS_INVALIDATED
        ))
        .put(ExamStatusCode.STATUS_REPORTED.toLowerCase(), Sets.newHashSet(
            ExamStatusCode.STATUS_RESCORED,
            ExamStatusCode.STATUS_INVALIDATED
        ))
        .put(ExamStatusCode.STATUS_EXPIRED.toLowerCase(), Sets.newHashSet(
            ExamStatusCode.STATUS_RESCORED,
            ExamStatusCode.STATUS_INVALIDATED
        ))
        .put(ExamStatusCode.STATUS_INVALIDATED.toLowerCase(), Sets.newHashSet(
            ExamStatusCode.STATUS_RESCORED,
            ExamStatusCode.STATUS_INVALIDATED
        ))
        .put(ExamStatusCode.STATUS_RESCORED.toLowerCase(), Sets.newHashSet(
            ExamStatusCode.STATUS_SCORED
        ))
        .put(ExamStatusCode.STATUS_SEGMENT_ENTRY.toLowerCase(), Sets.newHashSet(
            ExamStatusCode.STATUS_APPROVED,
            ExamStatusCode.STATUS_DENIED,
            ExamStatusCode.STATUS_EXPIRED,
            ExamStatusCode.STATUS_INVALIDATED,
            ExamStatusCode.STATUS_FORCE_COMPLETED
        ))
        .put(ExamStatusCode.STATUS_SEGMENT_EXIT.toLowerCase(), Sets.newHashSet(
            ExamStatusCode.STATUS_APPROVED,
            ExamStatusCode.STATUS_DENIED,
            ExamStatusCode.STATUS_EXPIRED,
            ExamStatusCode.STATUS_INVALIDATED,
            ExamStatusCode.STATUS_FORCE_COMPLETED
        ))
        .put(ExamStatusCode.STATUS_FORCE_COMPLETED.toLowerCase(), Sets.newHashSet(
            ExamStatusCode.STATUS_COMPLETED,
            ExamStatusCode.STATUS_SCORED
        ))
        .put(ExamStatusCode.STATUS_INITIALIZING.toLowerCase(), Sets.newHashSet(
            ExamStatusCode.STATUS_INITIALIZING,
            ExamStatusCode.STATUS_PENDING,
            ExamStatusCode.STATUS_DENIED,
            ExamStatusCode.STATUS_APPROVED,
            ExamStatusCode.STATUS_PAUSED,
            ExamStatusCode.STATUS_EXPIRED,
            ExamStatusCode.STATUS_INVALIDATED,
            ExamStatusCode.STATUS_FORCE_COMPLETED
        ))
        .build();

    @Override
    public Optional<ValidationError> validate(final Exam exam, final ExamStatusCode intendedStatus) {
        final boolean isValidTransition = stateMap.get(exam.getStatus().getCode().toLowerCase()) != null
            && stateMap.get(exam.getStatus().getCode().toLowerCase()).contains(intendedStatus.getCode().toLowerCase());

        if (!isValidTransition) {
            return Optional.of(new ValidationError(ValidationErrorCode.EXAM_STATUS_TRANSITION_FAILURE,
                String.format("Transitioning exam status from %s to %s is not allowed",
                    exam.getStatus().getCode(),
                    intendedStatus.getCode())));
        }

        return Optional.empty();
    }
}
