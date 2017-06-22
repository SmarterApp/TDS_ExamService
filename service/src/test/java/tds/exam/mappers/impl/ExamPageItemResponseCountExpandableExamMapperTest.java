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

package tds.exam.mappers.impl;

import com.google.common.collect.Sets;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import tds.exam.Exam;
import tds.exam.ExamItem;
import tds.exam.ExamPage;
import tds.exam.ExpandableExam;
import tds.exam.ExpandableExamAttributes;
import tds.exam.builder.ExamItemBuilder;
import tds.exam.builder.ExamPageBuilder;
import tds.exam.services.ExamItemService;
import tds.exam.services.ExamPageService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExamPageItemResponseCountExpandableExamMapperTest {
    @Mock
    private ExamPageService mockExamPageService;

    @Mock
    private ExamItemService mockExamItemService;

    private ExamPageItemResponseExpandableExamMapper mapper;

    @Before
    public void setUp() {
        mapper = new ExamPageItemResponseExpandableExamMapper(mockExamPageService, mockExamItemService);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void shouldMapPagesAndItems() {
        UUID examId = UUID.randomUUID();
        ExamPage examPage = new ExamPageBuilder().build();
        ExamItem examItem = new ExamItemBuilder().build();

        when(mockExamPageService.findAllPages(examId)).thenReturn(Collections.singletonList(examPage));
        when(mockExamItemService.findExamItemAndResponses(examId)).thenReturn(Collections.singletonList(examItem));

        Set<ExpandableExamAttributes> attributes = Sets.newHashSet(ExpandableExamAttributes.EXAM_PAGE_AND_ITEMS);

        ExpandableExam.Builder builder = new ExpandableExam.Builder(new Exam.Builder().withId(examId).build());
        Map<UUID, ExpandableExam.Builder> builders = new HashMap<>();
        builders.put(examId, builder);

        mapper.updateExpandableMapper(attributes, builders, null);

        verify(mockExamPageService).findAllPages(examId);
        verify(mockExamItemService).findExamItemAndResponses(examId);

        ExpandableExam expandableExam = builders.get(examId).build();

        assertThat(expandableExam.getExamPages()).containsExactly(examPage);
        assertThat(expandableExam.getExamItems()).containsExactly(examItem);
    }
}