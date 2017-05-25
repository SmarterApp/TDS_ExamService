package tds.score.services;

import TDS.Shared.Exceptions.ReturnStatusException;

import tds.itemrenderer.data.AccLookup;
import tds.itemrenderer.data.IITSDocument;
import tds.itemrenderer.data.ITSMachineRubric;
import tds.itemscoringengine.RubricContentSource;
import tds.student.services.data.PageGroup;

public interface ContentService {
    /**
     * Retrieves the content
     *
     * @param path           path the the content
     * @param accommodations the accommodations represented as a {@link tds.itemrenderer.data.AccLookup}
     * @return {@link tds.itemrenderer.data.IITSDocument}
     * @throws ReturnStatusException if the document cannot be found or parsed
     */
    IITSDocument getContent(final String path, final AccLookup accommodations);

    /**
     * Gets the item content
     *
     * @param clientName     the client name
     * @param bankKey        the bank key
     * @param itemKey        the item key
     * @param accommodations the accommodations represented as a {@link tds.itemrenderer.data.AccLookup}
     * @return {@link tds.itemrenderer.data.IITSDocument}
     * @throws ReturnStatusException if the documentat cannot be found or parsed
     */
    IITSDocument getItemContent(final String clientName, final long bankKey, final long itemKey, final AccLookup accommodations) throws ReturnStatusException;

    /**
     * Gets the item content
     *
     * @param clientName     the client name
     * @param bankKey        the bank key
     * @param stimulusKey    the stimulus key
     * @param accommodations the accommodations represented as a {@link tds.itemrenderer.data.AccLookup}
     * @return {@link tds.itemrenderer.data.IITSDocument}
     * @throws ReturnStatusException if the documentat cannot be found or parsed
     */
    IITSDocument getStimulusContent(final String clientName, final long bankKey, final long stimulusKey, final AccLookup accommodations) throws ReturnStatusException;

    /**
     * Parses the machine rubric
     *
     * @param itsDocument         {@link tds.itemrenderer.data.IITSDocument}
     * @param language            language to parse
     * @param rubricContentSource the {@link tds.itemscoringengine.RubricContentSource}
     * @return {@link tds.itemrenderer.data.ITSMachineRubric} for the document and language
     * @throws ReturnStatusException if the rubric cannot be found or parsed
     */
    ITSMachineRubric parseMachineRubric(final IITSDocument itsDocument, final String language, final RubricContentSource rubricContentSource);

    /**
     * Loads page group documents
     *
     * @param pageGroup the {@link tds.student.services.data.PageGroup}
     * @param accLookup the accommodations as a {@link tds.itemrenderer.data.AccLookup}
     * @throws ReturnStatusException
     */
    void loadPageGroupDocuments(final PageGroup pageGroup, final AccLookup accLookup);
}
