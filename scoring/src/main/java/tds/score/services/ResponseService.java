package tds.score.services;

import TDS.Shared.Data.ReturnStatus;
import TDS.Shared.Exceptions.ReturnStatusException;

import java.util.UUID;

import tds.student.sql.data.IItemResponseScorable;
import tds.student.sql.data.IItemResponseUpdate;
import tds.student.sql.data.OpportunityInstance;

/**
 * Service handling response updates
 */
public interface ResponseService {
    /**
     * Updates responses with the score
     *
     * @param oppInstance    {@link tds.student.sql.data.OpportunityInstance} that contains the information for the exam
     * @param responseUpdate {@link tds.student.sql.data.IItemResponseUpdate} that contains the response information
     * @param score          score for the item
     * @param scoreStatus    the score status
     * @param scoreRationale the score rationale
     * @param scoreLatency   the latency
     * @param pageDuration   the page duration
     * @return a {@link TDS.Shared.Data.ReturnStatus} that contains whether the update was successful
     * @throws ReturnStatusException
     */
    ReturnStatus updateScoredResponse(OpportunityInstance oppInstance, IItemResponseUpdate responseUpdate, int score, String scoreStatus, String scoreRationale, long scoreLatency, Float pageDuration) throws ReturnStatusException;

    ReturnStatus updateItemScore (UUID oppKey, IItemResponseScorable responseScorable, int score, String scoreStatus, String scoreRationale, String scoreDimensions) throws ReturnStatusException;
}
