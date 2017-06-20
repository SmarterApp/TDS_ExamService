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

package tds.exam.services.scoring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

import tds.assessment.ItemFileMetadata;
import tds.exam.configuration.ExamServiceProperties;
import tds.score.model.Item;
import tds.score.services.ItemService;

@Service
public class ItemServiceImpl implements ItemService {
    private final RestTemplate restTemplate;
    private final String assessmentUrl;

    @Autowired
    public ItemServiceImpl(final RestTemplate restTemplate, final ExamServiceProperties examServiceProperties) {
        this.restTemplate = restTemplate;
        this.assessmentUrl = examServiceProperties.getAssessmentUrl();
    }

    @Override
    public Optional<Item> findItemByStimulusKey(final String clientName, final long bankKey, final long stimulusKey) {
        UriComponents uriComponents =
            UriComponentsBuilder
                .fromHttpUrl(assessmentUrl)
                .path("assessments/item/metadata")
                .queryParam("clientName", clientName)
                .queryParam("bankKey", bankKey)
                .queryParam("stimulusKey", stimulusKey)
                .build();

        Optional<Item> maybeItem = Optional.empty();
        try {
            final ItemFileMetadata item = restTemplate.getForObject(uriComponents.toUri(), ItemFileMetadata.class);
            maybeItem = Optional.of(new Item(item.getFileName(), null));
        } catch (HttpClientErrorException hce) {
            if (hce.getStatusCode() != HttpStatus.NOT_FOUND) {
                throw hce;
            }
        }


        return maybeItem;
    }

    @Override
    public Optional<Item> findItemByKey(final String clientName, final long bankKey, final long itemKey) {
        UriComponents uriComponents =
            UriComponentsBuilder
                .fromHttpUrl(assessmentUrl)
                .path("assessments/item/metadata")
                .queryParam("clientName", clientName)
                .queryParam("bankKey", bankKey)
                .queryParam("itemKey", itemKey)
                .build();

        Optional<Item> maybeItem = Optional.empty();
        try {
            final ItemFileMetadata item = restTemplate.getForObject(uriComponents.toUri(), ItemFileMetadata.class);
            maybeItem = Optional.of(new Item(null, item.getFileName()));
        } catch (HttpClientErrorException hce) {
            if (hce.getStatusCode() != HttpStatus.NOT_FOUND) {
                throw hce;
            }
        }


        return maybeItem;
    }
}
