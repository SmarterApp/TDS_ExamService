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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import tds.common.ValidationError;
import tds.itemrenderer.data.IITSDocument;
import tds.itemscoringengine.ItemScore;
import tds.score.model.ExamInstance;
import tds.student.sql.data.IItemResponseScorable;
import tds.student.sql.data.ItemResponseUpdate;
import tds.student.sql.data.ItemResponseUpdateStatus;
import tds.trt.model.TDSReport;

public interface ItemScoringService {
    ItemScore checkScoreability(final IItemResponseScorable responseScorable, final IITSDocument itsDoc) throws ReturnStatusException;

    ItemScore checkScoreability(final TDSReport testResults, final IITSDocument itsDoc, final String languageCode) throws ReturnStatusException;

    boolean updateItemScore(final UUID oppKey, final IItemResponseScorable response, final ItemScore score) throws ReturnStatusException;

    /**
     * Updates the responses based on scores
     *
     * @param examInstance     basic exam information
     * @param responsesUpdated responses updated
     * @param pageDuration     the duration the
     * @return
     * @throws ReturnStatusException
     */
    List<ItemResponseUpdateStatus> updateResponses(final ExamInstance examInstance, final List<ItemResponseUpdate> responsesUpdated, final Float pageDuration) throws ReturnStatusException;

    ItemScore scoreItem(final UUID oppKey, final IItemResponseScorable responseScorable, final IITSDocument itsDoc) throws ReturnStatusException;

    ItemScore scoreItem(final TDSReport testResults, final TDSReport.Opportunity.Item item,
                        final IITSDocument itsDoc, final String languageCode) throws ReturnStatusException;

    /**
     * Rescores test results and forwards the TRT to ERT for processing
     *
     * @param examId      The id of the exam being rescored
     * @param testResults The test results to rescore
     * @return A status indicating whether or not the rescore was successful
     * @throws ReturnStatusException
     */
    Optional<ValidationError> rescoreTestResults(final UUID examId, final TDSReport testResults) throws ReturnStatusException;
}
