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

import tds.student.sql.data.ItemScoringConfig;

/**
 * Service to fetch the scoring related configuration
 */
public interface ScoreConfigService {
    /**
     * Finds the item score configurations
     *
     * @param clientName client name associated with the configuration
     * @return list of {@link tds.student.sql.data.ItemScoringConfig}
     * @throws ReturnStatusException if there is an error finding the configurations
     */
    List<ItemScoringConfig> findItemScoreConfigs(final String clientName) throws ReturnStatusException;
}
