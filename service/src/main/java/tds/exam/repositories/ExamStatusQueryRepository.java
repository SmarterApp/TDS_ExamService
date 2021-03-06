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

package tds.exam.repositories;

import com.google.common.base.Optional;
import org.joda.time.Instant;

import java.util.UUID;

import tds.exam.ExamStatusCode;

/**
 * Handles finding exam statuses
 */
public interface ExamStatusQueryRepository {
    /**
     * Find the {@link tds.exam.ExamStatusCode} with the code
     * @param code code for lookup
     * @return {@link tds.exam.ExamStatusCode} or empty if not found
     * @throws org.springframework.dao.EmptyResultDataAccessException if the status code cannot be found
     */
    ExamStatusCode findExamStatusCode(final String code);

    /**
     * Finds the {@link org.joda.time.Instant} the specified exam was the in the given examStatus
     *
     * @param examId     The id of the exam to check the status for
     * @param examStatus The status to check for
     * @return The {@link org.joda.time.Instant} the exam was last set to the specified status
     */
    Optional<Instant> findRecentTimeAtStatus(final UUID examId, final String examStatus);
}
