package tds.score.services.impl;

import TDS.Shared.Exceptions.ReturnStatusException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import tds.itemrenderer.ITSDocumentFactory;
import tds.itemrenderer.data.AccLookup;
import tds.itemrenderer.data.IITSDocument;
import tds.itemrenderer.data.ITSContent;
import tds.itemrenderer.data.ITSMachineRubric;
import tds.itemrenderer.processing.ItemDataReader;
import tds.itemscoringengine.RubricContentSource;
import tds.score.model.AccLookupWrapper;
import tds.score.model.Item;
import tds.score.services.ContentService;
import tds.score.services.ItemService;
import tds.student.services.data.ItemResponse;
import tds.student.services.data.PageGroup;

@Service
public class ContentServiceImpl implements ContentService {
    private final ItemService itemService;
    private final ItemDataReader itemDataReader;

    @Autowired
    public ContentServiceImpl(final ItemService itemService, final ItemDataReader itemDataReader) {
        this.itemService = itemService;
        this.itemDataReader = itemDataReader;
    }

    private static final Logger _logger = LoggerFactory.getLogger(ContentService.class);

    @Override
    public IITSDocument getContent(String xmlFilePath, AccLookup accommodations) throws ReturnStatusException {
        return getContent(xmlFilePath, new AccLookupWrapper(accommodations));
    }

    @Override
    public IITSDocument getItemContent(long bankKey, long itemKey, AccLookup accommodations) throws ReturnStatusException {
        try {
            Optional<Item> maybeItem = itemService.findItemByKey(bankKey, itemKey);
            if (!maybeItem.isPresent())
                return null;

            return getContent(maybeItem.get().getItemPath(), accommodations);
        } catch (ReturnStatusException e) {
            _logger.error(e.getMessage());
            throw e;
        }
    }

    @Override
    public IITSDocument getStimulusContent(long bankKey, long stimulusKey, AccLookup accommodations) throws ReturnStatusException {
        Optional<Item> maybeItem = itemService.findItemByStimulusKey(bankKey, stimulusKey);
        if (!maybeItem.isPresent())
            return null;

        return getContent(maybeItem.get().getStimulusPath(), accommodations);
    }

    @Override
    public void loadPageGroupDocuments(PageGroup pageGroup, AccLookup accLookup) throws ReturnStatusException {
        try {
            pageGroup.setDocument(getContent(pageGroup.getFilePath(), accLookup));
            for (ItemResponse itemResponse : pageGroup) {
                itemResponse.setDocument(getContent(itemResponse.getFilePath(), accLookup));
            }
        } catch (ReturnStatusException e) {
            _logger.error(e.getMessage());
            throw e;
        }
    }

    @Override
    public ITSMachineRubric parseMachineRubric(IITSDocument itsDocument, String language, RubricContentSource rubricContentSource) throws ReturnStatusException {
        ITSMachineRubric machineRubric = null;
        // if the source is item bank then parse the answer key attribute
        // NOTE: we use to get this from the response table
        if (rubricContentSource == RubricContentSource.AnswerKey) {
            machineRubric = new ITSMachineRubric(ITSMachineRubric.ITSMachineRubricType.Text, itsDocument.getAnswerKey() + "|" + itsDocument.getMaxScore());
        }
        // if the source is item xml then get the machine rubric element
        else if (rubricContentSource == RubricContentSource.ItemXML) {
            // get top level machine rubric
            machineRubric = itsDocument.getMachineRubric();
            // if empty try and get content elements machine rubric
            if (machineRubric == null) {
                // get its content for the current tests language
                ITSContent itsContent = itsDocument.getContent(language);
                // make sure this item has a machine rubric
                if (itsContent != null) {
                    machineRubric = itsContent.getMachineRubric();
                }
            }
        }
        return machineRubric;
    }

    private IITSDocument getContent(String xmlFilePath, AccLookupWrapper accommodations) throws ReturnStatusException {
        if (StringUtils.isEmpty(xmlFilePath)) {
            return null;
        }

        try {
            URI uri = new URI(xmlFilePath);
            return ITSDocumentFactory.load(uri, accommodations.getValue(), itemDataReader, true);
        } catch (URISyntaxException e) {
            throw new ReturnStatusException(e);
        }
    }
}
