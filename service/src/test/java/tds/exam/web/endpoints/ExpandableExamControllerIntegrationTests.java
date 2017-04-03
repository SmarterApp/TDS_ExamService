package tds.exam.web.endpoints;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import tds.exam.ExamStatusCode;
import tds.exam.ExpandableExam;
import tds.exam.ExpandableExamParameters;
import tds.exam.WebMvcControllerIntegrationTest;
import tds.exam.services.ExpandableExamService;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcControllerIntegrationTest(controllers = ExpandableExamController.class)
public class ExpandableExamControllerIntegrationTests {
    @Autowired
    private MockMvc http;

    @MockBean
    private ExpandableExamService mockExpandableExamService;

    @Test
    public void shouldReturnEmptyForEmptyList() throws Exception {
        final UUID sessionId = UUID.randomUUID();
        final Set<String> invalidStatuses = ImmutableSet.of(ExamStatusCode.STATUS_SUSPENDED);
        when(mockExpandableExamService.findExamsBySessionId(sessionId, invalidStatuses, ExpandableExamParameters.EXPANDABLE_PARAMS_EXAM_ACCOMMODATIONS))
            .thenReturn(new ArrayList<>());

        http.perform(get(new URI(String.format("/exam/session/%s", sessionId)))
            .param("statusNot", ExamStatusCode.STATUS_SUSPENDED)
            .param("expandable", ExpandableExamParameters.EXPANDABLE_PARAMS_EXAM_ACCOMMODATIONS.name())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("[0]").doesNotExist());
    }

    @Test
    public void shouldReturnListOfExpandableExamsForSessionId() throws Exception {
        final UUID sessionId = UUID.randomUUID();
        final Set<String> invalidStatuses = ImmutableSet.of(
            ExamStatusCode.STATUS_SUSPENDED,
            ExamStatusCode.STATUS_PENDING,
            ExamStatusCode.STATUS_DENIED
        );

        final ExpandableExam expandableExam1 = random(ExpandableExam.class);
        final ExpandableExam expandableExam2 = random(ExpandableExam.class);

        when(mockExpandableExamService.findExamsBySessionId(sessionId, invalidStatuses, ExpandableExamParameters.EXPANDABLE_PARAMS_EXAM_ACCOMMODATIONS,
            ExpandableExamParameters.EXPANDABLE_PARAMS_ITEM_RESPONSE_COUNT))
            .thenReturn(Arrays.asList(expandableExam1, expandableExam2));

        http.perform(get(new URI(String.format("/exam/session/%s", sessionId)))
            .param("statusNot", ExamStatusCode.STATUS_SUSPENDED)
            .param("statusNot", ExamStatusCode.STATUS_PENDING)
            .param("statusNot", ExamStatusCode.STATUS_DENIED)
            .param("embed", ExpandableExamParameters.EXPANDABLE_PARAMS_EXAM_ACCOMMODATIONS.name())
            .param("embed", ExpandableExamParameters.EXPANDABLE_PARAMS_ITEM_RESPONSE_COUNT.name())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("[0].exam.id", is(expandableExam1.getExam().getId().toString())))
            .andExpect(jsonPath("[0].itemsResponseCount", is(expandableExam1.getItemsResponseCount())))
            .andExpect(jsonPath("[0].examAccommodations", hasSize(expandableExam1.getExamAccommodations().size())))
            .andExpect(jsonPath("[0].examAccommodations[0].id", is(expandableExam1.getExamAccommodations().get(0).getId().toString())))
            .andExpect(jsonPath("[1].exam.id", is(expandableExam2.getExam().getId().toString())))
            .andExpect(jsonPath("[1].itemsResponseCount", is(expandableExam2.getItemsResponseCount())))
            .andExpect(jsonPath("[1].examAccommodations[0].id", is(expandableExam2.getExamAccommodations().get(0).getId().toString())))
            .andExpect(jsonPath("[1].examAccommodations", hasSize(expandableExam2.getExamAccommodations().size())));
    }

    @Test
    public void shouldReturnSingleExpandableExam() throws Exception {
        final ExpandableExam expandableExam = random(ExpandableExam.class);

        when(mockExpandableExamService.findExam(expandableExam.getExam().getId(), ExpandableExamParameters.EXPANDABLE_PARAMS_EXAM_NOTES,
            ExpandableExamParameters.EXPANDABLE_PARAMS_EXAM_SEGMENTS, ExpandableExamParameters.EXPANDABLE_PARAMS_EXAM_STATUS_DATES))
            .thenReturn(Optional.of(expandableExam));


        http.perform(get(new URI(String.format("/exam/%s/expandable", expandableExam.getExam().getId())))
            .param("embed", ExpandableExamParameters.EXPANDABLE_PARAMS_EXAM_NOTES.name())
            .param("embed", ExpandableExamParameters.EXPANDABLE_PARAMS_EXAM_SEGMENTS.name())
            .param("embed", ExpandableExamParameters.EXPANDABLE_PARAMS_EXAM_STATUS_DATES.name())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.exam.id", is(expandableExam.getExam().getId().toString())))
            .andExpect(jsonPath("$.startedAt", is(expandableExam.getStartedAt().toString())))
            .andExpect(jsonPath("$.examSegments", hasSize(expandableExam.getExamSegments().size())))
            .andExpect(jsonPath("$.examSegments[0].segmentId", is(expandableExam.getExamSegments().get(0).getSegmentId().toString())))
            .andExpect(jsonPath("$.examineeNotes[0].id", is(expandableExam.getExamineeNotes().get(0).getId())));


        verify(mockExpandableExamService).findExam(expandableExam.getExam().getId(), ExpandableExamParameters.EXPANDABLE_PARAMS_EXAM_NOTES,
            ExpandableExamParameters.EXPANDABLE_PARAMS_EXAM_SEGMENTS, ExpandableExamParameters.EXPANDABLE_PARAMS_EXAM_STATUS_DATES);
    }

    @Test
    public void shouldReturn404ForNoExam() throws Exception {
        when(mockExpandableExamService.findExam(any(), any())).thenReturn(Optional.empty());
        http.perform(get(new URI(String.format("/exam/%s/expandable", UUID.randomUUID())))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
        verify(mockExpandableExamService).findExam(any(), any());
    }
}
