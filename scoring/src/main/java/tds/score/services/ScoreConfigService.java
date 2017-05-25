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
    List<ItemScoringConfig> findItemScoreConfigs(final String clientName);
}
