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

import TDS.Shared.Exceptions.ReturnStatusException;

import java.util.List;
import java.util.UUID;

import tds.itemrenderer.data.ITSDocument;
import tds.itemscoringengine.ItemScore;
import tds.score.model.ExamInstance;
import tds.student.sql.data.IItemResponseScorable;
import tds.student.sql.data.ItemResponseUpdate;
import tds.student.sql.data.ItemResponseUpdateStatus;

public interface ItemScoringService {
    ItemScore checkScoreability (IItemResponseScorable responseScorable, ITSDocument itsDoc) throws ReturnStatusException;

    boolean updateItemScore (UUID oppKey, IItemResponseScorable response, ItemScore score) throws ReturnStatusException;

    /**
     * Updates the responses based on scores
     *
     * @param examInstance basic exam information
     * @param responsesUpdated responses updated
     * @param pageDuration the duration the
     * @return
     * @throws ReturnStatusException
     */
    List<ItemResponseUpdateStatus> updateResponses(ExamInstance examInstance, List<ItemResponseUpdate> responsesUpdated, Float pageDuration) throws ReturnStatusException;

    ItemScore scoreItem (UUID oppKey, IItemResponseScorable responseScorable, ITSDocument itsDoc) throws ReturnStatusException;
}
