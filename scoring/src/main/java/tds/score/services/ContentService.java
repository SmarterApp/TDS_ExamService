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

package tds.score.services;

import TDS.Shared.Exceptions.ReturnStatusException;

import tds.itemrenderer.data.AccLookup;
import tds.itemrenderer.data.ITSDocument;
import tds.itemrenderer.data.ITSMachineRubric;
import tds.itemscoringengine.RubricContentSource;
import tds.student.services.data.PageGroup;

public interface ContentService {
    /**
     * Retrieves the content
     *
     * @param path           path the the content
     * @param accommodations the accommodations represented as a {@link tds.itemrenderer.data.AccLookup}
     * @return {@link tds.itemrenderer.data.ITSDocument}
     * @throws ReturnStatusException if the document cannot be found or parsed
     */
    ITSDocument getContent(final String path, final AccLookup accommodations) throws ReturnStatusException;

    /**
     * Gets the item content
     *
     * @param clientName     the client name
     * @param bankKey        the bank key
     * @param itemKey        the item key
     * @param accommodations the accommodations represented as a {@link tds.itemrenderer.data.AccLookup}
     * @return {@link tds.itemrenderer.data.ITSDocument}
     * @throws ReturnStatusException if the documentat cannot be found or parsed
     */
    ITSDocument getItemContent(final String clientName, final long bankKey, final long itemKey, final AccLookup accommodations) throws ReturnStatusException;

    /**
     * Gets the item content
     *
     * @param clientName     the client name
     * @param bankKey        the bank key
     * @param stimulusKey    the stimulus key
     * @param accommodations the accommodations represented as a {@link tds.itemrenderer.data.AccLookup}
     * @return {@link tds.itemrenderer.data.ITSDocument}
     * @throws ReturnStatusException if the documentat cannot be found or parsed
     */
    ITSDocument getStimulusContent(final String clientName, final long bankKey, final long stimulusKey, final AccLookup accommodations) throws ReturnStatusException;

    /**
     * Parses the machine rubric
     *
     * @param itsDocument         {@link tds.itemrenderer.data.ITSDocument}
     * @param language            language to parse
     * @param rubricContentSource the {@link tds.itemscoringengine.RubricContentSource}
     * @return {@link tds.itemrenderer.data.ITSMachineRubric} for the document and language
     * @throws ReturnStatusException if the rubric cannot be found or parsed
     */
    ITSMachineRubric parseMachineRubric(final ITSDocument itsDocument, final String language, final RubricContentSource rubricContentSource) throws ReturnStatusException;

    /**
     * Loads page group documents
     *
     * @param pageGroup the {@link tds.student.services.data.PageGroup}
     * @param accLookup the accommodations as a {@link tds.itemrenderer.data.AccLookup}
     * @throws ReturnStatusException
     */
    void loadPageGroupDocuments(final PageGroup pageGroup, final AccLookup accLookup) throws ReturnStatusException;
}
