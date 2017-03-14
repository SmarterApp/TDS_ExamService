package tds.score.services;

import TDS.Shared.Exceptions.ReturnStatusException;

import java.util.List;
import java.util.UUID;

import tds.itemrenderer.data.IITSDocument;
import tds.itemscoringengine.ItemScore;
import tds.score.model.ExamInstance;
import tds.student.sql.data.IItemResponseScorable;
import tds.student.sql.data.ItemResponseUpdate;
import tds.student.sql.data.ItemResponseUpdateStatus;

public interface ItemScoringService {
    ItemScore checkScoreability (IItemResponseScorable responseScorable, IITSDocument itsDoc) throws ReturnStatusException;

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

    ItemScore scoreItem (UUID oppKey, IItemResponseScorable responseScorable, IITSDocument itsDoc) throws ReturnStatusException;
}
