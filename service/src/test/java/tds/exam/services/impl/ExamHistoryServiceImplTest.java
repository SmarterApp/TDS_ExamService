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

import java.util.Collections;
import java.util.HashSet;
import java.util.UUID;

import tds.exam.models.ItemGroupHistory;
import tds.exam.repositories.HistoryQueryRepository;
import tds.exam.services.ExamHistoryService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExamHistoryServiceImplTest {
    @Mock
    private HistoryQueryRepository mockHistoryQueryRepository;

    private ExamHistoryService examHistoryService;

    @Before
    public void setUp() {
        examHistoryService = new ExamHistoryServiceImpl(mockHistoryQueryRepository);
    }

    @Test
    public void shouldFindItemGroupHistories() {
        ItemGroupHistory history = new ItemGroupHistory(UUID.randomUUID(), new HashSet<>());

        UUID excludedExamId = UUID.randomUUID();
        long studentId = 1;
        String assessmentId = "ELA 3";

        when(mockHistoryQueryRepository.findPreviousItemGroups(studentId, excludedExamId, assessmentId)).thenReturn(Collections.singletonList(history));

        assertThat(examHistoryService.findPreviousItemGroups(studentId, excludedExamId, assessmentId)).containsExactly(history);

        verify(mockHistoryQueryRepository).findPreviousItemGroups(studentId, excludedExamId, assessmentId);
    }
}