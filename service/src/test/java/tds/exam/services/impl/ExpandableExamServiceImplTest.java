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

import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import tds.exam.Exam;
import tds.exam.ExamStatusCode;
import tds.exam.ExpandableExam;
import tds.exam.ExpandableExamAttributes;
import tds.exam.builder.ExamBuilder;
import tds.exam.mappers.ExpandableExamMapper;
import tds.exam.repositories.ExamQueryRepository;
import tds.exam.services.ExpandableExamService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExpandableExamServiceImplTest {
    private ExpandableExamService expandableExamService;
    private Collection<ExpandableExamMapper> mockExamMappers;

    @Mock
    private ExamQueryRepository mockExamQueryRepository;

    @Before
    public void setUp() {
        mockExamMappers = Arrays.asList(mock(ExpandableExamMapper.class), mock(ExpandableExamMapper.class));

        expandableExamService = new ExpandableExamServiceImpl(mockExamMappers, mockExamQueryRepository);
    }

    @Test
    public void shouldReturnExpandableExams() {
        UUID sessionId = UUID.randomUUID();
        Exam exam1 = new ExamBuilder().build();
        Exam exam2 = new ExamBuilder().build();
        final Set<String> invalidStatuses = Sets.newHashSet(
            ExamStatusCode.STATUS_PENDING,
            ExamStatusCode.STATUS_SUSPENDED,
            ExamStatusCode.STATUS_DENIED
        );

        when(mockExamQueryRepository.findAllExamsInSessionWithoutStatus(eq(sessionId), any())).thenReturn(Arrays.asList(exam1, exam2));

        List<ExpandableExam> expandableExams = expandableExamService.findExamsBySessionId(sessionId, invalidStatuses,
            ExpandableExamAttributes.EXAM_ACCOMMODATIONS);

        verify(mockExamQueryRepository).findAllExamsInSessionWithoutStatus(eq(sessionId), any());
        mockExamMappers.forEach(mockMapper -> verify(mockMapper).updateExpandableMapper(any(), any(), any()));

        assertThat(expandableExams).hasSize(2);

        ExpandableExam expExam1 = null;
        ExpandableExam expExam2 = null;

        for (ExpandableExam expandableExam : expandableExams) {
            if (expandableExam.getExam().getId().equals(exam1.getId())) {
                expExam1 = expandableExam;
            } else if (expandableExam.getExam().getId().equals(exam2.getId())) {
                expExam2 = expandableExam;
            }
        }

        assertThat(expExam1.getExam()).isEqualTo(exam1);
        assertThat(expExam2.getExam()).isEqualTo(exam2);
    }

    @Test
    public void shouldReturnSingleExpandableExam() {
        Exam exam = new ExamBuilder().build();
        when(mockExamQueryRepository.getExamById(exam.getId())).thenReturn(Optional.of(exam));
        Optional<ExpandableExam> maybeExpandableExam = expandableExamService.findExam(exam.getId(), ExpandableExamAttributes.EXAM_NOTES);
        verify(mockExamQueryRepository).getExamById(exam.getId());
        mockExamMappers.forEach(mockMapper -> verify(mockMapper).updateExpandableMapper(any(), any(), any()));

        assertThat(maybeExpandableExam.get().getExam()).isEqualTo(exam);
    }
}