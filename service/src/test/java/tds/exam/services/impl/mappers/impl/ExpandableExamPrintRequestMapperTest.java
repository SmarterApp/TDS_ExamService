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

package tds.exam.services.impl.mappers.impl;

import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;
import java.util.Set;

import tds.exam.Exam;
import tds.exam.ExamPrintRequest;
import tds.exam.ExpandableExamPrintRequest;
import tds.exam.builder.ExamBuilder;
import tds.exam.services.ExamService;
import tds.exam.mappers.ExpandableExamPrintRequestMapper;
import tds.exam.mappers.impl.ExamExpandableExamPrintRequestMapper;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExpandableExamPrintRequestMapperTest {
    private ExpandableExamPrintRequestMapper examExpandableExamPrintRequestMapper;

    private final Set<String> expandableAttributes = ImmutableSet.of(
        ExpandableExamPrintRequest.EXPANDABLE_PARAMS_PRINT_REQUEST_WITH_EXAM
    );

    @Mock
    private ExamService mockExamService;

    @Before
    public void setup() {
        examExpandableExamPrintRequestMapper = new ExamExpandableExamPrintRequestMapper(mockExamService);
    }

    @Test
    public void shouldMapExamToExamPrintRequest() {
        ExamPrintRequest request = random(ExamPrintRequest.class);
        ExpandableExamPrintRequest.Builder builder = new ExpandableExamPrintRequest.Builder(request);
        Exam exam = new ExamBuilder().build();

        when(mockExamService.findExam(request.getExamId())).thenReturn(Optional.of(exam));
        examExpandableExamPrintRequestMapper.updateExpandableMapper(expandableAttributes, builder, request.getExamId());

        verify(mockExamService).findExam(request.getExamId());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowIllegalStateExceptionForRequestWithNoExam() {
        ExamPrintRequest request = random(ExamPrintRequest.class);
        ExpandableExamPrintRequest.Builder builder = new ExpandableExamPrintRequest.Builder(request);

        when(mockExamService.findExam(request.getExamId())).thenReturn(Optional.empty());
        examExpandableExamPrintRequestMapper.updateExpandableMapper(expandableAttributes, builder, request.getExamId());
    }

}
