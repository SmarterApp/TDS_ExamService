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

import org.joda.time.Instant;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import tds.common.ValidationError;
import tds.exam.Exam;
import tds.exam.ExamStatusCode;
import tds.exam.builder.ExamBuilder;
import tds.exam.error.ValidationErrorCode;
import tds.exam.utils.ExamStatusChangeValidator;

import static org.assertj.core.api.Assertions.assertThat;

public class StatusTransitionChangeValidatorTest {
    private ExamStatusChangeValidator defaultExamStatusChangeValidator;

    @Before
    public void setUp() {
        defaultExamStatusChangeValidator = new StatusTransitionChangeValidator();
    }

    @Test
    public void shouldReturnTrueForAValidStatusTransition() {
        final Exam exam = new ExamBuilder()
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_PENDING), Instant.now())
            .build();
        final ExamStatusCode newStatus = new ExamStatusCode(ExamStatusCode.STATUS_APPROVED);

        final Optional<ValidationError> maybeValidationError = defaultExamStatusChangeValidator.validate(exam, newStatus);

        assertThat(maybeValidationError).isNotPresent();
    }

    @Test
    public void shouldReturnFalesForAnInvalidStatusTransition() {
        final Exam exam = new ExamBuilder()
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_DENIED), Instant.now())
            .build();
        final ExamStatusCode newStatus = new ExamStatusCode(ExamStatusCode.STATUS_STARTED);

        final Optional<ValidationError> maybeValidationError = defaultExamStatusChangeValidator.validate(exam, newStatus);

        assertThat(maybeValidationError).isPresent();
        ValidationError error = maybeValidationError.get();
        assertThat(error.getCode()).isEqualTo(ValidationErrorCode.EXAM_STATUS_TRANSITION_FAILURE);
        assertThat(error.getMessage()).isEqualTo(String.format("Transitioning exam status from %s to %s is not allowed",
            exam.getStatus().getCode(),
            newStatus.getCode()));
    }
}
