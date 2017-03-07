package tds.score.services;

import TDS.Shared.Exceptions.ReturnStatusException;

import tds.itemrenderer.data.AccLookup;
import tds.itemrenderer.data.IITSDocument;
import tds.itemrenderer.data.ITSMachineRubric;
import tds.itemscoringengine.RubricContentSource;
import tds.student.services.data.PageGroup;

public interface ContentService {
    // / <summary>
    // / Load ITS document.
    // / </summary>
    // not required
    IITSDocument getContent (String xmlFilePath, AccLookup accommodations) throws ReturnStatusException;

    // / <summary>
    // / Load ITS document.
    // / </summary>
    IITSDocument getItemContent (long bankKey, long itemKey, AccLookup
        accommodations) throws ReturnStatusException;

    // / <summary>
    // / Load ITS content.
    // / </summary>
    IITSDocument getStimulusContent (long bankKey, long stimulusKey, AccLookup
        accommodations) throws ReturnStatusException;

    // / <summary>
    // / Given a ITS document get the machine rubric.
    // / </summary>
    // / <param name="itsDocument"></param>
    // / <param name="language"></param>
    // / <param name="rubricContentSource"></param>
    // / <returns></returns>
    ITSMachineRubric parseMachineRubric (IITSDocument itsDocument, String language, RubricContentSource rubricContentSource) throws ReturnStatusException;

    void loadPageGroupDocuments (PageGroup pageGroup, AccLookup accLookup) throws ReturnStatusException;
}
