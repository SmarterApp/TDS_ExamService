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
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import tds.common.ValidationError;
import tds.exam.Exam;
import tds.exam.ExamStatusCode;
import tds.exam.builder.ExamBuilder;
import tds.exam.services.ExamSegmentService;
import tds.exam.utils.ExamStatusChangeValidator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CompletedStatusChangeValidatorTest {
    private ExamStatusChangeValidator validator;

    @Mock
    private ExamSegmentService mockExamSegmentService;

    @Before
    public void setUp() {
        validator = new CompletedStatusChangeValidator(mockExamSegmentService);
    }

    @Test
    public void shouldValidateTransitioningToReviewStatusFromAValidPreviousStatus() {
        final Exam exam = new ExamBuilder()
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_STARTED), Instant.now())
            .build();
        final ExamStatusCode newStatus = new ExamStatusCode(ExamStatusCode.STATUS_REVIEW);

        when(mockExamSegmentService.checkIfSegmentsCompleted(exam.getId()))
            .thenReturn(true);

        final Optional<ValidationError> maybeValidationError = validator.validate(exam, newStatus);

        assertThat(maybeValidationError).isNotPresent();
    }

    @Test
    public void shouldReturnValidationErrorMessageWhenTheExamIsNotComplete() {
        final Exam exam = new ExamBuilder()
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_STARTED), Instant.now())
            .build();
        final ExamStatusCode newStatus = new ExamStatusCode(ExamStatusCode.STATUS_REVIEW);

        when(mockExamSegmentService.checkIfSegmentsCompleted(exam.getId()))
            .thenReturn(false);

        final Optional<ValidationError> maybeValidationError = validator.validate(exam, newStatus);

        assertThat(maybeValidationError).isPresent();
    }
}
