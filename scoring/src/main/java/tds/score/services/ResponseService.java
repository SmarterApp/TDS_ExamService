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

package tds.score.services;

import TDS.Shared.Data.ReturnStatus;
import TDS.Shared.Exceptions.ReturnStatusException;

import java.util.UUID;

import tds.score.model.ExamInstance;
import tds.student.sql.data.IItemResponseScorable;
import tds.student.sql.data.IItemResponseUpdate;

/**
 * Service handling response updates
 */
public interface ResponseService {
    /**
     * Updates responses with the score
     *
     * @param examInstance   {@link tds.score.model.ExamInstance} that contains the information for the exam
     * @param responseUpdate {@link tds.student.sql.data.IItemResponseUpdate} that contains the response information
     * @param score          score for the item
     * @param scoreStatus    the score status
     * @param scoreRationale the score rationale
     * @param scoreLatency   the latency
     * @param pageDuration   the page duration
     * @return a {@link TDS.Shared.Data.ReturnStatus} that contains whether the update was successful
     * @throws ReturnStatusException if there is any type of error or validation condition failure
     */
    ReturnStatus updateScoredResponse(final ExamInstance examInstance, final IItemResponseUpdate responseUpdate, final int score,
                                      final String scoreStatus, final String scoreRationale, final long scoreLatency, final long pageDuration) throws ReturnStatusException;

    /**
     * Updates a specific item with an updated score.
     *
     * @param examId           the exam id
     * @param responseScorable the {@link tds.student.sql.data.IItemResponseScorable} containing some score inforamtion
     * @param score            score for the item
     * @param scoreStatus      the score status for the item
     * @param scoreRationale   the score rationale
     * @param scoreDimensions  the dimensions associated with the score
     * @return {@link TDS.Shared.Data.ReturnStatus} that contains the status of the
     * @throws ReturnStatusException if there is an unexpected error
     */
    ReturnStatus updateItemScore(final UUID examId, final IItemResponseScorable responseScorable, final int score, final String scoreStatus, final String scoreRationale, final String scoreDimensions) throws ReturnStatusException;
}
