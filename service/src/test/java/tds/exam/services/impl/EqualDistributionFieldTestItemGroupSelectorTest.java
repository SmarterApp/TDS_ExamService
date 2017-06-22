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
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tds.assessment.Assessment;
import tds.assessment.Item;
import tds.assessment.Segment;
import tds.exam.Exam;
import tds.exam.builder.AssessmentBuilder;
import tds.exam.builder.ExamBuilder;
import tds.exam.builder.ItemBuilder;
import tds.exam.builder.SegmentBuilder;
import tds.exam.models.FieldTestItemGroup;
import tds.exam.services.FieldTestItemGroupSelector;
import tds.exam.services.ItemPoolService;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EqualDistributionFieldTestItemGroupSelectorTest {
    private FieldTestItemGroupSelector selector;

    @Mock
    private ItemPoolService mockItemPoolService;

    @Before
    public void setUp() {
        selector = new EqualDistributionFieldTestItemGroupSelector(mockItemPoolService);
    }

    @Test
    public void shouldDistributeFieldTestItemGroupsEqually() {
        Map<String, Integer> itemGroupOccurances = new HashMap<>();
        final String assessmentKey = "assessment-key123";
        final int numberOfExamsToTest = 20;
        Segment segment = new SegmentBuilder()
            .withAssessmentKey(assessmentKey)
            .withFieldTestStartPosition(3)
            .withFieldTestEndPosition(7)
            .withFieldTestMinItems(2)
            .withFieldTestMaxItems(2)
            .build();
        Assessment assessment = new AssessmentBuilder()
            .withSegments(Arrays.asList(segment))
            .build();
        Item ftItem1 = new ItemBuilder("item-1")
            .withGroupId("group-id-1")
            .withGroupKey("group-key-1")
            .withFieldTest(true)
            .build();
        Item ftItem2 = new ItemBuilder("item-2")
            .withGroupId("group-id-2")
            .withGroupKey("group-key-2")
            .withFieldTest(true)
            .build();
        Item ftItem3 = new ItemBuilder("item-3")
            .withGroupId("group-id-3")
            .withGroupKey("group-key-3")
            .withFieldTest(true)
            .build();
        Item ftItem4 = new ItemBuilder("item-4")
            .withGroupId("group-id-4")
            .withGroupKey("group-key-4")
            .withFieldTest(true)
            .build();

        Set<Item> fieldTestItems = new HashSet<>(Arrays.asList(ftItem1, ftItem2, ftItem3, ftItem4));

        when(mockItemPoolService.getFieldTestItemPool(any(), eq(assessment.getItemConstraints()), any()))
            .thenReturn(fieldTestItems, fieldTestItems, fieldTestItems);

        for (int i = 0; i < numberOfExamsToTest; i++) {
            Exam exam = new ExamBuilder().withLanguageCode("ENU").build();
            List<FieldTestItemGroup> retFtItemGroupsForExam = selector.selectLeastUsedItemGroups(exam, new HashSet<>(),
                assessment, segment, segment.getFieldTestMinItems());
            assertThat(retFtItemGroupsForExam).hasSize(2);

            // Keep count of each item group selected by the algorithm.
            for (FieldTestItemGroup selectedItemgroup : retFtItemGroupsForExam) {
                Integer counter = itemGroupOccurances.get(selectedItemgroup.getGroupKey());
                if (counter == null) {
                    itemGroupOccurances.put(selectedItemgroup.getGroupKey(), 1);
                } else {
                    itemGroupOccurances.put(selectedItemgroup.getGroupKey(), counter + 1);
                }
            }
        }

        verify(mockItemPoolService, times(numberOfExamsToTest)).getFieldTestItemPool(any(), eq(assessment.getItemConstraints()), any());

        assertThat(itemGroupOccurances).hasSize(4);

        int min = itemGroupOccurances.values().stream().min(Integer::compareTo).get();
        int max = itemGroupOccurances.values().stream().max(Integer::compareTo).get();
        // Make sure the difference between the maximum and minimum value of each counter is not greater than 1
        assertThat(max - min).isLessThanOrEqualTo(1);

    }

    @Test
    public void shouldExcludeGroupAlreadySelected() {
        Exam exam = new ExamBuilder().withLanguageCode("ENU").build();
        final String assessmentKey = "assessment-key123";
        Segment segment = new SegmentBuilder()
            .withAssessmentKey(assessmentKey)
            .withFieldTestStartPosition(3)
            .withFieldTestEndPosition(7)
            .withFieldTestMinItems(4)
            .withFieldTestMaxItems(4)
            .build();
        Assessment assessment = new AssessmentBuilder()
            .withSegments(Arrays.asList(segment))
            .build();
        Item ftItem1 = new ItemBuilder("item-1")
            .withGroupId("group-id-1")
            .withGroupKey("group-key-1")
            .withFieldTest(true)
            .build();
        Item ftItem2 = new ItemBuilder("item-2")
            .withGroupId("group-id-2")
            .withGroupKey("group-key-2")
            .withFieldTest(true)
            .build();
        Item ftItem3 = new ItemBuilder("item-3")
            .withGroupId("group-id-3")
            .withGroupKey("group-key-3")
            .withFieldTest(true)
            .build();
        // This item should be excluded - it has already been assigned
        Item excludedFtItem = new ItemBuilder("item-4")
            .withGroupId("group-id-4")
            .withGroupKey("group-key-4")
            .withFieldTest(true)
            .build();

        when(mockItemPoolService.getFieldTestItemPool(eq(exam.getId()), eq(assessment.getItemConstraints()), any()))
            .thenReturn(new HashSet<>(Arrays.asList(ftItem1, ftItem2, ftItem3, excludedFtItem)));
        List<FieldTestItemGroup> retFtItemGroups = selector.selectLeastUsedItemGroups(exam, new HashSet<>(Arrays.asList(excludedFtItem.getGroupId())),
            assessment, segment, segment.getFieldTestMinItems());
        verify(mockItemPoolService).getFieldTestItemPool(eq(exam.getId()), eq(assessment.getItemConstraints()), any());
        assertThat(retFtItemGroups).hasSize(3);

        FieldTestItemGroup selectedItemGroup = null;
        FieldTestItemGroup notSelectedItemGroup = null;
        for (FieldTestItemGroup itemGroup : retFtItemGroups) {
            if (itemGroup.getGroupKey().equals(ftItem1.getGroupKey())) {
                selectedItemGroup = itemGroup;
            } else if (itemGroup.getGroupKey().equals(excludedFtItem.getGroupKey())) {
                notSelectedItemGroup = itemGroup;
            }
        }

        assertThat(selectedItemGroup.getGroupId()).isEqualTo(ftItem1.getGroupId());
        assertThat(selectedItemGroup.getBlockId()).isEqualTo(ftItem1.getBlockId());
        assertThat(selectedItemGroup.getItemCount()).isEqualTo(1);
        assertThat(selectedItemGroup.getExamId()).isEqualTo(exam.getId());
        assertThat(selectedItemGroup.getDeletedAt()).isNull();

        assertThat(notSelectedItemGroup).isNull();
    }

    @Test
    public void shouldSelectAndCacheNewItemsFromDifferentExam () {
        Exam exam1 = new ExamBuilder().build();
        Exam exam2 = new ExamBuilder().build();
        final String assessmentKey = "assessment-key123";
        Segment segment = new SegmentBuilder()
            .withAssessmentKey(assessmentKey)
            .withFieldTestStartPosition(3)
            .withFieldTestEndPosition(7)
            .withFieldTestMinItems(4)
            .withFieldTestMaxItems(4)
            .build();
        Assessment assessment = new AssessmentBuilder()
            .withSegments(Arrays.asList(segment))
            .build();
        // Exam 1 Item
        Item exam1ItemGroup = new ItemBuilder("itemgroup-1")
            .withGroupId("group-id-1")
            .withGroupKey("group-key-1")
            .withFieldTest(true)
            .build();
        // Exam 2 items
        Item exam2ItemGroup1 = new ItemBuilder("itemgroup-2")
            .withGroupId("group-id-2")
            .withGroupKey("group-key-2")
            .withFieldTest(true)
            .build();
        Item exam2ItemGroup2 = new ItemBuilder("itemgroup-3")
            .withGroupId("group-id-3")
            .withGroupKey("group-key-3")
            .withFieldTest(true)
            .build();

        // Exam 1
        when(mockItemPoolService.getFieldTestItemPool(eq(exam1.getId()), eq(assessment.getItemConstraints()), any()))
            .thenReturn(new HashSet<>(Arrays.asList(exam1ItemGroup)));

        // Exam 2
        when(mockItemPoolService.getFieldTestItemPool(eq(exam2.getId()), eq(assessment.getItemConstraints()), any()))
            .thenReturn(new HashSet<>(Arrays.asList(exam2ItemGroup1, exam2ItemGroup2)));
        List<FieldTestItemGroup> retFtItemGroupsExam1 = selector.selectLeastUsedItemGroups(exam1, new HashSet<>(),
            assessment, segment, segment.getFieldTestMinItems());
        List<FieldTestItemGroup> retFtItemGroupsExam2 = selector.selectLeastUsedItemGroups(exam2, new HashSet<>(),
            assessment, segment, segment.getFieldTestMinItems());

        assertThat(retFtItemGroupsExam1).hasSize(1);
        assertThat(retFtItemGroupsExam1.get(0).getGroupKey()).isEqualTo(exam1ItemGroup.getGroupKey());

        assertThat(retFtItemGroupsExam2).hasSize(2);

        FieldTestItemGroup group1 = null;
        FieldTestItemGroup group2 = null;
        for (FieldTestItemGroup itemGroup : retFtItemGroupsExam2) {
            if (itemGroup.getGroupKey().equals(exam2ItemGroup1.getGroupKey())) {
                group1 = itemGroup;
            } else if (itemGroup.getGroupKey().equals(exam2ItemGroup2.getGroupKey())) {
                group2 = itemGroup;
            }
        }

        assertThat(group1).isNotNull();
        assertThat(group2).isNotNull();
    }

    @Test
    public void shouldSelectMultiItemGroups() {
        Exam exam = new ExamBuilder().build();
        final String assessmentKey = "assessment-key123";
        Segment segment = new SegmentBuilder()
            .withAssessmentKey(assessmentKey)
            .withFieldTestStartPosition(3)
            .withFieldTestEndPosition(7)
            .withFieldTestMinItems(4)
            .withFieldTestMaxItems(4)
            .build();
        Assessment assessment = new AssessmentBuilder()
            .withSegments(Arrays.asList(segment))
            .build();
        // GROUP 1 (3 items)
        Item ftItem1g1 = new ItemBuilder("item-1-group1")
            .withGroupId("group-id-1")
            .withGroupKey("group-key-1")
            .withFieldTest(true)
            .build();
        Item ftItem2g1 = new ItemBuilder("item-2-group1")
            .withGroupId("group-id-1")
            .withGroupKey("group-key-1")
            .withFieldTest(true)
            .build();
        Item ftItem3g1 = new ItemBuilder("item-3-group1")
            .withGroupId("group-id-1")
            .withGroupKey("group-key-1")
            .withFieldTest(true)
            .build();
        // GROUP 2 (single item)
        Item ftItem4g2 = new ItemBuilder("item-4-group2")
            .withGroupId("group-id-2")
            .withGroupKey("group-key-2")
            .withFieldTest(true)
            .build();
        when(mockItemPoolService.getFieldTestItemPool(eq(exam.getId()), eq(assessment.getItemConstraints()), any()))
            .thenReturn(new HashSet<>(Arrays.asList(ftItem1g1, ftItem2g1, ftItem3g1, ftItem4g2)));
        List<FieldTestItemGroup> retFtItemGroups = selector.selectLeastUsedItemGroups(exam, new HashSet<>(),
            assessment, segment, segment.getFieldTestMinItems());
        verify(mockItemPoolService).getFieldTestItemPool(eq(exam.getId()), eq(assessment.getItemConstraints()), any());
        assertThat(retFtItemGroups).hasSize(2);

        FieldTestItemGroup multiItemItemGroup = null;
        FieldTestItemGroup singleItemItemgGroup = null;
        for (FieldTestItemGroup itemGroup : retFtItemGroups) {
            if (itemGroup.getGroupKey().equals(ftItem1g1.getGroupKey())) {
                multiItemItemGroup = itemGroup;
            } else if (itemGroup.getGroupKey().equals(ftItem4g2.getGroupKey())) {
                singleItemItemgGroup = itemGroup;
            }
        }

        assertThat(multiItemItemGroup.getGroupId()).isEqualTo(ftItem1g1.getGroupId());
        assertThat(multiItemItemGroup.getBlockId()).isEqualTo(ftItem1g1.getBlockId());
        assertThat(multiItemItemGroup.getItemCount()).isEqualTo(3);
        assertThat(multiItemItemGroup.getExamId()).isEqualTo(exam.getId());
        assertThat(multiItemItemGroup.getDeletedAt()).isNull();

        assertThat(singleItemItemgGroup.getGroupId()).isEqualTo(ftItem4g2.getGroupId());
        assertThat(singleItemItemgGroup.getBlockId()).isEqualTo(ftItem4g2.getBlockId());
        assertThat(singleItemItemgGroup.getItemCount()).isEqualTo(1);
        assertThat(singleItemItemgGroup.getExamId()).isEqualTo(exam.getId());
        assertThat(singleItemItemgGroup.getDeletedAt()).isNull();
    }
}
