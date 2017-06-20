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

package tds.exam.configuration.item.selection;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import tds.itemselection.selectors.MsbItemSelector;
import tds.itemselection.selectors.impl.AdaptiveSelector2013;
import tds.itemselection.services.ItemCandidatesService;
import tds.itemselection.services.SegmentService;

@Configuration
@Import(tds.itemselection.config.ItemSelectionConfiguration.class)
public class ItemSelectionConfiguration {
    @Bean
    @Qualifier("adaptiveSelector")
    public MsbItemSelector getAdaptiveItemSelector(final SegmentService segmentService, final ItemCandidatesService itemCandidatesService){
        return new AdaptiveSelector2013(
            segmentService, itemCandidatesService
        );
    }
}
