package tds.exam.web.endpoints;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import tds.common.Response;
import tds.common.ValidationError;
import tds.common.configuration.JacksonObjectMapperConfiguration;
import tds.common.configuration.SecurityConfiguration;
import tds.common.web.advice.ExceptionAdvice;
import tds.exam.ExamSegment;
import tds.exam.services.ExamSegmentService;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(ExamSegmentController.class)
@Import({ExceptionAdvice.class, JacksonObjectMapperConfiguration.class, SecurityConfiguration.class})
public class ExamSegmentControllerIntegrationTests {
    @Autowired
    private MockMvc http;

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

        http.perform(get(new URI(String.format("/exam/segments/%s", examId)))
            .param("sessionId", sessionId.toString())
            .param("browserId", browserId.toString())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("data[0].segmentPosition", is(1)))
            .andExpect(jsonPath("data[0].segmentKey", is("seg1")))
            .andExpect(jsonPath("data[1].segmentPosition", is(2)))
            .andExpect(jsonPath("data[1].segmentKey", is("seg2")));

        verify(mockExamSegmentService).findExamSegments(examId);
    }

    @Test
    public void shouldReturnErrorResponseForValidationErrorPresent() throws Exception {
        final UUID examId = UUID.randomUUID();
        final UUID sessionId = UUID.randomUUID();
        final UUID browserId = UUID.randomUUID();
        ValidationError error = new ValidationError("ruh", "roh");


        http.perform(get(new URI(String.format("/exam/segments/%s", examId)))
            .param("sessionId", sessionId.toString())
            .param("browserId", browserId.toString())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("error.code", is("ruh")))
            .andExpect(jsonPath("error.message", is("roh")));

        verifyZeroInteractions(mockExamSegmentService.findExamSegments(examId));
    }

    @Test
    public void shouldReturnNoContentForNoExamSegmentsPresent() throws Exception {
        final UUID examId = UUID.randomUUID();
        final UUID sessionId = UUID.randomUUID();
        final UUID browserId = UUID.randomUUID();
        when(mockExamSegmentService.findExamSegments(examId)).thenReturn(new ArrayList<>());

        http.perform(get(new URI(String.format("/exam/segments/%s", examId)))
            .param("sessionId", sessionId.toString())
            .param("browserId", browserId.toString())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent())
            .andExpect(jsonPath("data", Matchers.hasSize(0)));

        verify(mockExamSegmentService).findExamSegments(examId);
    }

    @Test
    public void shouldExitExamSegmentSuccessfully() throws Exception {
        final UUID examId = UUID.randomUUID();
        final int segmentPosition = 1;
        when(mockExamSegmentService.exitSegment(examId, segmentPosition)).thenReturn(Optional.empty());

        http.perform(put(new URI(String.format("/exam/segments/%s/exit/%d", examId, segmentPosition)))
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

        http.perform(put(new URI(String.format("/exam/segments/%s/exit/%d", examId, segmentPosition)))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity());

        verify(mockExamSegmentService).exitSegment(examId, segmentPosition);
    }
}
