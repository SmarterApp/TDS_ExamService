package tds.score.repositories;

import TDS.Shared.Exceptions.ReturnStatusException;

import tds.itemrenderer.data.AccLookup;
import tds.itemrenderer.data.IITSDocument;

public interface ContentRepository {
    IITSDocument getContent(final String itemPath, final AccLookup accommodations) throws ReturnStatusException;
}
