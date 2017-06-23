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

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import tds.itemscoringengine.IItemScorerManager;
import tds.score.configuration.ItemScoreSettings;
import tds.score.services.ResponseService;
import tds.score.services.ScoreConfigService;
import tds.student.services.abstractions.IContentService;

@RunWith(MockitoJUnitRunner.class)
@Ignore
public class ItemScoringServiceImplTest {
    @Mock
    private ScoreConfigService mockScoreConfigService;

    @Mock
    private IContentService mockContentService;

    @Mock
    private IItemScorerManager mockItemScorer;

    @Mock
    private ItemScoreSettings mockItemScoreSettings;

    @Mock
    private ResponseService mockResponseService;
}
