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

package tds.score.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import tds.itemrenderer.data.ITSDocument;
import tds.itemrenderer.processing.ITSDocumentParser;
import tds.itemrenderer.processing.ItemDataService;
import tds.itemrenderer.processing.RendererSpecService;
import tds.itemscoringengine.IItemScorerManager;
import tds.score.repositories.ContentRepository;
import tds.score.services.ContentService;
import tds.score.services.ItemScoringService;
import tds.score.services.ItemService;
import tds.score.services.ResponseService;
import tds.score.services.ScoreConfigService;
import tds.score.services.impl.ContentServiceImpl;
import tds.score.services.impl.ItemScoringServiceImpl;

@Configuration
public class ScoringConfiguration {

    @Bean
    public ItemScoringService getItemScoringService(final ItemScoreSettings itemScoreSettings,
                                                    final ResponseService responseService,
                                                    final ScoreConfigService scoreConfigService,
                                                    final ContentService contentService,
                                                    final IItemScorerManager itemScorer,
                                                    final ItemDataService itemDataService) {
        return new ItemScoringServiceImpl(responseService,
            scoreConfigService,
            contentService,
            itemScorer,
            itemScoreSettings,
            itemDataService);
    }

    @Bean
    public ContentService getContentService(final ItemService itemService,
                                            final ContentRepository contentRepository) {
        return new ContentServiceImpl(itemService, contentRepository);
    }

    @Bean
    public ITSDocumentParser<ITSDocument> documentParser(final RendererSpecService rendererSpecService) {
        return new ITSDocumentParser<>(rendererSpecService);
    }
}
