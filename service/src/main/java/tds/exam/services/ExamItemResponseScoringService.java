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

package tds.exam.services;

import tds.exam.ExamItemResponse;
import tds.exam.ExamItemResponseScore;

/**
 * Interface for scoring {@link tds.exam.ExamItemResponse}s
 */
public interface ExamItemResponseScoringService {
    /**
     * Apply scoring logic to an {@link tds.exam.ExamItemResponse}.
     *
     * @param response The {@link tds.exam.ExamItemResponse} that needs to be scored
     * @return An {@link tds.exam.ExamItemResponseScore} describing the score and scoring metadata for this
     * {@link tds.exam.ExamItemResponse}
     */
    ExamItemResponseScore getScore(final ExamItemResponse response);
}
