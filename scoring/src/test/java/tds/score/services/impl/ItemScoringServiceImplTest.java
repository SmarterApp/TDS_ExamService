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

import TDS.Shared.Data.ReturnStatus;
import TDS.Shared.Exceptions.ReturnStatusException;
import org.jaxen.util.SingletonList;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URI;
import java.util.Collections;
import java.util.UUID;

import tds.itemrenderer.data.AccLookup;
import tds.itemrenderer.data.IITSDocument;
import tds.itemrenderer.data.ITSAttribute;
import tds.itemrenderer.data.ITSDocument;
import tds.itemrenderer.data.ITSMachineRubric;
import tds.itemrenderer.data.ITSTypes;
import tds.itemrenderer.processing.ItemDataService;
import tds.itemscoringengine.IItemScorerManager;
import tds.itemscoringengine.ItemScore;
import tds.itemscoringengine.ItemScoreInfo;
import tds.itemscoringengine.ResponseInfo;
import tds.itemscoringengine.RubricContentSource;
import tds.itemscoringengine.ScorerInfo;
import tds.itemscoringengine.ScoringStatus;
import tds.score.configuration.ItemScoreSettings;
import tds.score.services.ContentService;
import tds.score.services.ItemScoringService;
import tds.score.services.ResponseService;
import tds.score.services.ScoreConfigService;
import tds.student.sql.data.ItemScoringConfig;
import tds.trt.model.TDSReport;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ItemScoringServiceImplTest {
    private ItemScoringService itemScoringService;

    @Mock
    private ScoreConfigService mockScoreConfigService;

    @Mock
    private ContentService mockContentService;

    @Mock
    private IItemScorerManager mockItemScorer;

    @Mock
    private ItemScoreSettings mockItemScoreSettings;

    @Mock
    private ResponseService mockResponseService;

    @Mock
    private ItemDataService mockItemDataService;

    private TDSReport mockTestResults;

    private IITSDocument mockItemContent;

    @Before
    public void setup() throws Exception {
        itemScoringService = new ItemScoringServiceImpl(mockResponseService, mockScoreConfigService, mockContentService,
            mockItemScorer, mockItemScoreSettings, mockItemDataService);
        final JAXBContext context = JAXBContext.newInstance(TDSReport.class);
        final Unmarshaller unmarshaller = context.createUnmarshaller();
        mockTestResults = (TDSReport) unmarshaller.unmarshal(new InputStreamReader(
            this.getClass().getResourceAsStream("/sample-trt-file.xml")));
    }

    @Test
    public void shouldScoreItemSuccessfullyMachineRubric() throws ReturnStatusException, IOException {
        // Begin Mocks
        ITSMachineRubric rubric = new ITSMachineRubric();
        rubric.setType(ITSMachineRubric.ITSMachineRubricType.Uri);
        rubric.setData("file:/tds/bank/items/Item-187-3238/Item_3238_v0.qrx");

        mockItemContent = new ITSDocument();
        mockItemContent.setBaseUri("/tds/bank/items/Item-187-3238/item-187-3238.xml");
        mockItemContent.setFormat("gi");
        mockItemContent.setRendererSpec("\n" +
            "\t\t\t\t\t<Question id=\"3238\" ITSVer=\"0\" ScoreEngineVer=\"1\" version=\"2.0\" xmlns=\"\"><Description /><QuestionPart id=\"1\"><Options><ShowButtons>delete,point</ShowButtons><GridColor>LightBlue</GridColor><GridSpacing>25,Y</GridSpacing><UpdateLayout>false</UpdateLayout><CanvasWidth>500</CanvasWidth><CanvasHeight>410</CanvasHeight><PaletteWidth>75</PaletteWidth><CenterImage>false</CenterImage><ProperLineGeometry>false</ProperLineGeometry><ScaleImage>false</ScaleImage></Options><Text /><ObjectMenuIcons /><ImageSpec><FileSpec>null/Pages/API/Resources.axd?path=qAbsmMINqDk7XvC7lcZKl4EiIv4i%2BD5vtVwW90nPUjZZKOAuQAhVHPBrAe6TQqjB&amp;file=item_3238_v0_BG_png16malpha.png</FileSpec><Position>0,0</Position></ImageSpec></QuestionPart><PreSetAnswerPart><AnswerSet><Question id=\"3238\"><QuestionPart id=\"1\"><ObjectSet /><SnapPoint /></QuestionPart></Question></AnswerSet></PreSetAnswerPart></Question>\n" +
            "\t\t\t\t\t\t");
        mockItemContent.setType(ITSTypes.ITSEntityType.Item);
        mockItemContent.setMachineRubric(rubric);
        mockItemContent.setIsLoaded(true);
        mockItemContent.setBankKey(187);
        mockItemContent.setValidated(true);
        mockItemContent.setId(3238);
        mockItemContent.addAttribute(new ITSAttribute("itm_att_Item Format", "gi"));

        ItemScoringConfig scoringConfig = new ItemScoringConfig();
        scoringConfig.setItemType("gi");
        scoringConfig.setContext("*");
        scoringConfig.setEnabled(true);
        ItemScoreInfo mockItemScoreInfo = new ItemScoreInfo();
        mockItemScoreInfo.setStatus(ScoringStatus.Scored);
        mockItemScoreInfo.setPoints(1);

        ItemScore mockItemScore = new ItemScore();
        mockItemScore.setScoreInfo(mockItemScoreInfo);

        when(mockContentService.getContent(isA(String.class), eq(AccLookup.getNone()))).thenReturn(mockItemContent);
        when(mockItemScoreSettings.isEnabled()).thenReturn(true);
        when(mockItemScorer.GetScorerInfo(any()))
            .thenReturn(new ScorerInfo("1.0", false, false, RubricContentSource.ItemXML));
        when(mockContentService.parseMachineRubric(isA(IITSDocument.class), eq("ENU"), isA(RubricContentSource.class)))
            .thenReturn(rubric);
        when(mockScoreConfigService.findItemScoreConfigs("SBAC_PT")).thenReturn(Collections.singletonList(scoringConfig));
        when(mockItemDataService.readData(isA(URI.class))).thenReturn("localhost");
        when(mockItemScorer.ScoreItem(isA(ResponseInfo.class), eq(null))).thenReturn(mockItemScore);
        // End Mocks

        final UUID examId = UUID.randomUUID();

        TDSReport.Opportunity.Item itemBeforeRescore = mockTestResults.getOpportunity().getItem().stream()
            .filter(item -> item.getKey() == 3238)
            .findFirst()
            .get();

        assertThat(itemBeforeRescore.getScore()).isEqualTo("0");

        final ReturnStatus status = itemScoringService.rescoreTestResults(examId, mockTestResults);

        assertThat(status.getStatus()).isEqualTo("SUCCESS");
        assertThat(status.getReason()).isNotNull();
        assertThat(status.getHttpStatusCode()).isEqualTo(200);

        TDSReport.Opportunity.Item itemAfterRescore = mockTestResults.getOpportunity().getItem().stream()
            .filter(item -> item.getKey() == 3238)
            .findFirst()
            .get();

        assertThat(itemAfterRescore.getScore()).isEqualTo("1");

        verify(mockItemScoreSettings, atLeastOnce()).isEnabled();
        verify(mockItemScorer, atLeastOnce()).GetScorerInfo(any());
        verify(mockContentService, atLeastOnce()).parseMachineRubric(isA(IITSDocument.class), eq("ENU"), isA(RubricContentSource.class));
        verify(mockScoreConfigService, atLeastOnce()).findItemScoreConfigs("SBAC_PT");
        verify(mockItemDataService, atLeastOnce()).readData(isA(URI.class));
        verify(mockItemScorer, atLeastOnce()).ScoreItem(isA(ResponseInfo.class), eq(null));
    }

    @Test
    @Ignore
    public void shouldScoreItemSuccessfullyMachineRubricAsynchronous() throws ReturnStatusException, IOException {
        // Begin Mocks
        ITSMachineRubric rubric = new ITSMachineRubric();
        rubric.setType(ITSMachineRubric.ITSMachineRubricType.Uri);
        rubric.setData("file:/tds/bank/items/Item-187-3238/Item_3238_v0.qrx");

        mockItemContent = new ITSDocument();
        mockItemContent.setBaseUri("/tds/bank/items/Item-187-3238/item-187-3238.xml");
        mockItemContent.setFormat("gi");
        mockItemContent.setRendererSpec("\n" +
            "\t\t\t\t\t<Question id=\"3238\" ITSVer=\"0\" ScoreEngineVer=\"1\" version=\"2.0\" xmlns=\"\"><Description /><QuestionPart id=\"1\"><Options><ShowButtons>delete,point</ShowButtons><GridColor>LightBlue</GridColor><GridSpacing>25,Y</GridSpacing><UpdateLayout>false</UpdateLayout><CanvasWidth>500</CanvasWidth><CanvasHeight>410</CanvasHeight><PaletteWidth>75</PaletteWidth><CenterImage>false</CenterImage><ProperLineGeometry>false</ProperLineGeometry><ScaleImage>false</ScaleImage></Options><Text /><ObjectMenuIcons /><ImageSpec><FileSpec>null/Pages/API/Resources.axd?path=qAbsmMINqDk7XvC7lcZKl4EiIv4i%2BD5vtVwW90nPUjZZKOAuQAhVHPBrAe6TQqjB&amp;file=item_3238_v0_BG_png16malpha.png</FileSpec><Position>0,0</Position></ImageSpec></QuestionPart><PreSetAnswerPart><AnswerSet><Question id=\"3238\"><QuestionPart id=\"1\"><ObjectSet /><SnapPoint /></QuestionPart></Question></AnswerSet></PreSetAnswerPart></Question>\n" +
            "\t\t\t\t\t\t");
        mockItemContent.setType(ITSTypes.ITSEntityType.Item);
        mockItemContent.setMachineRubric(rubric);
        mockItemContent.setIsLoaded(true);
        mockItemContent.setBankKey(187);
        mockItemContent.setValidated(true);
        mockItemContent.setId(3238);
        mockItemContent.addAttribute(new ITSAttribute("itm_att_Item Format", "gi"));

        ItemScoringConfig scoringConfig = new ItemScoringConfig();
        scoringConfig.setItemType("gi");
        scoringConfig.setContext("*");
        scoringConfig.setEnabled(true);
        ItemScoreInfo mockItemScoreInfo = new ItemScoreInfo();
        mockItemScoreInfo.setStatus(ScoringStatus.Scored);
        mockItemScoreInfo.setPoints(1);

        ItemScore mockItemScore = new ItemScore();
        mockItemScore.setScoreInfo(mockItemScoreInfo);

        when(mockContentService.getContent(isA(String.class), eq(AccLookup.getNone()))).thenReturn(mockItemContent);
        when(mockItemScoreSettings.isEnabled()).thenReturn(true);
        when(mockItemScoreSettings.getServerUrl()).thenReturn("http://localhost");
        when(mockItemScoreSettings.getCallbackUrl()).thenReturn("http://localhost");
        when(mockItemScorer.GetScorerInfo(any()))
            .thenReturn(new ScorerInfo("1.0", true, false, RubricContentSource.ItemXML));
        when(mockContentService.parseMachineRubric(isA(IITSDocument.class), eq("ENU"), isA(RubricContentSource.class)))
            .thenReturn(rubric);
        when(mockScoreConfigService.findItemScoreConfigs("SBAC_PT")).thenReturn(Collections.singletonList(scoringConfig));
        when(mockItemDataService.readData(isA(URI.class))).thenReturn("localhost");
        when(mockItemScorer.ScoreItem(isA(ResponseInfo.class), eq(null))).thenReturn(mockItemScore);
        // End Mocks

        final UUID examId = UUID.randomUUID();

        TDSReport.Opportunity.Item itemBeforeRescore = mockTestResults.getOpportunity().getItem().stream()
            .filter(item -> item.getKey() == 3238)
            .findFirst()
            .get();

        assertThat(itemBeforeRescore.getScore()).isEqualTo("0");

        final ReturnStatus status = itemScoringService.rescoreTestResults(examId, mockTestResults);

        assertThat(status.getStatus()).isEqualTo("SUCCESS");
        assertThat(status.getReason()).isNotNull();
        assertThat(status.getHttpStatusCode()).isEqualTo(200);

        TDSReport.Opportunity.Item itemAfterRescore = mockTestResults.getOpportunity().getItem().stream()
            .filter(item -> item.getKey() == 3238)
            .findFirst()
            .get();

        assertThat(itemAfterRescore.getScore()).isEqualTo("1");

        verify(mockItemScoreSettings, atLeastOnce()).isEnabled();
        verify(mockItemScorer, atLeastOnce()).GetScorerInfo(any());
        verify(mockContentService, atLeastOnce()).parseMachineRubric(isA(IITSDocument.class), eq("ENU"), isA(RubricContentSource.class));
        verify(mockScoreConfigService, atLeastOnce()).findItemScoreConfigs("SBAC_PT");
        verify(mockItemDataService, atLeastOnce()).readData(isA(URI.class));
        verify(mockItemScorer, atLeastOnce()).ScoreItem(isA(ResponseInfo.class), eq(null));
    }

    @Test
    public void shouldFailForNoMachineRubric() throws ReturnStatusException, IOException {
        // Begin Mocks
        mockItemContent = new ITSDocument();
        mockItemContent.setBaseUri("/tds/bank/items/Item-187-3238/item-187-3238.xml");
        mockItemContent.setFormat("gi");
        mockItemContent.setRendererSpec("\n" +
            "\t\t\t\t\t<Question id=\"3238\" ITSVer=\"0\" ScoreEngineVer=\"1\" version=\"2.0\" xmlns=\"\"><Description /><QuestionPart id=\"1\"><Options><ShowButtons>delete,point</ShowButtons><GridColor>LightBlue</GridColor><GridSpacing>25,Y</GridSpacing><UpdateLayout>false</UpdateLayout><CanvasWidth>500</CanvasWidth><CanvasHeight>410</CanvasHeight><PaletteWidth>75</PaletteWidth><CenterImage>false</CenterImage><ProperLineGeometry>false</ProperLineGeometry><ScaleImage>false</ScaleImage></Options><Text /><ObjectMenuIcons /><ImageSpec><FileSpec>null/Pages/API/Resources.axd?path=qAbsmMINqDk7XvC7lcZKl4EiIv4i%2BD5vtVwW90nPUjZZKOAuQAhVHPBrAe6TQqjB&amp;file=item_3238_v0_BG_png16malpha.png</FileSpec><Position>0,0</Position></ImageSpec></QuestionPart><PreSetAnswerPart><AnswerSet><Question id=\"3238\"><QuestionPart id=\"1\"><ObjectSet /><SnapPoint /></QuestionPart></Question></AnswerSet></PreSetAnswerPart></Question>\n" +
            "\t\t\t\t\t\t");
        mockItemContent.setType(ITSTypes.ITSEntityType.Item);
        mockItemContent.setIsLoaded(true);
        mockItemContent.setBankKey(187);
        mockItemContent.setValidated(true);
        mockItemContent.setId(3238);
        mockItemContent.addAttribute(new ITSAttribute("itm_att_Item Format", "gi"));

        ItemScoringConfig scoringConfig = new ItemScoringConfig();
        scoringConfig.setItemType("gi");
        scoringConfig.setContext("*");
        scoringConfig.setEnabled(true);
        ItemScoreInfo mockItemScoreInfo = new ItemScoreInfo();
        mockItemScoreInfo.setStatus(ScoringStatus.Scored);
        mockItemScoreInfo.setPoints(1);

        ItemScore mockItemScore = new ItemScore();
        mockItemScore.setScoreInfo(mockItemScoreInfo);

        when(mockContentService.getContent(isA(String.class), eq(AccLookup.getNone()))).thenReturn(mockItemContent);
        when(mockItemScoreSettings.isEnabled()).thenReturn(true);
        when(mockItemScorer.GetScorerInfo(any()))
            .thenReturn(new ScorerInfo("1.0", false, false, RubricContentSource.ItemXML));
        when(mockScoreConfigService.findItemScoreConfigs("SBAC_PT")).thenReturn(Collections.singletonList(scoringConfig));
        when(mockItemDataService.readData(isA(URI.class))).thenReturn("localhost");
        when(mockItemScorer.ScoreItem(isA(ResponseInfo.class), eq(null))).thenReturn(mockItemScore);
        // End Mocks

        final UUID examId = UUID.randomUUID();

        final ReturnStatus status = itemScoringService.rescoreTestResults(examId, mockTestResults);

        assertThat(status.getStatus()).isEqualTo("FAILED");
        assertThat(status.getReason()).isNotNull();
        assertThat(status.getHttpStatusCode()).isEqualTo(200);

        verify(mockItemScoreSettings, atLeastOnce()).isEnabled();
        verify(mockItemScorer, atLeastOnce()).GetScorerInfo(any());
    }

    @Test(expected = ReturnStatusException.class)
    public void shouldFailScoringNoItemContentFound() throws ReturnStatusException {
        when(mockItemScoreSettings.isEnabled()).thenReturn(true);
        when(mockContentService.getContent(isA(String.class), eq(AccLookup.getNone()))).thenReturn(null);

        final UUID examId = UUID.randomUUID();

        TDSReport.Opportunity.Item itemBeforeRescore = mockTestResults.getOpportunity().getItem().stream()
            .filter(item -> item.getKey() == 3238)
            .findFirst()
            .get();

        assertThat(itemBeforeRescore.getScore()).isEqualTo("0");

        final ReturnStatus status = itemScoringService.rescoreTestResults(examId, mockTestResults);
    }

    @Test(expected = ReturnStatusException.class)
    public void shouldFailScoringScoringDisabled() throws ReturnStatusException {
        when(mockItemScoreSettings.isEnabled()).thenReturn(false);

        final UUID examId = UUID.randomUUID();

        TDSReport.Opportunity.Item itemBeforeRescore = mockTestResults.getOpportunity().getItem().stream()
            .filter(item -> item.getKey() == 3238)
            .findFirst()
            .get();

        assertThat(itemBeforeRescore.getScore()).isEqualTo("0");

        final ReturnStatus status = itemScoringService.rescoreTestResults(examId, mockTestResults);
    }
}
