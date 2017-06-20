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
import java.util.Optional;
import java.util.UUID;

import tds.exam.repositories.ExamPageWrapperQueryRepository;
import tds.exam.services.ExamPageWrapperService;
import tds.exam.wrapper.ExamPageWrapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExamPageWrapperServiceImplTest {
    @Mock
    private ExamPageWrapperQueryRepository mockExamPageWrapperQueryRepository;

    private ExamPageWrapperService examPageWrapperService;

    @Before
    public void setUp() {
        examPageWrapperService = new ExamPageWrapperServiceImpl(mockExamPageWrapperQueryRepository);
    }

    @Test
    public void findExamPageByExamId() {
        ExamPageWrapper examPageWrapper = mock(ExamPageWrapper.class);
        UUID examId = UUID.randomUUID();

        when(mockExamPageWrapperQueryRepository.findPagesWithItems(examId)).thenReturn(Collections.singletonList(examPageWrapper));

        assertThat(examPageWrapperService.findPagesWithItems(examId)).containsExactly(examPageWrapper);

        verify(mockExamPageWrapperQueryRepository).findPagesWithItems(examId);
    }

    @Test
    public void findExamPageByExamIdAndPagePosition() {
        ExamPageWrapper examPageWrapper = mock(ExamPageWrapper.class);
        UUID examId = UUID.randomUUID();

        when(mockExamPageWrapperQueryRepository.findPageWithItems(examId, 1)).thenReturn(Optional.of(examPageWrapper));

        assertThat(examPageWrapperService.findPageWithItems(examId, 1).get()).isEqualTo(examPageWrapper);

        verify(mockExamPageWrapperQueryRepository).findPageWithItems(examId, 1);
    }

    @Test
    public void findExamPageByExamIdAndSegmentKey() {
        ExamPageWrapper examPageWrapper = mock(ExamPageWrapper.class);
        UUID examId = UUID.randomUUID();

        when(mockExamPageWrapperQueryRepository.findPagesForExamSegment(examId, "segmentKey")).thenReturn(Collections.singletonList(examPageWrapper));

        assertThat(examPageWrapperService.findPagesForExamSegment(examId, "segmentKey")).containsExactly(examPageWrapper);

        verify(mockExamPageWrapperQueryRepository).findPagesForExamSegment(examId, "segmentKey");
    }
}