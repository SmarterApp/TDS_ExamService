package tds.score.services.impl;

import TDS.Shared.Exceptions.ReturnStatusException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

import tds.itemrenderer.ITSDocumentFactory;
import tds.itemrenderer.data.AccLookup;
import tds.itemrenderer.data.IITSDocument;
import tds.itemrenderer.data.ITSContent;
import tds.itemrenderer.data.ITSMachineRubric;
import tds.itemscoringengine.RubricContentSource;
import tds.score.model.AccLookupWrapper;
import tds.score.model.Item;
import tds.score.services.ContentService;
import tds.student.services.data.ItemResponse;
import tds.student.services.data.PageGroup;

@Service
public class ContentServiceImpl implements ContentService {
    private final ItemService itemService;

    @Autowired
    public ContentServiceImpl(final ItemService itemService) {
        this.itemService = itemService;
    }

    private static final Logger _logger = LoggerFactory.getLogger(ContentService.class);

    public IITSDocument getContent(String xmlFilePath, AccLookup accommodations) throws ReturnStatusException {
        return getContent(xmlFilePath, new AccLookupWrapper(accommodations));
    }

    public IITSDocument getItemContent(long bankKey, long itemKey, AccLookup accommodations) throws ReturnStatusException {
        try {
//            String itemPath = itemBankService.getItemPath (bankKey, itemKey);
            Optional<Item> maybeItem = itemService.findItem(bankKey, itemKey, -1);
            if (!maybeItem.isPresent())
                return null;

            return getContent(maybeItem.get().getItemPath(), accommodations);
        } catch (ReturnStatusException e) {
            _logger.error(e.getMessage());
            throw e;
        }
    }

    public IITSDocument getStimulusContent(long bankKey, long stimulusKey, AccLookup accommodations) throws ReturnStatusException {
//        String stimulusPath = itemBankService.getStimulusPath (bankKey, stimulusKey);
        Optional<Item> maybeItem = itemService.findItem(bankKey, -1, stimulusKey);
        if (!maybeItem.isPresent())
            return null;

        return getContent(maybeItem.get().getStimulusPath(), accommodations);
    }

    // / <summary>
    // / Load all the documents for a page group.
    // / </summary>
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

    // / <summary>
    // / This parses the machine rubric from an ITS document.
    // / </summary>
    // / <returns>Returns either the data or a path for the rubric depending on
    // the source.</returns>
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
        if (StringUtils.isEmpty(xmlFilePath))
            return null;

        return ITSDocumentFactory.load(xmlFilePath, accommodations.getValue(), true);
    }
}
