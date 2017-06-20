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

import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import tds.exam.Exam;
import tds.exam.ExamStatusCode;
import tds.exam.ExpandableExam;
import tds.exam.ExpandableExamAttributes;
import tds.exam.services.ExpandableExamService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExpandableExamControllerTest {
    private ExpandableExamController controller;

    @Mock
    private ExpandableExamService mockExpandableService;

    @Before
    public void setUp() {
        controller = new ExpandableExamController(mockExpandableService);
    }

    @Test
    public void shouldFindExpandableExamsForSession() {
        UUID sessionId = UUID.randomUUID();
        Set<String> statuses = Sets.newHashSet(ExamStatusCode.STATUS_APPROVED);
        ExpandableExamAttributes expandableAttributes = ExpandableExamAttributes.EXAM_ACCOMMODATIONS;
        ExpandableExam exam = new ExpandableExam.Builder(mock(Exam.class)).build();
        List<ExpandableExam> expandableExams = Collections.singletonList(exam);
        when(mockExpandableService.findExamsBySessionId(sessionId, statuses, expandableAttributes)).thenReturn(expandableExams);
        ResponseEntity<List<ExpandableExam>> entity = controller.findExamsForSessionId(sessionId, statuses, expandableAttributes);

        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(entity.getBody()).isEqualTo(expandableExams);
    }
}