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

package tds.exam.web.endpoints;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import tds.exam.services.MessagingService;
import tds.score.model.ExamInstance;
import tds.score.services.ItemScoringService;
import tds.student.sql.data.ItemResponseUpdate;
import tds.student.sql.data.ItemResponseUpdateStatus;
import tds.trt.model.TDSReport;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExamScoringControllerTest {

    @Mock
    private ItemScoringService mockItemScoringService;

    @Mock
    private MessagingService mockMessagingService;

    private ExamScoringController controller;

    @Before
    public void setup() throws JAXBException {
        controller = new ExamScoringController(mockItemScoringService, mockMessagingService);
    }

    @Test
    public void itShouldUpdateExamScores() throws Exception {
        final UUID examId = UUID.randomUUID();
        final UUID sessionId = UUID.randomUUID();
        final UUID browserId = UUID.randomUUID();
        final String clientName = "clientName";
        final Float pageDuration = 1.23f;
        final List<ItemResponseUpdate> updates = singletonList(mock(ItemResponseUpdate.class));
        final List<ItemResponseUpdateStatus> responses = singletonList(mock(ItemResponseUpdateStatus.class));

        when(mockItemScoringService.updateResponses(any(ExamInstance.class), anyListOf(ItemResponseUpdate.class), eq(pageDuration)))
            .thenReturn(responses);

        final ResponseEntity<List<ItemResponseUpdateStatus>> response = controller.updateResponses(examId, sessionId, browserId, clientName, pageDuration, updates);
        assertThat(response.getBody()).containsExactlyElementsOf(responses);

        final ArgumentCaptor<ExamInstance> examInstanceArgumentCaptor = ArgumentCaptor.forClass(ExamInstance.class);
        final ArgumentCaptor<List> updatesArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(mockItemScoringService).updateResponses(examInstanceArgumentCaptor.capture(), updatesArgumentCaptor.capture(), eq(pageDuration));

        assertThat(examInstanceArgumentCaptor.getValue().getExamId()).isEqualTo(examId);
        assertThat(examInstanceArgumentCaptor.getValue().getBrowserId()).isEqualTo(browserId);
        assertThat(examInstanceArgumentCaptor.getValue().getSessionId()).isEqualTo(sessionId);
        assertThat(examInstanceArgumentCaptor.getValue().getClientName()).isEqualTo(clientName);

        assertThat(updatesArgumentCaptor.getValue()).containsExactlyElementsOf(updates);
    }

    @Test
    public void itShouldDeserializeTDSReport() throws IOException {
        final String trt = "<TDSReport>\n" +
            "  <Test academicYear=\"0\" assessmentType=\"summative\" assessmentVersion=\"12516\" bankKey=\"187\" contract=\"SBAC_PT\" grade=\"5\" mode=\"online\" name=\"(SBAC_PT)SBAC-MATH-5-Fall-2017-2018\" subject=\"MATH\" testId=\"SBAC-MATH-5\"/>\n" +
            "  <Examinee key=\"29\">\n" +
            "    <ExamineeRelationship context=\"FINAL\" contextDate=\"2017-10-27T23:14:53.748Z\" name=\"ResponsibleDistrictIdentifier\" value=\"DISTRICT2\"/>\n" +
            "    <ExamineeRelationship context=\"FINAL\" contextDate=\"2017-10-27T23:14:53.748Z\" name=\"OrganizationName\" value=\"District 2 - San Diego\"/>\n" +
            "    <ExamineeRelationship context=\"FINAL\" contextDate=\"2017-10-27T23:14:53.748Z\" name=\"ResponsibleInstitutionIdentifier\" value=\"DS9002\"/>\n" +
            "    <ExamineeRelationship context=\"FINAL\" contextDate=\"2017-10-27T23:14:53.748Z\" name=\"NameOfInstitution\" value=\"San Diego Institution\"/>\n" +
            "    <ExamineeRelationship context=\"FINAL\" contextDate=\"2017-10-27T23:14:53.748Z\" name=\"StateAbbreviation\" value=\"CA\"/>\n" +
            "    <ExamineeRelationship context=\"FINAL\" contextDate=\"2017-10-27T23:14:53.748Z\" name=\"StateName\" value=\"CALIFORNIA\"/>\n" +
            "    <ExamineeRelationship context=\"INITIAL\" contextDate=\"2017-10-27T23:12:46.801Z\" name=\"ResponsibleDistrictIdentifier\" value=\"DISTRICT2\"/>\n" +
            "    <ExamineeRelationship context=\"INITIAL\" contextDate=\"2017-10-27T23:12:46.801Z\" name=\"OrganizationName\" value=\"District 2 - San Diego\"/>\n" +
            "    <ExamineeRelationship context=\"INITIAL\" contextDate=\"2017-10-27T23:12:46.801Z\" name=\"ResponsibleInstitutionIdentifier\" value=\"DS9002\"/>\n" +
            "    <ExamineeRelationship context=\"INITIAL\" contextDate=\"2017-10-27T23:12:46.801Z\" name=\"NameOfInstitution\" value=\"San Diego Institution\"/>\n" +
            "    <ExamineeRelationship context=\"INITIAL\" contextDate=\"2017-10-27T23:12:46.801Z\" name=\"StateAbbreviation\" value=\"CA\"/>\n" +
            "    <ExamineeRelationship context=\"INITIAL\" contextDate=\"2017-10-27T23:12:46.801Z\" name=\"StateName\" value=\"CALIFORNIA\"/>\n" +
            "    <ExamineeAttribute context=\"FINAL\" contextDate=\"2017-10-27T23:14:53.735Z\" name=\"AlternateSSID\" value=\"JAMES5\"/>\n" +
            "    <ExamineeAttribute context=\"FINAL\" contextDate=\"2017-10-27T23:14:53.735Z\" name=\"AmericanIndianOrAlaskaNative\" value=\"NO\"/>\n" +
            "    <ExamineeAttribute context=\"FINAL\" contextDate=\"2017-10-27T23:14:53.735Z\" name=\"Asian\" value=\"YES\"/>\n" +
            "    <ExamineeAttribute context=\"FINAL\" contextDate=\"2017-10-27T23:14:53.735Z\" name=\"BlackOrAfricanAmerican\" value=\"NO\"/>\n" +
            "    <ExamineeAttribute context=\"FINAL\" contextDate=\"2017-10-27T23:14:53.735Z\" name=\"DemographicRaceTwoOrMoreRaces\" value=\"NO\"/>\n" +
            "    <ExamineeAttribute context=\"FINAL\" contextDate=\"2017-10-27T23:14:53.735Z\" name=\"Birthdate\" value=\"2000-10-08\"/>\n" +
            "    <ExamineeAttribute context=\"FINAL\" contextDate=\"2017-10-27T23:14:53.735Z\" name=\"EconomicDisadvantageStatus\" value=\"NO\"/>\n" +
            "    <ExamineeAttribute context=\"FINAL\" contextDate=\"2017-10-27T23:14:53.735Z\" name=\"EnglishLanguageProficiencyLevel\" value=\"\"/>\n" +
            "    <ExamineeAttribute context=\"FINAL\" contextDate=\"2017-10-27T23:14:53.735Z\" name=\"FirstEntryDateIntoUSSchool\" value=\"\"/>\n" +
            "    <ExamineeAttribute context=\"FINAL\" contextDate=\"2017-10-27T23:14:53.735Z\" name=\"FirstName\" value=\"JAMES5\"/>\n" +
            "    <ExamineeAttribute context=\"FINAL\" contextDate=\"2017-10-27T23:14:53.735Z\" name=\"Sex\" value=\"M\"/>\n" +
            "    <ExamineeAttribute context=\"FINAL\" contextDate=\"2017-10-27T23:14:53.735Z\" name=\"GradeLevelWhenAssessed\" value=\"FIFTHGRADE\"/>\n" +
            "    <ExamineeAttribute context=\"FINAL\" contextDate=\"2017-10-27T23:14:53.735Z\" name=\"HispanicOrLatinoEthnicity\" value=\"NO\"/>\n" +
            "    <ExamineeAttribute context=\"FINAL\" contextDate=\"2017-10-27T23:14:53.735Z\" name=\"IDEAIndicator\" value=\"YES\"/>\n" +
            "    <ExamineeAttribute context=\"FINAL\" contextDate=\"2017-10-27T23:14:53.735Z\" name=\"LanguageCode\" value=\"aar\"/>\n" +
            "    <ExamineeAttribute context=\"FINAL\" contextDate=\"2017-10-27T23:14:53.735Z\" name=\"LastOrSurname\" value=\"HAPPY\"/>\n" +
            "    <ExamineeAttribute context=\"FINAL\" contextDate=\"2017-10-27T23:14:53.735Z\" name=\"LEPExitDate\" value=\"\"/>\n" +
            "    <ExamineeAttribute context=\"FINAL\" contextDate=\"2017-10-27T23:14:53.735Z\" name=\"LEPStatus\" value=\"YES\"/>\n" +
            "    <ExamineeAttribute context=\"FINAL\" contextDate=\"2017-10-27T23:14:53.735Z\" name=\"LimitedEnglishProficiencyEntryDate\" value=\"\"/>\n" +
            "    <ExamineeAttribute context=\"FINAL\" contextDate=\"2017-10-27T23:14:53.735Z\" name=\"MiddleName\" value=\"\"/>\n" +
            "    <ExamineeAttribute context=\"FINAL\" contextDate=\"2017-10-27T23:14:53.735Z\" name=\"MigrantStatus\" value=\"NO\"/>\n" +
            "    <ExamineeAttribute context=\"FINAL\" contextDate=\"2017-10-27T23:14:53.735Z\" name=\"NativeHawaiianOrOtherPacificIslander\" value=\"NO\"/>\n" +
            "    <ExamineeAttribute context=\"FINAL\" contextDate=\"2017-10-27T23:14:53.735Z\" name=\"PrimaryDisabilityType\" value=\"\"/>\n" +
            "    <ExamineeAttribute context=\"FINAL\" contextDate=\"2017-10-27T23:14:53.735Z\" name=\"Section504Status\" value=\"NO\"/>\n" +
            "    <ExamineeAttribute context=\"FINAL\" contextDate=\"2017-10-27T23:14:53.735Z\" name=\"StudentIdentifier\" value=\"JAMES5\"/>\n" +
            "    <ExamineeAttribute context=\"FINAL\" contextDate=\"2017-10-27T23:14:53.735Z\" name=\"TitleIIILanguageInstructionProgramType\" value=\"\"/>\n" +
            "    <ExamineeAttribute context=\"FINAL\" contextDate=\"2017-10-27T23:14:53.735Z\" name=\"White\" value=\"NO\"/>\n" +
            "    <ExamineeAttribute context=\"INITIAL\" contextDate=\"2017-10-27T23:12:46.786Z\" name=\"AlternateSSID\" value=\"JAMES5\"/>\n" +
            "    <ExamineeAttribute context=\"INITIAL\" contextDate=\"2017-10-27T23:12:46.786Z\" name=\"AmericanIndianOrAlaskaNative\" value=\"NO\"/>\n" +
            "    <ExamineeAttribute context=\"INITIAL\" contextDate=\"2017-10-27T23:12:46.786Z\" name=\"Asian\" value=\"YES\"/>\n" +
            "    <ExamineeAttribute context=\"INITIAL\" contextDate=\"2017-10-27T23:12:46.786Z\" name=\"BlackOrAfricanAmerican\" value=\"NO\"/>\n" +
            "    <ExamineeAttribute context=\"INITIAL\" contextDate=\"2017-10-27T23:12:46.786Z\" name=\"DemographicRaceTwoOrMoreRaces\" value=\"NO\"/>\n" +
            "    <ExamineeAttribute context=\"INITIAL\" contextDate=\"2017-10-27T23:12:46.786Z\" name=\"Birthdate\" value=\"2000-10-08\"/>\n" +
            "    <ExamineeAttribute context=\"INITIAL\" contextDate=\"2017-10-27T23:12:46.786Z\" name=\"EconomicDisadvantageStatus\" value=\"NO\"/>\n" +
            "    <ExamineeAttribute context=\"INITIAL\" contextDate=\"2017-10-27T23:12:46.786Z\" name=\"EnglishLanguageProficiencyLevel\" value=\"\"/>\n" +
            "    <ExamineeAttribute context=\"INITIAL\" contextDate=\"2017-10-27T23:12:46.786Z\" name=\"FirstEntryDateIntoUSSchool\" value=\"\"/>\n" +
            "    <ExamineeAttribute context=\"INITIAL\" contextDate=\"2017-10-27T23:12:46.786Z\" name=\"FirstName\" value=\"JAMES5\"/>\n" +
            "    <ExamineeAttribute context=\"INITIAL\" contextDate=\"2017-10-27T23:12:46.786Z\" name=\"Sex\" value=\"M\"/>\n" +
            "    <ExamineeAttribute context=\"INITIAL\" contextDate=\"2017-10-27T23:12:46.786Z\" name=\"GradeLevelWhenAssessed\" value=\"FIFTHGRADE\"/>\n" +
            "    <ExamineeAttribute context=\"INITIAL\" contextDate=\"2017-10-27T23:12:46.786Z\" name=\"HispanicOrLatinoEthnicity\" value=\"NO\"/>\n" +
            "    <ExamineeAttribute context=\"INITIAL\" contextDate=\"2017-10-27T23:12:46.786Z\" name=\"IDEAIndicator\" value=\"YES\"/>\n" +
            "    <ExamineeAttribute context=\"INITIAL\" contextDate=\"2017-10-27T23:12:46.786Z\" name=\"LanguageCode\" value=\"aar\"/>\n" +
            "    <ExamineeAttribute context=\"INITIAL\" contextDate=\"2017-10-27T23:12:46.786Z\" name=\"LastOrSurname\" value=\"HAPPY\"/>\n" +
            "    <ExamineeAttribute context=\"INITIAL\" contextDate=\"2017-10-27T23:12:46.786Z\" name=\"LEPExitDate\" value=\"\"/>\n" +
            "    <ExamineeAttribute context=\"INITIAL\" contextDate=\"2017-10-27T23:12:46.786Z\" name=\"LEPStatus\" value=\"YES\"/>\n" +
            "    <ExamineeAttribute context=\"INITIAL\" contextDate=\"2017-10-27T23:12:46.786Z\" name=\"LimitedEnglishProficiencyEntryDate\" value=\"\"/>\n" +
            "    <ExamineeAttribute context=\"INITIAL\" contextDate=\"2017-10-27T23:12:46.786Z\" name=\"MiddleName\" value=\"\"/>\n" +
            "    <ExamineeAttribute context=\"INITIAL\" contextDate=\"2017-10-27T23:12:46.786Z\" name=\"MigrantStatus\" value=\"NO\"/>\n" +
            "    <ExamineeAttribute context=\"INITIAL\" contextDate=\"2017-10-27T23:12:46.786Z\" name=\"NativeHawaiianOrOtherPacificIslander\" value=\"NO\"/>\n" +
            "    <ExamineeAttribute context=\"INITIAL\" contextDate=\"2017-10-27T23:12:46.786Z\" name=\"PrimaryDisabilityType\" value=\"\"/>\n" +
            "    <ExamineeAttribute context=\"INITIAL\" contextDate=\"2017-10-27T23:12:46.786Z\" name=\"Section504Status\" value=\"NO\"/>\n" +
            "    <ExamineeAttribute context=\"INITIAL\" contextDate=\"2017-10-27T23:12:46.786Z\" name=\"StudentIdentifier\" value=\"JAMES5\"/>\n" +
            "    <ExamineeAttribute context=\"INITIAL\" contextDate=\"2017-10-27T23:12:46.786Z\" name=\"TitleIIILanguageInstructionProgramType\" value=\"\"/>\n" +
            "    <ExamineeAttribute context=\"INITIAL\" contextDate=\"2017-10-27T23:12:46.786Z\" name=\"White\" value=\"NO\"/>\n" +
            "  </Examinee>\n" +
            "  <Opportunity abnormalStarts=\"0\" administrationCondition=\"\" assessmentParticipantSessionPlatformUserAgent=\"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36\" clientName=\"SBAC_PT\" database=\"exam\" dateCompleted=\"2017-10-27T23:14:53.729Z\" effectiveDate=\"2017-09-15T21:34:58.000Z\" ftCount=\"30\" gracePeriodRestarts=\"0\" itemCount=\"30\" key=\"4a3b714d-fcf9-42ab-a072-9b225494cdba\" oppId=\"0\" opportunity=\"1\" pauseCount=\"0\" sessionId=\"0133ae47-3306-4f73-bf09-f155cd345bcb\" startDate=\"2017-10-27T23:13:19.686Z\" status=\"completed\" statusDate=\"2017-10-27T23:13:19.686Z\" taId=\"nacosta@fairwaytech.com\" taName=\"Yes-Amy A'costa\" windowId=\"ANNUAL\" windowOpportunity=\"1\">\n" +
            "    <Segment algorithm=\"fixedform\" algorithmVersion=\"0\" formId=\"PracTest::MG5::FA17::ENU\" formKey=\"187-946\" id=\"(SBAC_PT)SBAC-MATH-5-Fall-2017-2018\" position=\"1\"/>\n" +
            "    <Accommodation code=\"TDS_TTS0\" segment=\"0\" type=\"TTS\" value=\"None\"/>\n" +
            "    <Accommodation code=\"TDS_ERT_OR&amp;TDS_ERT_Auto\" segment=\"0\" type=\"Emboss Request Type\" value=\"Auto-Request\"/>\n" +
            "    <Accommodation code=\"TDS_TTX_A206\" segment=\"0\" type=\"TTX Business Rules\" value=\"A206\"/>\n" +
            "    <Accommodation code=\"TDS_Mute0\" segment=\"0\" type=\"Mute System Volume\" value=\"Not Muted\"/>\n" +
            "    <Accommodation code=\"ENU\" segment=\"0\" type=\"Language\" value=\"English\"/>\n" +
            "    <Accommodation code=\"TDS_FT_Verdana\" segment=\"0\" type=\"Font Type\" value=\"Verdana\"/>\n" +
            "    <Accommodation code=\"TDS_APC_PSP\" segment=\"0\" type=\"Audio Playback Controls\" value=\"Play Stop and Pause\"/>\n" +
            "    <Accommodation code=\"TDS_WL_Glossary\" segment=\"0\" type=\"Word List\" value=\"English Glossary\"/>\n" +
            "    <Accommodation code=\"TDS_IF_S14\" segment=\"0\" type=\"Item Font Size\" value=\"14pt Items\"/>\n" +
            "    <Accommodation code=\"TDS_Highlight0\" segment=\"0\" type=\"Highlight\" value=\"False\"/>\n" +
            "    <Accommodation code=\"TDS_TS_Modern\" segment=\"0\" type=\"Test Shell\" value=\"Modern skin\"/>\n" +
            "    <Accommodation code=\"TDS_CC0\" segment=\"0\" type=\"Color Choices\" value=\"Black on White\"/>\n" +
            "    <Accommodation code=\"TDS_Emboss_Stim&amp;TDS_Emboss_Item\" segment=\"0\" type=\"Emboss\" value=\"Stimuli&amp;Items\"/>\n" +
            "    <Accommodation code=\"TDS_Masking0\" segment=\"0\" type=\"Masking\" value=\"Masking Not Available\"/>\n" +
            "    <Accommodation code=\"TDS_MfR0\" segment=\"0\" type=\"Mark for Review\" value=\"False\"/>\n" +
            "    <Accommodation code=\"TDS_GN1\" segment=\"0\" type=\"Global Notes\" value=\"On\"/>\n" +
            "    <Accommodation code=\"TDS_ST0\" segment=\"0\" type=\"Strikethrough\" value=\"False\"/>\n" +
            "    <Accommodation code=\"TDS_T0\" segment=\"0\" type=\"Tutorial\" value=\"False\"/>\n" +
            "    <Accommodation code=\"TDS_TTSAA0\" segment=\"0\" type=\"TTS Audio Adjustments\" value=\"TTS Audio Adjustments Off\"/>\n" +
            "    <Accommodation code=\"TDS_ClosedCap1\" segment=\"0\" type=\"Closed Captioning\" value=\"Closed Captioning Available\"/>\n" +
            "    <Accommodation code=\"TDS_PS_L0\" segment=\"0\" type=\"Print Size\" value=\"No default zoom applied\"/>\n" +
            "    <Accommodation code=\"TDS_ExpandablePassages0\" segment=\"0\" type=\"Expandable Passages\" value=\"Expandable Passages Off\"/>\n" +
            "    <Accommodation code=\"TDS_ITM1\" segment=\"0\" type=\"Item Tools Menu\" value=\"On\"/>\n" +
            "    <Accommodation code=\"TDS_F_S14\" segment=\"0\" type=\"Passage Font Size\" value=\"14pt\"/>\n" +
            "    <Accommodation code=\"TDS_SVC1\" segment=\"0\" type=\"System Volume Control\" value=\"Show widget\"/>\n" +
            "    <Accommodation code=\"NEDS0\" segment=\"0\" type=\"Non-Embedded Designated Supports\" value=\"None\"/>\n" +
            "    <Accommodation code=\"TDS_TTSPause0\" segment=\"0\" type=\"TTS Pausing\" value=\"TTS Pausing Off\"/>\n" +
            "    <Accommodation code=\"TDS_SLM1\" segment=\"0\" type=\"Streamlined Mode\" value=\"On\"/>\n" +
            "    <Accommodation code=\"TDS_TPI_ResponsesFix\" segment=\"0\" type=\"Test Progress Indicator\" value=\"Show indicator as a fraction and adjust to test length\"/>\n" +
            "    <Accommodation code=\"TDS_BT_UXN\" segment=\"0\" type=\"Braille Type\" value=\"UEB - Uncontracted - Nemeth Math\"/>\n" +
            "    <Accommodation code=\"TDS_SCNotepad\" segment=\"0\" type=\"Student Comments\" value=\"On\"/>\n" +
            "    <Accommodation code=\"TDS_ASL0\" segment=\"0\" type=\"American Sign Language\" value=\"Do not show ASL videos\"/>\n" +
            "    <Accommodation code=\"TDS_PoD0\" segment=\"0\" type=\"Print on Request\" value=\"None\"/>\n" +
            "    <Accommodation code=\"TDS_APC_SCRUBBER\" segment=\"0\" type=\"Audio Playback Controls\" value=\"Scrubber\"/>\n" +
            "    <Accommodation code=\"NEA0\" segment=\"0\" type=\"Non-Embedded Accommodations\" value=\"None\"/>\n" +
            "    <Item adminDate=\"2017-10-27T23:13:26.930Z\" bankKey=\"187\" clientId=\"\" contentLevel=\"MA-Undesignated\" dropped=\"0\" format=\"GI\" isSelected=\"1\" key=\"3238\" mimeType=\"text/plain\" numberVisits=\"1\" operational=\"0\" pageNumber=\"1\" pageTime=\"10053\" pageVisits=\"0\" position=\"1\" responseDuration=\"10053\" score=\"0\" scoreStatus=\"SCORED\" segmentId=\"(SBAC_PT)SBAC-MATH-5-Fall-2017-2018\" strand=\"MA-Undesignated\">\n" +
            "      <Response date=\"2017-10-27T23:13:38.020Z\">&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt;&lt;AnswerSet&gt;&lt;Question id=&quot;&quot;&gt;&lt;QuestionPart id=&quot;1&quot;&gt;&lt;ObjectSet&gt;&lt;Object&gt;&lt;PointVector&gt;{(175,260)}&lt;/PointVector&gt;&lt;EdgeVector&gt; {} &lt;/EdgeVector&gt;&lt;LabelList&gt; {} &lt;/LabelList&gt;&lt;ValueList&gt; {} &lt;/ValueList&gt;&lt;/Object&gt;&lt;Object&gt;&lt;PointVector&gt;{(275,135)}&lt;/PointVector&gt;&lt;EdgeVector&gt; {} &lt;/EdgeVector&gt;&lt;LabelList&gt; {} &lt;/LabelList&gt;&lt;ValueList&gt; {} &lt;/ValueList&gt;&lt;/Object&gt;&lt;/ObjectSet&gt;&lt;SnapPoint&gt;&lt;/SnapPoint&gt;&lt;/QuestionPart&gt;&lt;/Question&gt;&lt;/AnswerSet&gt;</Response>\n" +
            "      <ScoreInfo maxScore=\"1\" scoreDimension=\"overall\" scorePoint=\"0\" scoreStatus=\"Scored\">\n" +
            "        <ScoreRationale>successfully scored</ScoreRationale>\n" +
            "      </ScoreInfo>\n" +
            "    </Item>\n" +
            "    <Item adminDate=\"2017-10-27T23:13:27.127Z\" bankKey=\"187\" clientId=\"\" contentLevel=\"MA-Undesignated\" dropped=\"0\" format=\"MC\" isSelected=\"1\" key=\"3364\" mimeType=\"text/plain\" numberVisits=\"1\" operational=\"0\" pageNumber=\"2\" pageTime=\"2009\" pageVisits=\"0\" position=\"2\" responseDuration=\"2009\" score=\"0\" scoreStatus=\"SCORED\" segmentId=\"(SBAC_PT)SBAC-MATH-5-Fall-2017-2018\" strand=\"MA-Undesignated\">\n" +
            "      <Response date=\"2017-10-27T23:13:39.017Z\">&lt;![CDATA[ A ]]&gt;</Response>\n" +
            "      <ScoreInfo maxScore=\"1\" scoreDimension=\"overall\" scorePoint=\"0\" scoreStatus=\"Scored\">\n" +
            "        <ScoreRationale>D</ScoreRationale>\n" +
            "      </ScoreInfo>\n" +
            "    </Item>\n" +
            "    <Item adminDate=\"2017-10-27T23:13:27.318Z\" bankKey=\"187\" clientId=\"\" contentLevel=\"MA-Undesignated\" dropped=\"0\" format=\"MC\" isSelected=\"1\" key=\"3375\" mimeType=\"text/plain\" numberVisits=\"1\" operational=\"0\" pageNumber=\"3\" pageTime=\"1210\" pageVisits=\"0\" position=\"3\" responseDuration=\"1210\" score=\"0\" scoreStatus=\"SCORED\" segmentId=\"(SBAC_PT)SBAC-MATH-5-Fall-2017-2018\" strand=\"MA-Undesignated\">\n" +
            "      <Response date=\"2017-10-27T23:13:41.786Z\">&lt;![CDATA[ C ]]&gt;</Response>\n" +
            "      <ScoreInfo maxScore=\"1\" scoreDimension=\"overall\" scorePoint=\"0\" scoreStatus=\"Scored\">\n" +
            "        <ScoreRationale>B</ScoreRationale>\n" +
            "      </ScoreInfo>\n" +
            "    </Item>\n" +
            "    <Item adminDate=\"2017-10-27T23:13:38.335Z\" bankKey=\"187\" clientId=\"\" contentLevel=\"MA-Undesignated\" dropped=\"0\" format=\"MC\" isSelected=\"1\" key=\"3373\" mimeType=\"text/plain\" numberVisits=\"2\" operational=\"0\" pageNumber=\"4\" pageTime=\"7065\" pageVisits=\"0\" position=\"4\" responseDuration=\"7065\" score=\"0\" scoreStatus=\"SCORED\" segmentId=\"(SBAC_PT)SBAC-MATH-5-Fall-2017-2018\" strand=\"MA-Undesignated\">\n" +
            "      <Response date=\"2017-10-27T23:13:47.016Z\">&lt;![CDATA[ A ]]&gt;</Response>\n" +
            "      <ScoreInfo maxScore=\"1\" scoreDimension=\"overall\" scorePoint=\"0\" scoreStatus=\"Scored\">\n" +
            "        <ScoreRationale>C</ScoreRationale>\n" +
            "      </ScoreInfo>\n" +
            "    </Item>\n" +
            "    <Item adminDate=\"2017-10-27T23:13:39.419Z\" bankKey=\"187\" clientId=\"\" contentLevel=\"MA-Undesignated\" dropped=\"0\" format=\"EQ\" isSelected=\"1\" key=\"3266\" mimeType=\"text/plain\" numberVisits=\"1\" operational=\"0\" pageNumber=\"5\" pageTime=\"2209\" pageVisits=\"0\" position=\"5\" responseDuration=\"2209\" score=\"-1\" scoreStatus=\"NOTSCORED\" segmentId=\"(SBAC_PT)SBAC-MATH-5-Fall-2017-2018\" strand=\"MA-Undesignated\">\n" +
            "      <Response date=\"2017-10-27T23:13:49.591Z\">&lt;response type=&quot;plaintext&quot;&gt;F33&lt;/response&gt;</Response>\n" +
            "      <ScoreInfo maxScore=\"1\" scoreDimension=\"overall\" scorePoint=\"-1\" scoreStatus=\"NotScored\">\n" +
            "        <ScoreRationale>No responses found</ScoreRationale>\n" +
            "      </ScoreInfo>\n" +
            "    </Item>\n" +
            "    <Item adminDate=\"2017-10-27T23:13:42.010Z\" bankKey=\"187\" clientId=\"\" contentLevel=\"MA-Undesignated\" dropped=\"0\" format=\"EQ\" isSelected=\"1\" key=\"3240\" mimeType=\"text/plain\" numberVisits=\"1\" operational=\"0\" pageNumber=\"6\" pageTime=\"1413\" pageVisits=\"0\" position=\"6\" responseDuration=\"1413\" score=\"-1\" scoreStatus=\"NOTSCORED\" segmentId=\"(SBAC_PT)SBAC-MATH-5-Fall-2017-2018\" strand=\"MA-Undesignated\">\n" +
            "      <Response date=\"2017-10-27T23:13:51.122Z\">&lt;response type=&quot;plaintext&quot;&gt;3F4&lt;/response&gt;</Response>\n" +
            "      <ScoreInfo maxScore=\"1\" scoreDimension=\"overall\" scorePoint=\"-1\" scoreStatus=\"NotScored\">\n" +
            "        <ScoreRationale>No responses found</ScoreRationale>\n" +
            "      </ScoreInfo>\n" +
            "    </Item>\n" +
            "    <Item adminDate=\"2017-10-27T23:13:46.340Z\" bankKey=\"187\" clientId=\"\" contentLevel=\"MA-Undesignated\" dropped=\"0\" format=\"MC\" isSelected=\"1\" key=\"3377\" mimeType=\"text/plain\" numberVisits=\"2\" operational=\"0\" pageNumber=\"7\" pageTime=\"2021\" pageVisits=\"0\" position=\"7\" responseDuration=\"2021\" score=\"0\" scoreStatus=\"SCORED\" segmentId=\"(SBAC_PT)SBAC-MATH-5-Fall-2017-2018\" strand=\"MA-Undesignated\">\n" +
            "      <Response date=\"2017-10-27T23:13:52.896Z\">&lt;![CDATA[ C ]]&gt;</Response>\n" +
            "      <ScoreInfo maxScore=\"1\" scoreDimension=\"overall\" scorePoint=\"0\" scoreStatus=\"Scored\">\n" +
            "        <ScoreRationale>A</ScoreRationale>\n" +
            "      </ScoreInfo>\n" +
            "    </Item>\n" +
            "    <Item adminDate=\"2017-10-27T23:13:49.816Z\" bankKey=\"187\" clientId=\"\" contentLevel=\"MA-Undesignated\" dropped=\"0\" format=\"MC\" isSelected=\"1\" key=\"3368\" mimeType=\"text/plain\" numberVisits=\"2\" operational=\"0\" pageNumber=\"8\" pageTime=\"1629\" pageVisits=\"0\" position=\"8\" responseDuration=\"1629\" score=\"0\" scoreStatus=\"SCORED\" segmentId=\"(SBAC_PT)SBAC-MATH-5-Fall-2017-2018\" strand=\"MA-Undesignated\">\n" +
            "      <Response date=\"2017-10-27T23:13:53.431Z\">&lt;![CDATA[ A ]]&gt;</Response>\n" +
            "      <ScoreInfo maxScore=\"1\" scoreDimension=\"overall\" scorePoint=\"0\" scoreStatus=\"Scored\">\n" +
            "        <ScoreRationale>B</ScoreRationale>\n" +
            "      </ScoreInfo>\n" +
            "    </Item>\n" +
            "    <Item adminDate=\"2017-10-27T23:13:51.416Z\" bankKey=\"187\" clientId=\"\" contentLevel=\"MA-Undesignated\" dropped=\"0\" format=\"EQ\" isSelected=\"1\" key=\"3433\" mimeType=\"text/plain\" numberVisits=\"1\" operational=\"0\" pageNumber=\"9\" pageTime=\"1248\" pageVisits=\"0\" position=\"9\" responseDuration=\"1248\" score=\"-1\" scoreStatus=\"NOTSCORED\" segmentId=\"(SBAC_PT)SBAC-MATH-5-Fall-2017-2018\" strand=\"MA-Undesignated\">\n" +
            "      <Response date=\"2017-10-27T23:13:54.733Z\">&lt;response type=&quot;plaintext&quot;&gt;F3&lt;/response&gt;</Response>\n" +
            "      <ScoreInfo maxScore=\"1\" scoreDimension=\"overall\" scorePoint=\"-1\" scoreStatus=\"NotScored\">\n" +
            "        <ScoreRationale>No responses found</ScoreRationale>\n" +
            "      </ScoreInfo>\n" +
            "    </Item>\n" +
            "    <Item adminDate=\"2017-10-27T23:13:52.032Z\" bankKey=\"187\" clientId=\"\" contentLevel=\"MA-Undesignated\" dropped=\"0\" format=\"EQ\" isSelected=\"1\" key=\"3214\" mimeType=\"text/plain\" numberVisits=\"1\" operational=\"0\" pageNumber=\"10\" pageTime=\"1481\" pageVisits=\"0\" position=\"10\" responseDuration=\"1481\" score=\"-1\" scoreStatus=\"NOTSCORED\" segmentId=\"(SBAC_PT)SBAC-MATH-5-Fall-2017-2018\" strand=\"MA-Undesignated\">\n" +
            "      <Response date=\"2017-10-27T23:13:56.526Z\">&lt;response type=&quot;plaintext&quot;&gt;F3&lt;/response&gt;</Response>\n" +
            "      <ScoreInfo maxScore=\"1\" scoreDimension=\"overall\" scorePoint=\"-1\" scoreStatus=\"NotScored\">\n" +
            "        <ScoreRationale>No responses found</ScoreRationale>\n" +
            "      </ScoreInfo>\n" +
            "    </Item>\n" +
            "    <Item adminDate=\"2017-10-27T23:13:53.110Z\" bankKey=\"187\" clientId=\"\" contentLevel=\"MA-Undesignated\" dropped=\"0\" format=\"MC\" isSelected=\"1\" key=\"3274\" mimeType=\"text/plain\" numberVisits=\"2\" operational=\"0\" pageNumber=\"11\" pageTime=\"2365\" pageVisits=\"0\" position=\"11\" responseDuration=\"2365\" score=\"0\" scoreStatus=\"SCORED\" segmentId=\"(SBAC_PT)SBAC-MATH-5-Fall-2017-2018\" strand=\"MA-Undesignated\">\n" +
            "      <Response date=\"2017-10-27T23:13:58.618Z\">&lt;![CDATA[ B ]]&gt;</Response>\n" +
            "      <ScoreInfo maxScore=\"1\" scoreDimension=\"overall\" scorePoint=\"0\" scoreStatus=\"Scored\">\n" +
            "        <ScoreRationale>D</ScoreRationale>\n" +
            "      </ScoreInfo>\n" +
            "    </Item>\n" +
            "    <Item adminDate=\"2017-10-27T23:13:55.017Z\" bankKey=\"187\" clientId=\"\" contentLevel=\"MA-Undesignated\" dropped=\"0\" format=\"MC\" isSelected=\"1\" key=\"3302\" mimeType=\"text/plain\" numberVisits=\"2\" operational=\"0\" pageNumber=\"12\" pageTime=\"3208\" pageVisits=\"0\" position=\"12\" responseDuration=\"3208\" score=\"0\" scoreStatus=\"SCORED\" segmentId=\"(SBAC_PT)SBAC-MATH-5-Fall-2017-2018\" strand=\"MA-Undesignated\">\n" +
            "      <Response date=\"2017-10-27T23:14:01.731Z\">&lt;![CDATA[ C ]]&gt;</Response>\n" +
            "      <ScoreInfo maxScore=\"1\" scoreDimension=\"overall\" scorePoint=\"0\" scoreStatus=\"Scored\">\n" +
            "        <ScoreRationale>D</ScoreRationale>\n" +
            "      </ScoreInfo>\n" +
            "    </Item>\n" +
            "    <Item adminDate=\"2017-10-27T23:13:56.923Z\" bankKey=\"187\" clientId=\"\" contentLevel=\"MA-Undesignated\" dropped=\"0\" format=\"EQ\" isSelected=\"1\" key=\"3463\" mimeType=\"text/plain\" numberVisits=\"1\" operational=\"0\" pageNumber=\"13\" pageTime=\"2371\" pageVisits=\"0\" position=\"13\" responseDuration=\"2371\" score=\"-1\" scoreStatus=\"NOTSCORED\" segmentId=\"(SBAC_PT)SBAC-MATH-5-Fall-2017-2018\" strand=\"MA-Undesignated\">\n" +
            "      <Response date=\"2017-10-27T23:14:04.512Z\">&lt;response type=&quot;plaintext&quot;&gt;F3&lt;/response&gt;</Response>\n" +
            "      <ScoreInfo maxScore=\"1\" scoreDimension=\"overall\" scorePoint=\"-1\" scoreStatus=\"NotScored\">\n" +
            "        <ScoreRationale>No responses found</ScoreRationale>\n" +
            "      </ScoreInfo>\n" +
            "    </Item>\n" +
            "    <Item adminDate=\"2017-10-27T23:13:57.825Z\" bankKey=\"187\" clientId=\"\" contentLevel=\"MA-Undesignated\" dropped=\"0\" format=\"EQ\" isSelected=\"1\" key=\"3320\" mimeType=\"text/plain\" numberVisits=\"1\" operational=\"0\" pageNumber=\"14\" pageTime=\"2496\" pageVisits=\"0\" position=\"14\" responseDuration=\"2496\" score=\"-1\" scoreStatus=\"NOTSCORED\" segmentId=\"(SBAC_PT)SBAC-MATH-5-Fall-2017-2018\" strand=\"MA-Undesignated\">\n" +
            "      <Response date=\"2017-10-27T23:14:06.930Z\">&lt;response type=&quot;plaintext&quot;&gt;F3&lt;/response&gt;</Response>\n" +
            "      <ScoreInfo maxScore=\"1\" scoreDimension=\"overall\" scorePoint=\"-1\" scoreStatus=\"NotScored\">\n" +
            "        <ScoreRationale>No responses found</ScoreRationale>\n" +
            "      </ScoreInfo>\n" +
            "    </Item>\n" +
            "    <Item adminDate=\"2017-10-27T23:14:01.226Z\" bankKey=\"187\" clientId=\"\" contentLevel=\"MA-Undesignated\" dropped=\"0\" format=\"EQ\" isSelected=\"1\" key=\"3284\" mimeType=\"text/plain\" numberVisits=\"1\" operational=\"0\" pageNumber=\"15\" pageTime=\"2290\" pageVisits=\"0\" position=\"15\" responseDuration=\"2290\" score=\"-1\" scoreStatus=\"NOTSCORED\" segmentId=\"(SBAC_PT)SBAC-MATH-5-Fall-2017-2018\" strand=\"MA-Undesignated\">\n" +
            "      <Response date=\"2017-10-27T23:14:09.020Z\">&lt;response type=&quot;plaintext&quot;&gt;F3&lt;/response&gt;</Response>\n" +
            "      <ScoreInfo maxScore=\"1\" scoreDimension=\"overall\" scorePoint=\"-1\" scoreStatus=\"NotScored\">\n" +
            "        <ScoreRationale>No responses found</ScoreRationale>\n" +
            "      </ScoreInfo>\n" +
            "    </Item>\n" +
            "    <Item adminDate=\"2017-10-27T23:14:04.732Z\" bankKey=\"187\" clientId=\"\" contentLevel=\"MA-Undesignated\" dropped=\"0\" format=\"MC\" isSelected=\"1\" key=\"3310\" mimeType=\"text/plain\" numberVisits=\"2\" operational=\"0\" pageNumber=\"16\" pageTime=\"1648\" pageVisits=\"0\" position=\"16\" responseDuration=\"1648\" score=\"0\" scoreStatus=\"SCORED\" segmentId=\"(SBAC_PT)SBAC-MATH-5-Fall-2017-2018\" strand=\"MA-Undesignated\">\n" +
            "      <Response date=\"2017-10-27T23:14:10.524Z\">&lt;![CDATA[ C ]]&gt;</Response>\n" +
            "      <ScoreInfo maxScore=\"1\" scoreDimension=\"overall\" scorePoint=\"0\" scoreStatus=\"Scored\">\n" +
            "        <ScoreRationale>A</ScoreRationale>\n" +
            "      </ScoreInfo>\n" +
            "    </Item>\n" +
            "    <Item adminDate=\"2017-10-27T23:14:07.119Z\" bankKey=\"187\" clientId=\"\" contentLevel=\"MA-Undesignated\" dropped=\"0\" format=\"MI\" isSelected=\"1\" key=\"3613\" mimeType=\"text/plain\" numberVisits=\"1\" operational=\"0\" pageNumber=\"17\" pageTime=\"3901\" pageVisits=\"0\" position=\"17\" responseDuration=\"3901\" score=\"0\" scoreStatus=\"SCORED\" segmentId=\"(SBAC_PT)SBAC-MATH-5-Fall-2017-2018\" strand=\"MA-Undesignated\">\n" +
            "      <Response date=\"2017-10-27T23:14:15.018Z\">&lt;itemResponse&gt;&lt;response id=&quot;RESPONSE&quot;&gt;&lt;value&gt;2 a&lt;/value&gt;&lt;value&gt;2 b&lt;/value&gt;&lt;/response&gt;&lt;/itemResponse&gt;</Response>\n" +
            "      <ScoreInfo maxScore=\"1\" scoreDimension=\"overall\" scorePoint=\"0\" scoreStatus=\"Scored\">\n" +
            "        <ScoreRationale>successfully scored</ScoreRationale>\n" +
            "      </ScoreInfo>\n" +
            "    </Item>\n" +
            "    <Item adminDate=\"2017-10-27T23:14:09.216Z\" bankKey=\"187\" clientId=\"\" contentLevel=\"MA-Undesignated\" dropped=\"0\" format=\"MI\" isSelected=\"1\" key=\"3605\" mimeType=\"text/plain\" numberVisits=\"1\" operational=\"0\" pageNumber=\"18\" pageTime=\"4066\" pageVisits=\"0\" position=\"18\" responseDuration=\"4066\" score=\"0\" scoreStatus=\"SCORED\" segmentId=\"(SBAC_PT)SBAC-MATH-5-Fall-2017-2018\" strand=\"MA-Undesignated\">\n" +
            "      <Response date=\"2017-10-27T23:14:18.703Z\">&lt;itemResponse&gt;&lt;response id=&quot;RESPONSE&quot;&gt;&lt;value&gt;1 a&lt;/value&gt;&lt;value&gt;2 b&lt;/value&gt;&lt;/response&gt;&lt;/itemResponse&gt;</Response>\n" +
            "      <ScoreInfo maxScore=\"1\" scoreDimension=\"overall\" scorePoint=\"0\" scoreStatus=\"Scored\">\n" +
            "        <ScoreRationale>successfully scored</ScoreRationale>\n" +
            "      </ScoreInfo>\n" +
            "    </Item>\n" +
            "    <Item adminDate=\"2017-10-27T23:14:09.914Z\" bankKey=\"187\" clientId=\"\" contentLevel=\"MA-Undesignated\" dropped=\"0\" format=\"EQ\" isSelected=\"1\" key=\"3354\" mimeType=\"text/plain\" numberVisits=\"1\" operational=\"0\" pageNumber=\"19\" pageTime=\"2688\" pageVisits=\"0\" position=\"19\" responseDuration=\"2688\" score=\"-1\" scoreStatus=\"NOTSCORED\" segmentId=\"(SBAC_PT)SBAC-MATH-5-Fall-2017-2018\" strand=\"MA-Undesignated\">\n" +
            "      <Response date=\"2017-10-27T23:14:21.590Z\">&lt;response type=&quot;plaintext&quot;&gt;3F&lt;/response&gt;</Response>\n" +
            "      <ScoreInfo maxScore=\"2\" scoreDimension=\"overall\" scorePoint=\"-1\" scoreStatus=\"NotScored\">\n" +
            "        <ScoreRationale>No responses found</ScoreRationale>\n" +
            "      </ScoreInfo>\n" +
            "    </Item>\n" +
            "    <Item adminDate=\"2017-10-27T23:14:15.234Z\" bankKey=\"187\" clientId=\"\" contentLevel=\"MA-Undesignated\" dropped=\"0\" format=\"EQ\" isSelected=\"1\" key=\"3292\" mimeType=\"text/plain\" numberVisits=\"1\" operational=\"0\" pageNumber=\"20\" pageTime=\"1716\" pageVisits=\"0\" position=\"20\" responseDuration=\"1716\" score=\"-1\" scoreStatus=\"NOTSCORED\" segmentId=\"(SBAC_PT)SBAC-MATH-5-Fall-2017-2018\" strand=\"MA-Undesignated\">\n" +
            "      <Response date=\"2017-10-27T23:14:23.045Z\">&lt;response type=&quot;plaintext&quot;&gt;F3&lt;/response&gt;</Response>\n" +
            "      <ScoreInfo maxScore=\"1\" scoreDimension=\"overall\" scorePoint=\"-1\" scoreStatus=\"NotScored\">\n" +
            "        <ScoreRationale>No responses found</ScoreRationale>\n" +
            "      </ScoreInfo>\n" +
            "    </Item>\n" +
            "    <Item adminDate=\"2017-10-27T23:14:18.829Z\" bankKey=\"187\" clientId=\"\" contentLevel=\"MA-Undesignated\" dropped=\"0\" format=\"EQ\" isSelected=\"1\" key=\"3218\" mimeType=\"text/plain\" numberVisits=\"1\" operational=\"0\" pageNumber=\"21\" pageTime=\"2012\" pageVisits=\"0\" position=\"21\" responseDuration=\"2012\" score=\"-1\" scoreStatus=\"NOTSCORED\" segmentId=\"(SBAC_PT)SBAC-MATH-5-Fall-2017-2018\" strand=\"MA-Undesignated\">\n" +
            "      <Response date=\"2017-10-27T23:14:25.056Z\">&lt;response type=&quot;plaintext&quot;&gt;3F&lt;/response&gt;</Response>\n" +
            "      <ScoreInfo maxScore=\"1\" scoreDimension=\"overall\" scorePoint=\"-1\" scoreStatus=\"NotScored\">\n" +
            "        <ScoreRationale>No responses found</ScoreRationale>\n" +
            "      </ScoreInfo>\n" +
            "    </Item>\n" +
            "    <Item adminDate=\"2017-10-27T23:14:21.740Z\" bankKey=\"187\" clientId=\"\" contentLevel=\"MA-Undesignated\" dropped=\"0\" format=\"EQ\" isSelected=\"1\" key=\"3338\" mimeType=\"text/plain\" numberVisits=\"1\" operational=\"0\" pageNumber=\"22\" pageTime=\"1641\" pageVisits=\"0\" position=\"22\" responseDuration=\"1641\" score=\"-1\" scoreStatus=\"NOTSCORED\" segmentId=\"(SBAC_PT)SBAC-MATH-5-Fall-2017-2018\" strand=\"MA-Undesignated\">\n" +
            "      <Response date=\"2017-10-27T23:14:26.756Z\">&lt;response type=&quot;plaintext&quot;&gt;F3&lt;/response&gt;</Response>\n" +
            "      <ScoreInfo maxScore=\"1\" scoreDimension=\"overall\" scorePoint=\"-1\" scoreStatus=\"NotScored\">\n" +
            "        <ScoreRationale>No responses found</ScoreRationale>\n" +
            "      </ScoreInfo>\n" +
            "    </Item>\n" +
            "    <Item adminDate=\"2017-10-27T23:14:23.196Z\" bankKey=\"187\" clientId=\"\" contentLevel=\"MA-Undesignated\" dropped=\"0\" format=\"MC\" isSelected=\"1\" key=\"3545\" mimeType=\"text/plain\" numberVisits=\"2\" operational=\"0\" pageNumber=\"23\" pageTime=\"1385\" pageVisits=\"0\" position=\"23\" responseDuration=\"1385\" score=\"1\" scoreStatus=\"SCORED\" segmentId=\"(SBAC_PT)SBAC-MATH-5-Fall-2017-2018\" strand=\"MA-Undesignated\">\n" +
            "      <Response date=\"2017-10-27T23:14:27.918Z\">&lt;![CDATA[ D ]]&gt;</Response>\n" +
            "      <ScoreInfo maxScore=\"1\" scoreDimension=\"overall\" scorePoint=\"1\" scoreStatus=\"Scored\">\n" +
            "        <ScoreRationale>D</ScoreRationale>\n" +
            "      </ScoreInfo>\n" +
            "    </Item>\n" +
            "    <Item adminDate=\"2017-10-27T23:14:25.206Z\" bankKey=\"187\" clientId=\"\" contentLevel=\"MA-Undesignated\" dropped=\"0\" format=\"EQ\" isSelected=\"1\" key=\"3252\" mimeType=\"text/plain\" numberVisits=\"1\" operational=\"0\" pageNumber=\"24\" pageTime=\"2026\" pageVisits=\"0\" position=\"24\" responseDuration=\"2026\" score=\"-1\" scoreStatus=\"NOTSCORED\" segmentId=\"(SBAC_PT)SBAC-MATH-5-Fall-2017-2018\" strand=\"MA-Undesignated\">\n" +
            "      <Response date=\"2017-10-27T23:14:30.024Z\">&lt;response type=&quot;plaintext&quot;&gt;F3&lt;/response&gt;</Response>\n" +
            "      <ScoreInfo maxScore=\"1\" scoreDimension=\"overall\" scorePoint=\"-1\" scoreStatus=\"NotScored\">\n" +
            "        <ScoreRationale>No responses found</ScoreRationale>\n" +
            "      </ScoreInfo>\n" +
            "    </Item>\n" +
            "    <Item adminDate=\"2017-10-27T23:14:26.838Z\" bankKey=\"187\" clientId=\"\" contentLevel=\"MA-Undesignated\" dropped=\"0\" format=\"MI\" isSelected=\"1\" key=\"3290\" mimeType=\"text/plain\" numberVisits=\"1\" operational=\"0\" pageNumber=\"25\" pageTime=\"2018\" pageVisits=\"0\" position=\"25\" responseDuration=\"2018\" score=\"0\" scoreStatus=\"SCORED\" segmentId=\"(SBAC_PT)SBAC-MATH-5-Fall-2017-2018\" strand=\"MA-Undesignated\">\n" +
            "      <Response date=\"2017-10-27T23:14:32.120Z\">&lt;itemResponse&gt;&lt;response id=&quot;RESPONSE&quot;&gt;&lt;value&gt;2 a&lt;/value&gt;&lt;/response&gt;&lt;/itemResponse&gt;</Response>\n" +
            "      <ScoreInfo maxScore=\"1\" scoreDimension=\"overall\" scorePoint=\"0\" scoreStatus=\"Scored\">\n" +
            "        <ScoreRationale>successfully scored</ScoreRationale>\n" +
            "      </ScoreInfo>\n" +
            "    </Item>\n" +
            "    <Item adminDate=\"2017-10-27T23:14:27.339Z\" bankKey=\"187\" clientId=\"\" contentLevel=\"MA-Undesignated\" dropped=\"0\" format=\"GI\" isSelected=\"1\" key=\"3395\" mimeType=\"text/plain\" numberVisits=\"1\" operational=\"0\" pageNumber=\"26\" pageTime=\"3833\" pageVisits=\"0\" position=\"26\" responseDuration=\"3833\" score=\"0\" scoreStatus=\"SCORED\" segmentId=\"(SBAC_PT)SBAC-MATH-5-Fall-2017-2018\" strand=\"MA-Undesignated\">\n" +
            "      <Response date=\"2017-10-27T23:14:35.965Z\">&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt;&lt;AnswerSet&gt;&lt;Question id=&quot;&quot;&gt;&lt;QuestionPart id=&quot;1&quot;&gt;&lt;ObjectSet&gt;&lt;Object&gt;&lt;PointVector&gt;{(216,221)}&lt;/PointVector&gt;&lt;EdgeVector&gt; {} &lt;/EdgeVector&gt;&lt;LabelList&gt; {} &lt;/LabelList&gt;&lt;ValueList&gt; {} &lt;/ValueList&gt;&lt;/Object&gt;&lt;Object&gt;&lt;PointVector&gt;{(256,280)}&lt;/PointVector&gt;&lt;EdgeVector&gt; {} &lt;/EdgeVector&gt;&lt;LabelList&gt; {} &lt;/LabelList&gt;&lt;ValueList&gt; {} &lt;/ValueList&gt;&lt;/Object&gt;&lt;/ObjectSet&gt;&lt;SnapPoint&gt;10@136,70;136,89;136,109;136,130;136,149;136,149;136,169;136,190;136,210;136,230;156,70;156,89;156,109;156,130;156,149;156,169;156,190;156,210;156,230;176,70;176,89;176,109;176,130;176,149;176,169;176,190;176,210;176,230;196,70;196,89;196,109;196,130;196,149;196,169;196,190;196,210;196,230;216,70;216,89;216,109;216,130;216,149;216,169;216,189;216,210;216,230;236,70;236,89;236,109;236,130;236,149;236,169;236,190;236,210;236,230;256,70;256,89;256,109;256,130;256,149;256,169;256,190;256,210;256,230;276,70;276,89;276,109;276,130;276,130;276,149;276,169;276,190;276,210;276,230;296,70;296,89;295,109;296,130;296,149;296,170;296,190;296,210;296,230&lt;/SnapPoint&gt;&lt;/QuestionPart&gt;&lt;/Question&gt;&lt;/AnswerSet&gt;</Response>\n" +
            "      <ScoreInfo maxScore=\"1\" scoreDimension=\"overall\" scorePoint=\"0\" scoreStatus=\"Scored\">\n" +
            "        <ScoreRationale>successfully scored</ScoreRationale>\n" +
            "      </ScoreInfo>\n" +
            "    </Item>\n" +
            "    <Item adminDate=\"2017-10-27T23:14:30.322Z\" bankKey=\"187\" clientId=\"\" contentLevel=\"MA-Undesignated\" dropped=\"0\" format=\"EQ\" isSelected=\"1\" key=\"3449\" mimeType=\"text/plain\" numberVisits=\"1\" operational=\"0\" pageNumber=\"27\" pageTime=\"1594\" pageVisits=\"0\" position=\"27\" responseDuration=\"1594\" score=\"-1\" scoreStatus=\"NOTSCORED\" segmentId=\"(SBAC_PT)SBAC-MATH-5-Fall-2017-2018\" strand=\"MA-Undesignated\">\n" +
            "      <Response date=\"2017-10-27T23:14:37.687Z\">&lt;response type=&quot;plaintext&quot;&gt;F34&lt;/response&gt;</Response>\n" +
            "      <ScoreInfo maxScore=\"1\" scoreDimension=\"overall\" scorePoint=\"-1\" scoreStatus=\"NotScored\">\n" +
            "        <ScoreRationale>No responses found</ScoreRationale>\n" +
            "      </ScoreInfo>\n" +
            "    </Item>\n" +
            "    <Item adminDate=\"2017-10-27T23:14:32.333Z\" bankKey=\"187\" clientId=\"\" contentLevel=\"MA-Undesignated\" dropped=\"0\" format=\"MC\" isSelected=\"1\" key=\"3549\" mimeType=\"text/plain\" numberVisits=\"2\" operational=\"0\" pageNumber=\"28\" pageTime=\"2789\" pageVisits=\"0\" position=\"28\" responseDuration=\"2789\" score=\"1\" scoreStatus=\"SCORED\" segmentId=\"(SBAC_PT)SBAC-MATH-5-Fall-2017-2018\" strand=\"MA-Undesignated\">\n" +
            "      <Response date=\"2017-10-27T23:14:40.287Z\">&lt;![CDATA[ C ]]&gt;</Response>\n" +
            "      <ScoreInfo maxScore=\"1\" scoreDimension=\"overall\" scorePoint=\"1\" scoreStatus=\"Scored\">\n" +
            "        <ScoreRationale>C</ScoreRationale>\n" +
            "      </ScoreInfo>\n" +
            "    </Item>\n" +
            "    <Item adminDate=\"2017-10-27T23:14:36.166Z\" bankKey=\"187\" clientId=\"\" contentLevel=\"MA-Undesignated\" dropped=\"0\" format=\"EQ\" isSelected=\"1\" key=\"3200\" mimeType=\"text/plain\" numberVisits=\"1\" operational=\"0\" pageNumber=\"29\" pageTime=\"1599\" pageVisits=\"0\" position=\"29\" responseDuration=\"1599\" score=\"-1\" scoreStatus=\"NOTSCORED\" segmentId=\"(SBAC_PT)SBAC-MATH-5-Fall-2017-2018\" strand=\"MA-Undesignated\">\n" +
            "      <Response date=\"2017-10-27T23:14:41.988Z\">&lt;response type=&quot;plaintext&quot;&gt;F3&lt;/response&gt;</Response>\n" +
            "      <ScoreInfo maxScore=\"1\" scoreDimension=\"overall\" scorePoint=\"-1\" scoreStatus=\"NotScored\">\n" +
            "        <ScoreRationale>No responses found</ScoreRationale>\n" +
            "      </ScoreInfo>\n" +
            "    </Item>\n" +
            "    <Item adminDate=\"2017-10-27T23:14:37.835Z\" bankKey=\"187\" clientId=\"\" contentLevel=\"MA-Undesignated\" dropped=\"0\" format=\"GI\" isSelected=\"1\" key=\"3521\" mimeType=\"text/plain\" numberVisits=\"2\" operational=\"0\" pageNumber=\"30\" pageTime=\"7055\" pageVisits=\"0\" position=\"30\" responseDuration=\"7055\" score=\"0\" scoreStatus=\"SCORED\" segmentId=\"(SBAC_PT)SBAC-MATH-5-Fall-2017-2018\" strand=\"MA-Undesignated\">\n" +
            "      <Response date=\"2017-10-27T23:14:49.223Z\">&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt;&lt;AnswerSet&gt;&lt;Question id=&quot;&quot;&gt;&lt;QuestionPart id=&quot;1&quot;&gt;&lt;ObjectSet&gt;&lt;AtomicObject&gt;{4(180,323)}&lt;/AtomicObject&gt;&lt;/ObjectSet&gt;&lt;SnapPoint&gt;40@180,87;170,139;177,190;180,292;170,343;178,395&lt;/SnapPoint&gt;&lt;/QuestionPart&gt;&lt;/Question&gt;&lt;/AnswerSet&gt;</Response>\n" +
            "      <ScoreInfo maxScore=\"2\" scoreDimension=\"overall\" scorePoint=\"0\" scoreStatus=\"Scored\">\n" +
            "        <ScoreRationale>successfully scored</ScoreRationale>\n" +
            "      </ScoreInfo>\n" +
            "    </Item>\n" +
            "  </Opportunity>\n" +
            "</TDSReport>";

        XmlMapper objectMapper = new XmlMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        TDSReport report = objectMapper.readValue(trt, TDSReport.class);
        assertThat(report).isNotNull();

        try
        {
            JAXBContext context = JAXBContext.newInstance(TDSReport.class);

            javax.xml.bind.Unmarshaller um = context.createUnmarshaller();

            report = (TDSReport)um.unmarshal(new StringReader(trt));
            assertThat(report).isNotNull();
        }
        catch (JAXBException e)
        {
            e.printStackTrace();
        }

        Jaxb2Marshaller marshaller;
    }
}