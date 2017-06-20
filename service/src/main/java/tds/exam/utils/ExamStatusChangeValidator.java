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

package tds.exam.utils;

import java.util.Optional;

import tds.common.ValidationError;
import tds.exam.Exam;
import tds.exam.ExamStatusCode;

/**
 * An interface to allow for conducting status transition logic.
 */
public interface ExamStatusChangeValidator {
    /**
     * Validate that an {@link tds.exam.Exam} can transition from one state to another.
     *
     * @param exam           The {@link tds.exam.Exam} containing the current {@link tds.exam.ExamStatusCode}
     * @param intendedStatus The {@link tds.exam.ExamStatusCode} to which the {@link tds.exam.Exam} wants to change
     * @return True if the status transition is valid; otherwise false
     */
    Optional<ValidationError> validate(final Exam exam, final ExamStatusCode intendedStatus);
}
