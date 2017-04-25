package tds.exam.configuration.item.selection;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import tds.itemselection.selectors.ItemSelector;
import tds.itemselection.selectors.impl.AdaptiveSelector2013;
import tds.itemselection.services.ItemCandidatesService;
import tds.itemselection.services.SegmentService;

@Configuration
@Import(tds.itemselection.config.ItemSelectionConfiguration.class)
public class ItemSelectionConfiguration {
    @Bean
    @Qualifier("adaptiveSelector")
    public ItemSelector getAdaptiveItemSelector(final SegmentService segmentService, final ItemCandidatesService itemCandidatesService){
        return new AdaptiveSelector2013(
            segmentService, itemCandidatesService
        );
    }
}
