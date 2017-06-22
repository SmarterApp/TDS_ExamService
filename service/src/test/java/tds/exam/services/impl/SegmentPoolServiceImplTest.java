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

package tds.exam.services.impl;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import tds.assessment.Item;
import tds.assessment.ItemConstraint;
import tds.assessment.ItemProperty;
import tds.assessment.Segment;
import tds.assessment.Strand;
import tds.common.Algorithm;
import tds.exam.ExamAccommodation;
import tds.exam.builder.ItemBuilder;
import tds.exam.models.SegmentPoolInfo;
import tds.exam.services.ItemPoolService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SegmentPoolServiceImplTest {
    private SegmentPoolServiceImpl segmentPoolService;
    private ItemPoolService mockItemPoolService;

    @Before
    public void setUp() {
        mockItemPoolService = mock(ItemPoolService.class);
        segmentPoolService = new SegmentPoolServiceImpl(mockItemPoolService);
    }

    @Test
    public void shouldReturnSegmentPoolInfoForThreeItemsEqualLengthAndStrandCount(){
        final UUID examId = UUID.randomUUID();
        final String assessmentId = "my-assessment-id";
        final String segmentKey = "my-segment-key";
        Segment segment = new Segment(segmentKey, Algorithm.ADAPTIVE_2);
        segment.setMinItems(5);
        segment.setMaxItems(13);

        Set<Strand> strands = new HashSet<>();
        Strand includedStrand1 = new Strand.Builder()
                .withName("included-strand1")
                .withMinItems(3)
                .withMaxItems(8)
                .withSegmentKey(segmentKey)
                .withAdaptiveCut(-32.123F)
                .build();
        Strand includedStrand2 = new Strand.Builder()
                .withName("included-strand2")
                .withMinItems(1)
                .withMaxItems(5)
                .withSegmentKey(segmentKey)
                .withAdaptiveCut(-37.523F)
                .build();
        Strand excludedStrand = new Strand.Builder()
                .withName("excluded-strand")
                .withMinItems(1)
                .withMaxItems(5)
                .withSegmentKey(segmentKey)
                .withAdaptiveCut(null)
                .build();

        strands.add(includedStrand1);
        strands.add(includedStrand2);
        strands.add(excludedStrand);

        segment.setStrands(strands);

        final String itemId1 = "item-1";
        final String itemId2 = "item-2";
        final String itemId3 = "item-3";
        final String ftItemId = "ft-item";
        final String excludedStrandItemId = "excluded-strand-item";

        List<ItemProperty> enuProps1 = new ArrayList<>();
        enuProps1.add(new ItemProperty("Language", "ENU", "English", itemId1));
        enuProps1.add(new ItemProperty("Language", "ENU-Braille", "Braille English", itemId1));
        enuProps1.add(new ItemProperty("--ITEMTYPE--", "ER", "Extended Response", itemId1));

        List<ItemProperty> enuProps2 = new ArrayList<>();
        enuProps2.add(new ItemProperty("Language", "ENU", "English", itemId2));
        enuProps2.add(new ItemProperty("Language", "ENU-Braille", "Braille English", itemId2));
        enuProps2.add(new ItemProperty("--ITEMTYPE--", "MI", "Matching Item", itemId2));

        List<ItemProperty> enuProps3 = new ArrayList<>();
        enuProps2.add(new ItemProperty("Language", "ENU", "English", itemId3));
        enuProps2.add(new ItemProperty("Language", "ENU-Braille", "Braille English", itemId3));
        enuProps2.add(new ItemProperty("--ITEMTYPE--", "MI", "Matching Item", itemId3));

        List<ItemProperty> ftProps = new ArrayList<>();
        enuProps2.add(new ItemProperty("Language", "ENU", "English", ftItemId));
        enuProps2.add(new ItemProperty("Language", "ENU-Braille", "Braille English", ftItemId));
        enuProps2.add(new ItemProperty("--ITEMTYPE--", "MI", "Matching Item", ftItemId));

        List<ItemProperty> excludedStrandItemProps = new ArrayList<>();
        enuProps2.add(new ItemProperty("Language", "ENU", "English", excludedStrandItemId));
        enuProps2.add(new ItemProperty("Language", "ENU-Braille", "Braille English", excludedStrandItemId));
        enuProps2.add(new ItemProperty("--ITEMTYPE--", "MI", "Matching Item", excludedStrandItemId));

        List<Item> items = new ArrayList<>();
        Item item1 = new ItemBuilder(itemId1)
            .withStrand(includedStrand1.getName())
            .withItemProperties(enuProps1)
            .build();
        Item item2 = new ItemBuilder(itemId2)
            .withStrand(includedStrand1.getName())
            .withItemProperties(enuProps2)
            .build();
        Item item3 = new ItemBuilder(itemId3)
            .withStrand(includedStrand2.getName())
            .withItemProperties(enuProps3)
            .build();

        // Should be included in the itemPoolIds list, but wont be factored into other calculations because its an FT item
        Item ftItem = new ItemBuilder(ftItemId)
            .withStrand(includedStrand2.getName())
            .withItemProperties(ftProps)
            .withFieldTest(true)
            .build();

        // This item will be included in the itemPoolIds list, but wont be factored into other calculations
        Item excludedStrandItem = new ItemBuilder(excludedStrandItemId)
            .withItemProperties(excludedStrandItemProps)
            .withStrand(excludedStrand.getName())       // This should be excluded from strand calculations
            .build();

        items.add(item1);
        items.add(item2);
        items.add(item3);
        items.add(ftItem);
        items.add(excludedStrandItem);
        segment.setItems(items);

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
                .withInclusive(true)
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
        examAccommodations.add(new ExamAccommodation.Builder(UUID.randomUUID())
                .withExamId(examId)
                .withType("Language")
                .withCode("ENU")
                .withDescription("English")
                .withSegmentKey(segmentKey)
                .build());
        examAccommodations.add(new ExamAccommodation.Builder(UUID.randomUUID())
                .withExamId(examId)
                .withType("type1")
                .withCode("TDS_T1")
                .withDescription("type 1 desc")
                .withSegmentKey(segmentKey)
                .build());

        when(mockItemPoolService.getItemPool(examId, itemConstraints, segment.getItems("ENU"))).thenReturn(new HashSet<>(items));
        SegmentPoolInfo segmentPoolInfo = segmentPoolService.computeSegmentPool(examId, segment, itemConstraints, "ENU");
        assertThat(segmentPoolInfo).isNotNull();
        assertThat(segmentPoolInfo.getPoolCount()).isEqualTo(6);
        assertThat(segmentPoolInfo.getLength()).isEqualTo(6);

        List<String> itemIds = segmentPoolInfo.getItemPool().stream().map(Item::getId).collect(Collectors.toList());
        assertThat(itemIds).contains(itemId1, itemId2, excludedStrandItemId, ftItemId);
    }

    @Test
    public void shouldReturnSegmentPoolInfoForThreeItems(){
        final UUID examId = UUID.randomUUID();
        final String assessmentId = "my-assessment-id";
        final String segmentKey = "my-segment-key";
        Segment segment = new Segment(segmentKey, Algorithm.ADAPTIVE_2);
        segment.setMinItems(5);
        segment.setMaxItems(6);

        Set<Strand> strands = new HashSet<>();
        Strand includedStrand1 = new Strand.Builder()
            .withName("included-strand1")
            .withMinItems(3)
            .withMaxItems(8)
            .withSegmentKey(segmentKey)
            .withAdaptiveCut(-32.123F)
            .build();
        Strand includedStrand2 = new Strand.Builder()
            .withName("included-strand2")
            .withMinItems(1)
            .withMaxItems(5)
            .withSegmentKey(segmentKey)
            .withAdaptiveCut(-37.523F)
            .build();
        Strand excludedStrand = new Strand.Builder()
            .withName("excluded-strand")
            .withMinItems(1)
            .withMaxItems(5)
            .withSegmentKey(segmentKey)
            .withAdaptiveCut(null)
            .build();

        strands.add(includedStrand1);
        strands.add(includedStrand2);
        strands.add(excludedStrand);

        segment.setStrands(strands);

        final String itemId1 = "item-1";
        final String itemId2 = "item-2";
        final String itemId3 = "item-3";
        final String ftItemId = "ft-item";
        final String excludedStrandItemId = "excluded-strand-item";

        List<ItemProperty> enuProps1 = new ArrayList<>();
        enuProps1.add(new ItemProperty("Language", "ENU", "English", itemId1));
        enuProps1.add(new ItemProperty("Language", "ENU-Braille", "Braille English", itemId1));
        enuProps1.add(new ItemProperty("--ITEMTYPE--", "ER", "Extended Response", itemId1));

        List<ItemProperty> enuProps2 = new ArrayList<>();
        enuProps2.add(new ItemProperty("Language", "ENU", "English", itemId2));
        enuProps2.add(new ItemProperty("Language", "ENU-Braille", "Braille English", itemId2));
        enuProps2.add(new ItemProperty("--ITEMTYPE--", "MI", "Matching Item", itemId2));

        List<ItemProperty> enuProps3 = new ArrayList<>();
        enuProps2.add(new ItemProperty("Language", "ENU", "English", itemId3));
        enuProps2.add(new ItemProperty("Language", "ENU-Braille", "Braille English", itemId3));
        enuProps2.add(new ItemProperty("--ITEMTYPE--", "MI", "Matching Item", itemId3));

        List<ItemProperty> ftProps = new ArrayList<>();
        enuProps2.add(new ItemProperty("Language", "ENU", "English", ftItemId));
        enuProps2.add(new ItemProperty("Language", "ENU-Braille", "Braille English", ftItemId));
        enuProps2.add(new ItemProperty("--ITEMTYPE--", "MI", "Matching Item", ftItemId));

        List<ItemProperty> excludedStrandItemProps = new ArrayList<>();
        enuProps2.add(new ItemProperty("Language", "ENU", "English", excludedStrandItemId));
        enuProps2.add(new ItemProperty("Language", "ENU-Braille", "Braille English", excludedStrandItemId));
        enuProps2.add(new ItemProperty("--ITEMTYPE--", "MI", "Matching Item", excludedStrandItemId));

        List<Item> items = new ArrayList<>();
        Item item1 = new ItemBuilder(itemId1)
            .withStrand(includedStrand1.getName())
            .withItemProperties(enuProps1)
            .build();

        Item item2 = new ItemBuilder(itemId2)
            .withStrand(includedStrand1.getName())
            .withItemProperties(enuProps2)
            .build();

        Item item3 = new ItemBuilder(itemId3)
            .withStrand(includedStrand2.getName())
            .withItemProperties(enuProps3)
            .build();

        // Should be included in the itemPoolIds list, but wont be factored into other calculations because its an FT item
        Item ftItem = new ItemBuilder(ftItemId)
            .withStrand(includedStrand2.getName())
            .withItemProperties(ftProps)
            .withFieldTest(true)
            .build();

        // This item will be included in the itemPoolIds list, but wont be factored into other calculations
        Item excludedStrandItem = new ItemBuilder(excludedStrandItemId)
            .withItemProperties(excludedStrandItemProps)
            .withStrand(excludedStrand.getName())       // This should be excluded from strand calculations
            .build();

        items.add(item1);
        items.add(item2);
        items.add(item3);
        items.add(ftItem);
        items.add(excludedStrandItem);
        segment.setItems(items);

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
            .withInclusive(true)
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
        examAccommodations.add(new ExamAccommodation.Builder(UUID.randomUUID())
            .withExamId(examId)
            .withType("Language")
            .withCode("ENU")
            .withDescription("English")
            .withSegmentKey(segmentKey)
            .build());
        examAccommodations.add(new ExamAccommodation.Builder(UUID.randomUUID())
            .withExamId(examId)
            .withType("type1")
            .withCode("TDS_T1")
            .withDescription("type 1 desc")
            .withSegmentKey(segmentKey)
            .build());

        when(mockItemPoolService.getItemPool(examId, itemConstraints, segment.getItems("ENU"))).thenReturn(new HashSet<>(items));
        SegmentPoolInfo segmentPoolInfo = segmentPoolService.computeSegmentPool(examId, segment, itemConstraints, "ENU");
        assertThat(segmentPoolInfo).isNotNull();
        assertThat(segmentPoolInfo.getPoolCount()).isEqualTo(6);
        assertThat(segmentPoolInfo.getLength()).isEqualTo(4);

        List<String> itemIds = segmentPoolInfo.getItemPool().stream().map(Item::getId).collect(Collectors.toList());
        assertThat(itemIds).contains(itemId1, itemId2, excludedStrandItemId, ftItemId);
    }
}
