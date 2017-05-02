package tds.exam.web.endpoints;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import tds.common.ValidationError;
import tds.exam.ExamItem;
import tds.exam.ExamSegment;
import tds.exam.WebMvcControllerIntegrationTest;
import tds.exam.builder.ExamItemBuilder;
import tds.exam.builder.ExamPageBuilder;
import tds.exam.builder.ExamSegmentBuilder;
import tds.exam.services.ExamSegmentService;
import tds.exam.wrapper.ExamPageWrapper;
import tds.exam.wrapper.ExamSegmentWrapper;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcControllerIntegrationTest(controllers = ExamSegmentController.class)
public class ExamSegmentControllerIntegrationTests {
    @Autowired
    private MockMvc http;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ExamSegmentService mockExamSegmentService;

    @Test
    public void shouldReturnExamSegments() throws Exception {
        final UUID examId = UUID.randomUUID();
        final UUID sessionId = UUID.randomUUID();
        final UUID browserId = UUID.randomUUID();
        ExamSegment seg1 = new ExamSegment.Builder()
            .withSegmentKey("seg1")
            .withExamId(examId)
            .withSegmentPosition(1)
            .build();
        ExamSegment seg2 = new ExamSegment.Builder()
            .withSegmentKey("seg2")
            .withExamId(examId)
            .withSegmentPosition(2)
            .build();
        List<ExamSegment> mockExamSegments = Arrays.asList(seg1, seg2);
        when(mockExamSegmentService.findExamSegments(examId)).thenReturn(mockExamSegments);

        http.perform(get(new URI(String.format("/exam/%s/segments", examId)))
            .param("sessionId", sessionId.toString())
            .param("browserId", browserId.toString())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("[0].segmentPosition", is(1)))
            .andExpect(jsonPath("[0].segmentKey", is("seg1")))
            .andExpect(jsonPath("[1].segmentPosition", is(2)))
            .andExpect(jsonPath("[1].segmentKey", is("seg2")));

        verify(mockExamSegmentService).findExamSegments(examId);
    }

    @Test
    public void shouldReturnEmptyListForNoExamSegmentsPresent() throws Exception {
        final UUID examId = UUID.randomUUID();
        final UUID sessionId = UUID.randomUUID();
        final UUID browserId = UUID.randomUUID();
        when(mockExamSegmentService.findExamSegments(examId)).thenReturn(new ArrayList<>());

        http.perform(get(new URI(String.format("/exam/%s/segments", examId)))
            .param("sessionId", sessionId.toString())
            .param("browserId", browserId.toString())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", Matchers.hasSize(0)));

        verify(mockExamSegmentService).findExamSegments(examId);
    }

    @Test
    public void shouldExitExamSegmentSuccessfully() throws Exception {
        final UUID examId = UUID.randomUUID();
        final int segmentPosition = 1;
        when(mockExamSegmentService.exitSegment(examId, segmentPosition)).thenReturn(Optional.empty());

        http.perform(put(new URI(String.format("/exam/%s/segments/%d/exit", examId, segmentPosition)))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        verify(mockExamSegmentService).exitSegment(examId, segmentPosition);
    }

    @Test
    public void shouldFailToExitExamWithValidationError() throws Exception {
        final UUID examId = UUID.randomUUID();
        final int segmentPosition = 1;
        when(mockExamSegmentService.exitSegment(examId, segmentPosition))
            .thenReturn(Optional.of(new ValidationError("waffles", "burritos")));

        http.perform(put(new URI(String.format("/exam/%s/segments/%d/exit", examId, segmentPosition)))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity());

        verify(mockExamSegmentService).exitSegment(examId, segmentPosition);
    }

    @Test
    public void shouldCheckIfSegmentsAreSatisfied() throws Exception {
        final UUID examId = UUID.randomUUID();
        when(mockExamSegmentService.checkIfSegmentsCompleted(examId)).thenReturn(true);

        http.perform(get(new URI(String.format("/exam/%s/segments/completed", examId)))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", is(true)));
    }

    @Test
    public void shouldFindExamSegmentWrappersForExam() throws Exception {
        final UUID examId = UUID.randomUUID();

        ExamItem examItem = new ExamItemBuilder().build();

        ExamSegment examSegment = new ExamSegmentBuilder()
            .withExamId(examId)
            .withSegmentKey("segmentKey")
            .withSegmentPosition(1)
            .build();

        ExamPageWrapper examPageWrapper = new ExamPageWrapper(new ExamPageBuilder()
            .withExamId(examId)
            .withSegmentKey("segmentKey")
            .withPagePosition(1)
            .build(),
            singletonList(examItem));

        when(mockExamSegmentService.findAllExamSegments(examId)).thenReturn(singletonList(new ExamSegmentWrapper(examSegment, singletonList(examPageWrapper))));

        MvcResult result = http.perform(get(new URI(String.format("/exam/%s/segmentWrappers", examId)))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("[0].examSegment.segmentKey", is("segmentKey")))
            .andExpect(jsonPath("[0].examSegment.segmentPosition", is(1)))
            .andExpect(jsonPath("[0].examPages").isArray())
            .andExpect(jsonPath("[0].examPages[0].examPage.segmentKey", is("segmentKey")))
            .andExpect(jsonPath("[0].examPages[0].examItems[0].id", is(examItem.getId().toString())))
            .andReturn();
        String json = result.getResponse().getContentAsString();

        JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, ExamSegmentWrapper.class);
        List<ExamSegmentWrapper> wrappers = objectMapper.readValue(json, type);

        assertThat(wrappers).hasSize(1);

        ExamSegmentWrapper wrapper = wrappers.get(0);

        assertThat(wrapper.getExamPages()).hasSize(1);
        assertThat(wrapper.getExamSegment()).isNotNull();
        assertThat(wrapper.getExamPages().get(0).getExamItems()).hasSize(1);
    }

    @Test
    public void shouldFindSingleExamSegmentWrapperForExamAndSegmentPosition() throws Exception {
        final UUID examId = UUID.randomUUID();

        ExamItem examItem = new ExamItemBuilder().build();

        ExamSegment examSegment = new ExamSegmentBuilder()
            .withExamId(examId)
            .withSegmentKey("segmentKey")
            .withSegmentPosition(1)
            .build();

        ExamPageWrapper examPageWrapper = new ExamPageWrapper(new ExamPageBuilder()
            .withExamId(examId)
            .withSegmentKey("segmentKey")
            .withPagePosition(1)
            .build(),
            singletonList(examItem));

        ExamPageWrapper examPageWrapper2 = new ExamPageWrapper(new ExamPageBuilder()
            .withExamId(examId)
            .withSegmentKey("segmentKey")
            .withPagePosition(1)
            .build(),
            singletonList(examItem));

        when(mockExamSegmentService.findExamSegment(examId, 1)).thenReturn(Optional.of(new ExamSegmentWrapper(examSegment, Arrays.asList(examPageWrapper, examPageWrapper2))));

        MvcResult result = http.perform(get(new URI(String.format("/exam/%s/segmentWrappers/1", examId)))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.examSegment.segmentKey", is("segmentKey")))
            .andExpect(jsonPath("$.examSegment.segmentPosition", is(1)))
            .andExpect(jsonPath("$.examPages").isArray())
            .andExpect(jsonPath("$.examPages[0].examPage.segmentKey", is("segmentKey")))
            .andExpect(jsonPath("$.examPages[0].examItems[0].id", is(examItem.getId().toString())))
            .andReturn();

        String json = result.getResponse().getContentAsString();
        ExamSegmentWrapper wrapper = objectMapper.readValue(json, ExamSegmentWrapper.class);

        assertThat(wrapper.getExamPages()).hasSize(2);
        assertThat(wrapper.getExamSegment()).isNotNull();
        assertThat(wrapper.getExamPages().get(0).getExamItems()).hasSize(1);
    }

    @Test
    public void shouldFindSingleExamSegmentWrapperForExamAndSegmentAndPagePosition() throws Exception {
        final UUID examId = UUID.randomUUID();

        ExamItem examItem = new ExamItemBuilder().build();

        ExamSegment examSegment = new ExamSegmentBuilder()
            .withExamId(examId)
            .withSegmentKey("segmentKey")
            .withSegmentPosition(1)
            .build();

        ExamPageWrapper examPageWrapper = new ExamPageWrapper(new ExamPageBuilder()
            .withExamId(examId)
            .withSegmentKey("segmentKey")
            .withPagePosition(1)
            .build(),
            singletonList(examItem));

        when(mockExamSegmentService.findExamSegmentWithPageAtPosition(examId, 1, 2)).thenReturn(Optional.of(new ExamSegmentWrapper(examSegment, singletonList(examPageWrapper))));

        MvcResult result = http.perform(get(new URI(String.format("/exam/%s/segmentWrappers/1?pagePosition=2", examId)))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.examSegment.segmentKey", is("segmentKey")))
            .andExpect(jsonPath("$.examSegment.segmentPosition", is(1)))
            .andExpect(jsonPath("$.examPages").isArray())
            .andExpect(jsonPath("$.examPages[0].examPage.segmentKey", is("segmentKey")))
            .andExpect(jsonPath("$.examPages[0].examItems[0].id", is(examItem.getId().toString())))
            .andReturn();

        String json = result.getResponse().getContentAsString();
        ExamSegmentWrapper wrapper = objectMapper.readValue(json, ExamSegmentWrapper.class);

        assertThat(wrapper.getExamPages()).hasSize(1);
        assertThat(wrapper.getExamSegment()).isNotNull();
        assertThat(wrapper.getExamPages().get(0).getExamItems()).hasSize(1);
    }

    @Test
    public void shouldReturnNotFoundWhenCannotFindExamSegmentWrappersForExam() throws Exception {
        UUID examId = UUID.randomUUID();
        when(mockExamSegmentService.findExamSegments(examId)).thenReturn(Collections.emptyList());

        http.perform(get(new URI(String.format("/exam/%s/segmentWrappers", examId)))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnNotFoundWhenCannotFindSingleExamSegmentWrapperForExamAndSegmentPosition() throws Exception {
        UUID examId = UUID.randomUUID();
        when(mockExamSegmentService.findExamSegment(examId, 1)).thenReturn(Optional.empty());

        http.perform(get(new URI(String.format("/exam/%s/segmentWrappers/1", examId)))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnNotFoundWhenCannotFindSingleExamSegmentWrapperForExamAndSegmentAndPagePosition() throws Exception {
        UUID examId = UUID.randomUUID();
        when(mockExamSegmentService.findExamSegmentWithPageAtPosition(examId, 1, 2)).thenReturn(Optional.empty());

        http.perform(get(new URI(String.format("/exam/%s/segmentWrappers/1?pagePosition=2", examId)))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }
}
