package tds.exam.services.item.selection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import tds.assessment.ContentLevelSpecification;
import tds.assessment.Item;
import tds.assessment.ItemControlParameter;
import tds.assessment.ItemGroup;
import tds.assessment.ItemMeasurement;
import tds.assessment.ItemProperty;
import tds.assessment.Segment;
import tds.assessment.SegmentItemInformation;
import tds.exam.builder.ItemBuilder;
import tds.exam.builder.SegmentBuilder;
import tds.exam.services.AssessmentService;
import tds.itemselection.api.ItemSelectionException;
import tds.itemselection.loader.TestSegment;
import tds.itemselection.services.SegmentService;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SegmentServiceImplTest {
    @Mock
    private AssessmentService mockAssessmentService;

    private SegmentService segmentService;

    @Before
    public void setUp() {
        segmentService = new SegmentServiceImpl(mockAssessmentService);
    }

    @After
    public void tearDown() {
    }

    @Test(expected = ItemSelectionException.class)
    public void shouldThrowIfSegmentInformationCannotBeFound() throws Exception {
        when(mockAssessmentService.findSegmentItemInformation("key")).thenReturn(Optional.empty());
        segmentService.getSegment("key");
    }

    @Test
    public void shouldReturnTestSegmentThatHasItemGroups() throws Exception {
        Segment segment = new SegmentBuilder()
            .withKey("segmentKey")
            .build();
        Item segmentItem = new ItemBuilder("item-123")
            .withGroupId("group-123")
            .build();
        Item parentItem = new ItemBuilder("item-567")
            .withGroupId("group-567")
            .build();

        segmentItem.setSegmentKey(segment.getKey());
        parentItem.setSegmentKey(segment.getAssessmentKey());

        ItemMeasurement itemMeasurement = new ItemMeasurement.Builder()
            .withItemKey("item-123")
            .withDimension("dimensions")
            .withItemResponseTheoryModel("IRT3PL")
            .withParameterName("name")
            .withParameterValue(1.3f)
            .withParameterNumber(1)
            .build();

        ItemGroup itemGroup = new ItemGroup("group-567", 1, 10, 1.2f);

        ContentLevelSpecification reportingCategory = new ContentLevelSpecification.Builder()
            .withAbilityWeight(2.1f)
            .withAdaptiveCut(2.2f)
            .withBpWeight(1.2f)
            .withContentLevel("contentLevel")
            .withElementType(1)
            .withMaxItems(1)
            .withMinItems(0)
            .withPrecisionTarget(2.3f)
            .withPrecisionTargetMetWeight(2.4f)
            .withPrecisionTargetNotMetWeight(2.5f)
            .withReportingCategory(true)
            .withScalar(2.6f)
            .withStartAbility(2.7f)
            .withStartInfo(2.8f)
            .withStrictMax(true)
            .build();

        ContentLevelSpecification bpElement = new ContentLevelSpecification.Builder()
            .withAbilityWeight(2.1f)
            .withAdaptiveCut(2.2f)
            .withBpWeight(1.2f)
            .withContentLevel("bpLevel")
            .withElementType(1)
            .withMaxItems(1)
            .withMinItems(0)
            .withPrecisionTarget(2.3f)
            .withPrecisionTargetMetWeight(2.4f)
            .withPrecisionTargetNotMetWeight(2.5f)
            .withReportingCategory(false)
            .withScalar(2.6f)
            .withStartAbility(2.7f)
            .withStartInfo(2.8f)
            .withStrictMax(true)
            .build();

        ItemProperty property = new ItemProperty("propName", "value", "item-123");
        ItemProperty property2 = new ItemProperty("propName2", "value", "item-123");

        ItemControlParameter control = new ItemControlParameter("elementId", "name", "value");

        SegmentItemInformation segmentItemInformation = new SegmentItemInformation.Builder()
            .withSegment(segment)
            .withSegmentItems(Collections.singletonList(segmentItem))
            .withParentItems(Collections.singletonList(parentItem))
            .withItemGroups(Collections.singletonList(itemGroup))
            .withItemMeasurements(Collections.singletonList(itemMeasurement))
            .withContentLevelSpecifications(Arrays.asList(reportingCategory, bpElement))
            .withPoolFilterProperties(Arrays.asList(property, property2))
            .withControlParameters(Collections.singletonList(control))
            .build();

        when(mockAssessmentService.findSegmentItemInformation("segmentKey"))
            .thenReturn(Optional.of(segmentItemInformation));

        TestSegment testSegment = segmentService.getSegment("segmentKey");


        assertThat(testSegment.getBp()).isNotNull();

        assertThat(testSegment.getPool().getItemGroup("group-123")).isNotNull();
    }

    @Test
    public void shouldReturnTestSegmentThatHasZeroItemGroups() throws Exception {
        Segment segment = new SegmentBuilder()
            .withKey("segmentKey")
            .build();
        Item segmentItem = new ItemBuilder("187-1792")
            .withItemType("MC")
            .withGroupId("I-187-1792")
            .withGroupKey("I-187-1792_A")
            .withBlockId("A")
            .build();

        segmentItem.setPosition(1);
        segmentItem.setFieldTest(true);
        segmentItem.setRequired(true);
        segmentItem.setStrand("SBAC_PT-MA-Undesignated");
        segmentItem.setPrintable(false);
        segmentItem.setBankKey(187);
        segmentItem.setItemKey(1792);

        segmentItem.setSegmentKey(segment.getKey());

        ItemMeasurement itemMeasurement = new ItemMeasurement.Builder()
            .withItemKey("item-123")
            .withDimension("dimensions")
            .withItemResponseTheoryModel("IRT3PL")
            .withParameterName("name")
            .withParameterValue(1.3f)
            .withParameterNumber(1)
            .build();

        ContentLevelSpecification reportingCategory = new ContentLevelSpecification.Builder()
            .withAbilityWeight(2.1f)
            .withAdaptiveCut(2.2f)
            .withBpWeight(1.2f)
            .withContentLevel("contentLevel")
            .withElementType(1)
            .withMaxItems(1)
            .withMinItems(0)
            .withPrecisionTarget(2.3f)
            .withPrecisionTargetMetWeight(2.4f)
            .withPrecisionTargetNotMetWeight(2.5f)
            .withReportingCategory(true)
            .withScalar(2.6f)
            .withStartAbility(2.7f)
            .withStartInfo(2.8f)
            .withStrictMax(true)
            .build();

        ContentLevelSpecification bpElement = new ContentLevelSpecification.Builder()
            .withAbilityWeight(2.1f)
            .withAdaptiveCut(2.2f)
            .withBpWeight(1.2f)
            .withContentLevel("bpLevel")
            .withElementType(1)
            .withMaxItems(1)
            .withMinItems(0)
            .withPrecisionTarget(2.3f)
            .withPrecisionTargetMetWeight(2.4f)
            .withPrecisionTargetNotMetWeight(2.5f)
            .withReportingCategory(false)
            .withScalar(2.6f)
            .withStartAbility(2.7f)
            .withStartInfo(2.8f)
            .withStrictMax(true)
            .build();

        ItemProperty property = new ItemProperty("propName", "value", "item-123");
        ItemProperty property2 = new ItemProperty("propName2", "value", "item-123");

        ItemControlParameter control = new ItemControlParameter("elementId", "name", "value");

        SegmentItemInformation segmentItemInformation = new SegmentItemInformation.Builder()
            .withSegment(segment)
            .withSegmentItems(Collections.singletonList(segmentItem))
            .withParentItems(Collections.emptyList())
            .withItemGroups(Collections.emptyList())
            .withItemMeasurements(Collections.singletonList(itemMeasurement))
            .withContentLevelSpecifications(Arrays.asList(reportingCategory, bpElement))
            .withPoolFilterProperties(Arrays.asList(property, property2))
            .withControlParameters(Collections.singletonList(control))
            .build();

        when(mockAssessmentService.findSegmentItemInformation("segmentKey"))
            .thenReturn(Optional.of(segmentItemInformation));

        TestSegment testSegment = segmentService.getSegment("segmentKey");


        assertThat(testSegment.getBp()).isNotNull();

        assertThat(testSegment.getPool().getItemGroup("I-187-1792")).isNotNull();
    }
}