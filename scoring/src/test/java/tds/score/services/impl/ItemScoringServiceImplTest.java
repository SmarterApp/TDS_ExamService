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
