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

import tds.itemrenderer.data.AccLookup;
import tds.itemrenderer.data.IITSDocument;
import tds.itemrenderer.data.ITSContent;
import tds.itemrenderer.data.ITSMachineRubric;
import tds.itemrenderer.service.ItemDocumentService;
import tds.itemscoringengine.RubricContentSource;
import tds.score.model.AccLookupWrapper;
import tds.score.model.Item;
import tds.score.services.ContentService;
import tds.score.services.ItemService;
import tds.student.services.data.ItemResponse;
import tds.student.services.data.PageGroup;

@Service
public class ContentServiceImpl implements ContentService {
    private static final Logger _logger = LoggerFactory.getLogger(ContentService.class);

    private final ItemService itemService;
    private final ItemDocumentService itemDocumentService;

    @Autowired
    public ContentServiceImpl(final ItemService itemService, final ItemDocumentService itemDocumentService) {
        this.itemService = itemService;
        this.itemDocumentService = itemDocumentService;
    }

    @Override
    public IITSDocument getContent(final String xmlFilePath, final AccLookup accommodations) throws ReturnStatusException {
        return getContent(xmlFilePath, new AccLookupWrapper(accommodations));
    }

    @Override
    public IITSDocument getItemContent(final String clientName, final long bankKey, final long itemKey, final AccLookup accommodations) throws ReturnStatusException {
        try {
            Optional<Item> maybeItem = itemService.findItemByKey(clientName, bankKey, itemKey);
            if (!maybeItem.isPresent())
                return null;

            return getContent(maybeItem.get().getItemPath(), accommodations);
        } catch (ReturnStatusException e) {
            _logger.error(e.getMessage());
            throw e;
        }
    }

    @Override
    public IITSDocument getStimulusContent(final String clientName, final long bankKey, final long stimulusKey, final AccLookup accommodations) throws ReturnStatusException {
        Optional<Item> maybeItem = itemService.findItemByStimulusKey(clientName, bankKey, stimulusKey);
        if (!maybeItem.isPresent())
            return null;

        return getContent(maybeItem.get().getStimulusPath(), accommodations);
    }

    @Override
    public void loadPageGroupDocuments(final PageGroup pageGroup, final AccLookup accLookup) throws ReturnStatusException {
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
    public ITSMachineRubric parseMachineRubric(final IITSDocument itsDocument, final String language, final RubricContentSource rubricContentSource) throws ReturnStatusException {
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
            IITSDocument document = itemDocumentService.loadItemDocument(uri, accommodations.getValue(), true);
            return document;
        } catch (URISyntaxException e) {
            throw new ReturnStatusException(e);
        }
    }
}
