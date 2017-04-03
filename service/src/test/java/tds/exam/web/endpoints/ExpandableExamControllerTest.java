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
import tds.exam.ExpandableExamParameters;
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
        ExpandableExamParameters embed = ExpandableExamParameters.EXPANDABLE_PARAMS_EXAM_ACCOMMODATIONS;
        ExpandableExam exam = new ExpandableExam.Builder(mock(Exam.class)).build();
        List<ExpandableExam> expandableExams = Collections.singletonList(exam);
        when(mockExpandableService.findExamsBySessionId(sessionId, statuses, embed)).thenReturn(expandableExams);
        ResponseEntity<List<ExpandableExam>> entity = controller.findExamsForSessionId(sessionId, statuses, embed);

        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(entity.getBody()).isEqualTo(expandableExams);
    }
}