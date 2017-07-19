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

package tds.exam.repositories.impl;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

import tds.exam.configuration.ExamServiceProperties;
import tds.score.repositories.ItemDataRepository;

@Repository
@Primary
public class ItemDataRepositoryImpl implements ItemDataRepository {
    private static final String CONTENT_SERVICE_PATH = "loadData";
    private final RestTemplate restTemplate;
    private final String contentUrl;

    ItemDataRepositoryImpl(final RestTemplate restTemplate, final ExamServiceProperties examServiceProperties) {
        this.restTemplate = restTemplate;
        this.contentUrl = examServiceProperties.getContentUrl();
    }

    @Override
    public String findOne(final String itemDataPath) throws IOException {
        final UriComponentsBuilder builder =
            UriComponentsBuilder
                .fromHttpUrl(String.format("%s/%s?itemPath=%s",
                    contentUrl,
                    CONTENT_SERVICE_PATH,
                    itemDataPath));

        return restTemplate.getForObject(builder.build().toUri(), String.class);
    }
}
