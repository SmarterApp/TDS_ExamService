package tds.score.services;

import TDS.Shared.Exceptions.ReturnStatusException;

import java.util.List;
import java.util.UUID;

import tds.itemrenderer.data.IITSDocument;
import tds.itemscoringengine.ItemScore;
import tds.student.sql.data.IItemResponseScorable;
import tds.student.sql.data.ItemResponseUpdate;
import tds.student.sql.data.ItemResponseUpdateStatus;
import tds.student.sql.data.OpportunityInstance;

public interface ItemScoringService {
    ItemScore checkScoreability (IItemResponseScorable responseScorable, IITSDocument itsDoc) throws ReturnStatusException;

    boolean updateItemScore (UUID oppKey, IItemResponseScorable response, ItemScore score) throws ReturnStatusException;

    List<ItemResponseUpdateStatus> updateResponses (OpportunityInstance oppInstance, List<ItemResponseUpdate> responsesUpdated, Float pageDuration) throws ReturnStatusException;

    ItemScore scoreItem (UUID oppKey, IItemResponseScorable responseScorable, IITSDocument itsDoc) throws ReturnStatusException;
}
