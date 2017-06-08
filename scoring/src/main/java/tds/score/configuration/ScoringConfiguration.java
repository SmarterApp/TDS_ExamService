package tds.score.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tds.itemrenderer.configuration.ItemDocumentSettings;
import tds.itemrenderer.data.ITSDocument;
import tds.itemrenderer.processing.ITSDocumentParser;
import tds.itemrenderer.processing.ItemDataService;
import tds.itemrenderer.processing.RendererSpecService;
import tds.itemrenderer.service.ItemDocumentService;
import tds.itemrenderer.service.impl.ITSDocumentService;
import tds.itemscoringengine.IItemScorerManager;
import tds.score.services.ContentService;
import tds.score.services.ItemScoringService;
import tds.score.services.ItemService;
import tds.score.services.ResponseService;
import tds.score.services.RubricService;
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
                                                    final RubricService rubricService) {
        return new ItemScoringServiceImpl(responseService,
            scoreConfigService,
            contentService,
            itemScorer,
            itemScoreSettings,
            rubricService);
    }

    @Bean
    public ContentService getContentService(final ItemService itemService,
                                            final ItemDocumentService itemDocumentService) {
        return new ContentServiceImpl(itemService, itemDocumentService);
    }

    @Bean
    public ITSDocumentParser<ITSDocument> documentParser(final RendererSpecService rendererSpecService) {
        return new ITSDocumentParser<>(rendererSpecService);
    }

    @Bean
    public ItemDocumentService getItemDocumentService(final ItemDataService itemDataService,
                                                      final ItemScoreSettings itemScoreSettings,
                                                      final ITSDocumentParser<ITSDocument> documentParser) {
        ItemDocumentSettings itemDocumentSettings = new ItemDocumentSettings();
        itemDocumentSettings.setEncryptionEnabled(itemScoreSettings.isEncryptionEnabled());
        return new ITSDocumentService(itemDataService, documentParser, itemDocumentSettings);
    }
}
