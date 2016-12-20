package tds.exam.services.impl;


import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import tds.assessment.Item;
import tds.assessment.ItemConstraint;
import tds.assessment.ItemProperty;
import tds.exam.ExamAccommodation;
import tds.exam.services.ExamAccommodationService;
import tds.exam.services.ItemPoolService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ItemPoolServiceImplTest {
    private ExamAccommodationService mockExamAccommodationService;
    private ItemPoolService itemPoolService;

    @Before
    public void setUp() {
        mockExamAccommodationService = mock(ExamAccommodationService.class);
        itemPoolService = new ItemPoolServiceImpl(mockExamAccommodationService);
    }

    @Test
    public void shouldFindOneItemAndExcludeForConstraint() {
        final UUID examId = UUID.randomUUID();
        final String segmentKey = "my-segment-key";
        final String assessmentId = "my-assessment-id";
        final String itemId1 = "item-1";
        final String itemId2 = "item-2";
        final String itemId3 = "item-3";

        List<ItemProperty> itemProperties1 = new ArrayList<>();
        itemProperties1.add(new ItemProperty("Language", "ENU", "English", itemId1));
        itemProperties1.add(new ItemProperty("Language", "ENU-Braille", "Braille English", itemId1));
        itemProperties1.add(new ItemProperty("--ITEMTYPE--", "ER", "Extended Response", itemId1));

        List<ItemProperty> itemProperties2 = new ArrayList<>();
        itemProperties2.add(new ItemProperty("Language", "ENU", "English", itemId2));
        itemProperties2.add(new ItemProperty("Language", "ENU-Braille", "Braille English", itemId2));
        itemProperties2.add(new ItemProperty("--ITEMTYPE--", "MI", "Matching Item", itemId2));
        itemProperties2.add(new ItemProperty("TestAccommodation", "TEST", "Should be excluded", itemId2));

        List<ItemProperty> itemProperties3 = new ArrayList<>();
        // No ENU accommodation, should not be returned for itempool.
        itemProperties3.add(new ItemProperty("Language", "ESN", "Spanish", itemId3));
        itemProperties3.add(new ItemProperty("--ITEMTYPE--", "MI", "Matching Item", itemId3));

        List<Item> items = new ArrayList<>();
        Item item1 = new Item(itemId1);
        item1.setItemProperties(itemProperties1);
        Item item2 = new Item(itemId2);
        item2.setItemProperties(itemProperties2);
        Item item3 = new Item(itemId3);
        item3.setItemProperties(itemProperties3);

        items.add(item1);
        items.add(item2);
        items.add(item3);

        List<ItemConstraint> itemConstraints = new ArrayList<>();
        itemConstraints.add(new ItemConstraint.Builder()
                .withAssessmentId(assessmentId)
                .withToolType("Language")
                .withToolValue("ENU")
                .withPropertyName("Language")
                .withPropertyValue("ENU")
                .withInclusive(true)
                .build());
        // This should exclude item2, the Matching Item
        itemConstraints.add(new ItemConstraint.Builder()
                .withAssessmentId(assessmentId)
                .withToolType("--ITEMTYPE--")
                .withToolValue("MI")
                .withPropertyName("--ITEMTYPE")
                .withPropertyValue("MI")
                .withInclusive(false)
                .build());
        itemConstraints.add(new ItemConstraint.Builder()
                .withAssessmentId(assessmentId)
                .withToolType("TestAccommodation")
                .withToolValue("TEST")
                .withPropertyName("TestAccommodation")
                .withPropertyValue("TEST")
                .withInclusive(false)
                .build());

        List<ExamAccommodation> examAccommodations = new ArrayList<>();
        examAccommodations.add(new ExamAccommodation.Builder()
                .withExamId(examId)
                .withType("Language")
                .withCode("ENU")
                .withDescription("English")
                .withSegmentKey(segmentKey)
                .build());
        examAccommodations.add(new ExamAccommodation.Builder()
                .withExamId(examId)
                .withType("type1")
                .withCode("TDS_T1")
                .withDescription("type 1 desc")
                .withSegmentKey(segmentKey)
                .build());
        examAccommodations.add(new ExamAccommodation.Builder()
                .withExamId(examId)
                .withType("TestAccommodation")
                .withCode("TEST")
                .withDescription("type 1 desc")
                .withSegmentKey(segmentKey)
                .build());

        when(mockExamAccommodationService.findAllAccommodations(examId)).thenReturn(examAccommodations);
        Set<Item> retItemIds = itemPoolService.getItemPool(examId, itemConstraints, items);
        verify(mockExamAccommodationService).findAllAccommodations(examId);
        assertThat(retItemIds).hasSize(1);
        assertThat(retItemIds).contains(item1);
    }

    @Test
    public void shouldFindTwoEnuItems() {
        final UUID examId = UUID.randomUUID();
        final String segmentKey = "my-segment-key";
        final String assessmentId = "my-assessment-id";
        final String itemId1 = "item-1";
        final String itemId2 = "item-2";
        final String itemId3 = "item-3";

        List<ItemProperty> itemProperties1 = new ArrayList<>();
        itemProperties1.add(new ItemProperty("Language", "ENU", "English", itemId1));
        itemProperties1.add(new ItemProperty("Language", "ENU-Braille", "Braille English", itemId1));
        itemProperties1.add(new ItemProperty("--ITEMTYPE--", "ER", "Extended Response", itemId1));

        List<ItemProperty> itemProperties2 = new ArrayList<>();
        itemProperties2.add(new ItemProperty("Language", "ENU", "English", itemId2));
        itemProperties2.add(new ItemProperty("Language", "ENU-Braille", "Braille English", itemId2));
        itemProperties2.add(new ItemProperty("--ITEMTYPE--", "MI", "Matching Item", itemId2));

        List<ItemProperty> itemProperties3 = new ArrayList<>();
        // No ENU accommodation, should not be returned for itempool.
        itemProperties3.add(new ItemProperty("Language", "ESN", "Spanish", itemId3));
        itemProperties3.add(new ItemProperty("--ITEMTYPE--", "MI", "Matching Item", itemId3));

        List<Item> items = new ArrayList<>();
        Item item1 = new Item(itemId1);
        item1.setItemProperties(itemProperties1);
        Item item2 = new Item(itemId2);
        item2.setItemProperties(itemProperties2);
        Item item3 = new Item(itemId3);
        item3.setItemProperties(itemProperties3);
        items.add(item1);
        items.add(item2);
        items.add(item3);

        List<ItemConstraint> itemConstraints = new ArrayList<>();
        itemConstraints.add(new ItemConstraint.Builder()
                .withAssessmentId(assessmentId)
                .withToolType("Language")
                .withToolValue("ENU")
                .withPropertyName("Language")
                .withPropertyValue("ENU")
                .withInclusive(true)
                .build());
        itemConstraints.add(new ItemConstraint.Builder()
                .withAssessmentId(assessmentId)
                .withToolType("--ITEMTYPE--")
                .withToolValue("ER")
                .withPropertyName("--ITEMTYPE")
                .withPropertyValue("ENU")
                .withInclusive(false)
                .build());
        itemConstraints.add(new ItemConstraint.Builder()
                .withAssessmentId(assessmentId)
                .withToolType("Language")
                .withToolValue("ESN")
                .withPropertyName("Language")
                .withPropertyValue("ESN")
                .withInclusive(false)
                .build());

        List<ExamAccommodation> examAccommodations = new ArrayList<>();
        examAccommodations.add(new ExamAccommodation.Builder()
                .withExamId(examId)
                .withType("Language")
                .withCode("ENU")
                .withDescription("English")
                .withSegmentKey(segmentKey)
                .build());
        examAccommodations.add(new ExamAccommodation.Builder()
                .withExamId(examId)
                .withType("type1")
                .withCode("TDS_T1")
                .withDescription("type 1 desc")
                .withSegmentKey(segmentKey)
                .build());

        when(mockExamAccommodationService.findAllAccommodations(examId)).thenReturn(examAccommodations);
        Set<Item> retItemIds = itemPoolService.getItemPool(examId, itemConstraints, items);
        verify(mockExamAccommodationService).findAllAccommodations(examId);
        assertThat(retItemIds).hasSize(2);
        assertThat(retItemIds).contains(item1, item2);
    }

    @Test
    public void shouldReturnZeroItemsForNoMatchingAccommodations() {
        final UUID examId = UUID.randomUUID();
        final String segmentKey = "my-segment-key";
        final String assessmentId = "my-assessment-id";
        final String itemId1 = "item-1";
        final String itemId2 = "item-2";
        final String itemId3 = "item-3";

        List<ItemProperty> itemProperties1 = new ArrayList<>();
        itemProperties1.add(new ItemProperty("Language", "ENU", "English", itemId1));
        itemProperties1.add(new ItemProperty("Language", "ENU-Braille", "Braille English", itemId1));
        itemProperties1.add(new ItemProperty("--ITEMTYPE--", "ER", "Extended Response", itemId1));

        List<ItemProperty> itemProperties2 = new ArrayList<>();
        itemProperties2.add(new ItemProperty("Language", "ENU", "English", itemId2));
        itemProperties2.add(new ItemProperty("Language", "ENU-Braille", "Braille English", itemId2));
        itemProperties2.add(new ItemProperty("--ITEMTYPE--", "MI", "Matching Item", itemId2));

        List<ItemProperty> itemProperties3 = new ArrayList<>();
        // No ENU accommodation, should not be returned for itempool.
        itemProperties3.add(new ItemProperty("Language", "ESN", "Spanish", itemId3));
        itemProperties3.add(new ItemProperty("--ITEMTYPE--", "MI", "Matching Item", itemId3));

        List<Item> items = new ArrayList<>();
        Item item1 = new Item(itemId1);
        item1.setItemProperties(itemProperties1);
        Item item2 = new Item(itemId2);
        item2.setItemProperties(itemProperties2);
        Item item3 = new Item(itemId3);
        item3.setItemProperties(itemProperties3);
        items.add(item1);
        items.add(item2);
        items.add(item3);

        List<ItemConstraint> itemConstraints = new ArrayList<>();
        itemConstraints.add(new ItemConstraint.Builder()
                .withAssessmentId(assessmentId)
                .withToolType("Language")
                .withToolValue("ENU")
                .withPropertyName("Language")
                .withPropertyValue("ENU")
                .withInclusive(true)
                .build());
        itemConstraints.add(new ItemConstraint.Builder()
                .withAssessmentId(assessmentId)
                .withToolType("--ITEMTYPE--")
                .withToolValue("ER")
                .withPropertyName("--ITEMTYPE")
                .withPropertyValue("ENU")
                .withInclusive(false)
                .build());
        itemConstraints.add(new ItemConstraint.Builder()
                .withAssessmentId(assessmentId)
                .withToolType("Language")
                .withToolValue("ESN")
                .withPropertyName("Language")
                .withPropertyValue("ESN")
                .withInclusive(false)
                .build());

        List<ExamAccommodation> examAccommodations = new ArrayList<>();
        examAccommodations.add(new ExamAccommodation.Builder()
                .withExamId(examId)
                .withType("type1")
                .withCode("TDS_T1")
                .withDescription("type 1 desc")
                .withSegmentKey(segmentKey)
                .build());

        when(mockExamAccommodationService.findAllAccommodations(examId)).thenReturn(examAccommodations);
        Set<Item> retItemIds = itemPoolService.getItemPool(examId, itemConstraints, items);
        verify(mockExamAccommodationService).findAllAccommodations(examId);
        assertThat(retItemIds).isEmpty();
    }

    @Test
    public void shouldFilterByFieldTest() {
        final UUID examId = UUID.randomUUID();
        final String segmentKey = "my-segment-key";
        final String assessmentId = "my-assessment-id";
        final String itemId1 = "item-1";
        final String itemId2 = "item-2";
        final String itemId3 = "item-3";

        List<ItemProperty> itemProperties1 = new ArrayList<>();
        itemProperties1.add(new ItemProperty("Language", "ENU", "English", itemId1));
        itemProperties1.add(new ItemProperty("--ITEMTYPE--", "ER", "Extended Response", itemId1));

        List<ItemProperty> itemProperties2 = new ArrayList<>();
        itemProperties1.add(new ItemProperty("Language", "ENU", "English", itemId2));
        itemProperties1.add(new ItemProperty("--ITEMTYPE--", "ER", "Extended Response", itemId2));

        List<ItemProperty> itemProperties3 = new ArrayList<>();
        itemProperties1.add(new ItemProperty("Language", "ENU", "English", itemId3));
        itemProperties1.add(new ItemProperty("--ITEMTYPE--", "ER", "Extended Response", itemId3));

        List<Item> items = new ArrayList<>();
        Item ftItem1 = new Item(itemId1);
        ftItem1.setFieldTest(true);
        ftItem1.setItemProperties(itemProperties1);

        Item ftItem2 = new Item(itemId2);
        ftItem2.setFieldTest(true);
        ftItem2.setItemProperties(itemProperties2);

        Item regularItem = new Item(itemId3);
        regularItem.setFieldTest(false);
        regularItem.setItemProperties(itemProperties3);

        items.add(ftItem1);
        items.add(ftItem2);
        items.add(regularItem);

        List<ItemConstraint> itemConstraints = new ArrayList<>();
        itemConstraints.add(new ItemConstraint.Builder()
            .withAssessmentId(assessmentId)
            .withToolType("Language")
            .withToolValue("ENU")
            .withPropertyName("Language")
            .withPropertyValue("ENU")
            .withInclusive(true)
            .build());

        List<ExamAccommodation> examAccommodations = new ArrayList<>();
        examAccommodations.add(new ExamAccommodation.Builder()
            .withExamId(examId)
            .withType("Language")
            .withCode("ENU")
            .withDescription("English")
            .withSegmentKey(segmentKey)
            .build());

        when(mockExamAccommodationService.findAllAccommodations(examId)).thenReturn(examAccommodations);
        Set<Item> retFtItems = itemPoolService.getItemPool(examId, itemConstraints, items, true);
        verify(mockExamAccommodationService).findAllAccommodations(examId);
        assertThat(retFtItems).hasSize(2);

        Item retItem1 = null;
        Item retItem2 = null;

        for (Item item : retFtItems) {
            if (item.getId().equals(itemId1)) {
                retItem1 = item;
            } else if (item.getId().equals(itemId2)) {
                retItem2 = item;
            }
        }

        assertThat(retItem1).isNotNull();
        assertThat(retItem2).isNotNull();

        Set<Item> nonFtItems = itemPoolService.getItemPool(examId, itemConstraints, items, false);
        assertThat(nonFtItems).hasSize(1);

        Item nonFtItem = null;

        for (Item item : nonFtItems) {
            if (item.getId().equals(itemId3)) {
                nonFtItem = item;
            }
        }

        assertThat(nonFtItem).isNotNull();
    }

    @Test
    public void shouldReturnZeroItemsForNoENUConstraint() {
        final UUID examId = UUID.randomUUID();
        final String segmentKey = "my-segment-key";
        final String assessmentId = "my-assessment-id";
        final String itemId1 = "item-1";
        final String itemId2 = "item-2";

        List<ItemProperty> itemProperties1 = new ArrayList<>();
        itemProperties1.add(new ItemProperty("Language", "ENU", "English", itemId1));
        itemProperties1.add(new ItemProperty("Language", "ENU-Braille", "Braille English", itemId1));
        itemProperties1.add(new ItemProperty("--ITEMTYPE--", "ER", "Extended Response", itemId1));

        List<ItemProperty> itemProperties2 = new ArrayList<>();
        itemProperties2.add(new ItemProperty("Language", "ENU", "English", itemId2));
        itemProperties2.add(new ItemProperty("Language", "ENU-Braille", "Braille English", itemId2));
        itemProperties2.add(new ItemProperty("--ITEMTYPE--", "MI", "Matching Item", itemId2));

        List<Item> items = new ArrayList<>();
        Item item1 = new Item(itemId1);
        item1.setItemProperties(itemProperties1);
        Item item2 = new Item(itemId2);
        item2.setItemProperties(itemProperties2);
        items.add(item1);
        items.add(item2);

        List<ItemConstraint> itemConstraints = new ArrayList<>();
        itemConstraints.add(new ItemConstraint.Builder()
            .withAssessmentId(assessmentId)
            .withToolType("--ITEMTYPE--")
            .withToolValue("ER")
            .withPropertyName("--ITEMTYPE")
            .withPropertyValue("ENU")
            .withInclusive(false)
            .build());
        itemConstraints.add(new ItemConstraint.Builder()
            .withAssessmentId(assessmentId)
            .withToolType("Language")
            .withToolValue("ESN")
            .withPropertyName("Language")
            .withPropertyValue("ESN")
            .withInclusive(true)
            .build());

        List<ExamAccommodation> examAccommodations = new ArrayList<>();
        examAccommodations.add(new ExamAccommodation.Builder()
            .withExamId(examId)
            .withType("Language")
            .withCode("ENU")
            .withDescription("English")
            .withSegmentKey(segmentKey)
            .build());
        examAccommodations.add(new ExamAccommodation.Builder()
            .withExamId(examId)
            .withType("type1")
            .withCode("TDS_T1")
            .withDescription("type 1 desc")
            .withSegmentKey(segmentKey)
            .build());
        examAccommodations.add(new ExamAccommodation.Builder()
            .withExamId(examId)
            .withType("type1")
            .withCode("TDS_T1")
            .withDescription("type 1 desc")
            .withSegmentKey(segmentKey)
            .build());

        when(mockExamAccommodationService.findAllAccommodations(examId)).thenReturn(examAccommodations);
        Set<Item> retItemIds = itemPoolService.getItemPool(examId, itemConstraints, items);
        verify(mockExamAccommodationService).findAllAccommodations(examId);
        assertThat(retItemIds).isEmpty();
    }
}

