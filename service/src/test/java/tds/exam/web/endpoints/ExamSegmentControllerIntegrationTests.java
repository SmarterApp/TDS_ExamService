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
import tds.common.Response;
import tds.common.ValidationError;
import tds.common.configuration.JacksonObjectMapperConfiguration;
import tds.common.configuration.SecurityConfiguration;
import tds.common.web.advice.ExceptionAdvice;
import tds.exam.ExamSegment;
import tds.exam.services.ExamSegmentService;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
        when(mockExamSegmentService.findExamSegments(examId, sessionId, browserId)).thenReturn(new Response<>(mockExamSegments));

        http.perform(get(new URI(String.format("/exam/segments/%s", examId)))
            .param("sessionId", sessionId.toString())
            .param("browserId", browserId.toString())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("data[0].segmentPosition", is(1)))
            .andExpect(jsonPath("data[0].segmentKey", is("seg1")))
            .andExpect(jsonPath("data[1].segmentPosition", is(2)))
            .andExpect(jsonPath("data[1].segmentKey", is("seg2")));

        verify(mockExamSegmentService).findExamSegments(examId, sessionId, browserId);
    }

    @Test
    public void shouldReturnErrorResponseForValidationErrorPresent() throws Exception {
        final UUID examId = UUID.randomUUID();
        final UUID sessionId = UUID.randomUUID();
        final UUID browserId = UUID.randomUUID();
        ValidationError error = new ValidationError("ruh", "roh");
        when(mockExamSegmentService.findExamSegments(examId, sessionId, browserId)).thenReturn(new Response<>(error));

        http.perform(get(new URI(String.format("/exam/segments/%s", examId)))
            .param("sessionId", sessionId.toString())
            .param("browserId", browserId.toString())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("error.code", is("ruh")))
            .andExpect(jsonPath("error.message", is("roh")));

        verify(mockExamSegmentService).findExamSegments(examId, sessionId, browserId);
    }

    @Test
    public void shouldReturnNoContentForNoExamSegmentsPresent() throws Exception {
        final UUID examId = UUID.randomUUID();
        final UUID sessionId = UUID.randomUUID();
        final UUID browserId = UUID.randomUUID();
        when(mockExamSegmentService.findExamSegments(examId, sessionId, browserId)).thenReturn(new Response(new ArrayList<>()));

        http.perform(get(new URI(String.format("/exam/segments/%s", examId)))
            .param("sessionId", sessionId.toString())
            .param("browserId", browserId.toString())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent())
            .andExpect(jsonPath("data", Matchers.hasSize(0)));

        verify(mockExamSegmentService).findExamSegments(examId, sessionId, browserId);
    }

}
