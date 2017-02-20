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
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import tds.common.Response;
import tds.common.ValidationError;
import tds.common.configuration.JacksonObjectMapperConfiguration;
import tds.common.web.advice.ExceptionAdvice;
import tds.exam.Exam;
import tds.exam.ExamSegment;
import tds.exam.ExamStatusCode;
import tds.exam.ExamStatusStage;
import tds.exam.builder.ExamBuilder;
import tds.exam.error.ValidationErrorCode;
import tds.exam.services.ExamPageService;
import tds.exam.services.ExamService;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tds.exam.configuration.SupportApplicationConfiguration.CONFIG_APP_CONTEXT;

@RunWith(SpringRunner.class)
@WebMvcTest(ExamController.class)
@Import({ExceptionAdvice.class, JacksonObjectMapperConfiguration.class})
public class ExamControllerIntegrationTests {
    @Autowired
    private MockMvc http;
    
    @MockBean
    private ExamService mockExamService;
    
    @MockBean
    private ExamPageService mockExamPageService;
    
    @Test
    public void shouldReturnExam() throws Exception {
        UUID examId = UUID.randomUUID();
        Exam exam = new ExamBuilder().withId(examId).build();
        
        when(mockExamService.findExam(examId)).thenReturn(Optional.of(exam));
        
        http.perform(get(new URI(String.format("/exam/%s", examId)))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("id", is(examId.toString())));
        
        verify(mockExamService).findExam(examId);
    }
    
    @Test
    public void shouldReturnNotFoundIfExamCannotBeFound() throws Exception {
        UUID examId = UUID.randomUUID();
        
        when(mockExamService.findExam(examId)).thenReturn(Optional.empty());
        
        http.perform(get(new URI(String.format("/exam/%s", examId)))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
        
        verify(mockExamService).findExam(examId);
    }
    
    @Test
    public void shouldPauseAnExam() throws Exception {
        UUID examId = UUID.randomUUID();
        
        when(mockExamService.updateExamStatus(examId,
            new ExamStatusCode(ExamStatusCode.STATUS_PAUSED, ExamStatusStage.INACTIVE))).thenReturn(Optional.empty());
        
        http.perform(put(new URI(String.format("/exam/%s/pause", examId)))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent())
            .andExpect(header().string("Location", String.format("http://localhost/exam/%s", examId)));
        
        verify(mockExamService).updateExamStatus(examId,
            new ExamStatusCode(ExamStatusCode.STATUS_PAUSED, ExamStatusStage.INACTIVE));
    }
    
    @Test
    public void shouldReturnAnErrorWhenAttemptingToPauseAnExamInAnInvalidTransitionState() throws Exception {
        UUID examId = UUID.randomUUID();
        
        when(mockExamService.updateExamStatus(examId, new ExamStatusCode(ExamStatusCode.STATUS_PAUSED, ExamStatusStage.INACTIVE)))
            .thenReturn(Optional.of(new ValidationError(ValidationErrorCode.EXAM_STATUS_TRANSITION_FAILURE, "Bad transition from foo to bar")));
        
        http.perform(put(new URI(String.format("/exam/%s/pause", examId)))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("errors").isNotEmpty())
            .andExpect(jsonPath("errors").isArray())
            .andExpect(jsonPath("errors[0].code", is("badStatusTransition")))
            .andExpect(jsonPath("errors[0].message", is("Bad transition from foo to bar")));
    }
    
    @Test
    public void shouldPauseAllExamsInASession() throws Exception {
        UUID sessionId = UUID.randomUUID();
        doNothing().when(mockExamService).pauseAllExamsInSession(sessionId);
        
        http.perform(put(new URI(String.format("/exam/pause/%s", sessionId)))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());
        
        verify(mockExamService).pauseAllExamsInSession(sessionId);
    }
    
    @Test
    public void shouldUpdateExamStatus() throws Exception {
        final UUID examId = UUID.randomUUID();
        final String statusCode = ExamStatusCode.STATUS_APPROVED;
        
        when(mockExamService.updateExamStatus(eq(examId), any(), (String) isNull())).thenReturn(Optional.empty());
        
        http.perform(put(new URI(String.format("/exam/%s/status/", examId)))
            .param("status", statusCode)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());
        
        verify(mockExamService).updateExamStatus(eq(examId), any(), (String) isNull());
    }
    
    @Test
    public void shouldFailStatusUpdateWithError() throws Exception {
        final UUID examId = UUID.randomUUID();
        final String statusCode = ExamStatusCode.STATUS_APPROVED;
        
        when(mockExamService.updateExamStatus(eq(examId), any(), (String) isNull()))
            .thenReturn(Optional.of(new ValidationError("Some", "Error")));
        
        http.perform(put(new URI(String.format("/exam/%s/status/", examId)))
            .param("status", statusCode)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity());
        
        verify(mockExamService).updateExamStatus(eq(examId), any(), (String) isNull());
    }
    
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
        when(mockExamService.findExamSegments(examId, sessionId, browserId)).thenReturn(new Response<>(mockExamSegments));
        
        http.perform(get(new URI(String.format("/exam/segments/%s", examId)))
            .param("sessionId", sessionId.toString())
            .param("browserId", browserId.toString())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("data[0].segmentPosition", is(1)))
            .andExpect(jsonPath("data[0].segmentKey", is("seg1")))
            .andExpect(jsonPath("data[1].segmentPosition", is(2)))
            .andExpect(jsonPath("data[1].segmentKey", is("seg2")));
        
        verify(mockExamService).findExamSegments(examId, sessionId, browserId);
    }
    
    @Test
    public void shouldReturnErrorResponseForValidationErrorPresent() throws Exception {
        final UUID examId = UUID.randomUUID();
        final UUID sessionId = UUID.randomUUID();
        final UUID browserId = UUID.randomUUID();
        ValidationError error = new ValidationError("ruh", "roh");
        when(mockExamService.findExamSegments(examId, sessionId, browserId)).thenReturn(new Response<>(error));
        
        http.perform(get(new URI(String.format("/exam/segments/%s", examId)))
            .param("sessionId", sessionId.toString())
            .param("browserId", browserId.toString())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("error.code", is("ruh")))
            .andExpect(jsonPath("error.message", is("roh")));
            
        verify(mockExamService).findExamSegments(examId, sessionId, browserId);
    }
    
    @Test
    public void shouldReturnNoContentForNoExamSegmentsPresent() throws Exception {
        final UUID examId = UUID.randomUUID();
        final UUID sessionId = UUID.randomUUID();
        final UUID browserId = UUID.randomUUID();
        when(mockExamService.findExamSegments(examId, sessionId, browserId)).thenReturn(new Response(new ArrayList<>()));
        
        http.perform(get(new URI(String.format("/exam/segments/%s", examId)))
            .param("sessionId", sessionId.toString())
            .param("browserId", browserId.toString())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent())
            .andExpect(jsonPath("data", Matchers.hasSize(0)));
        
        verify(mockExamService).findExamSegments(examId, sessionId, browserId);
    }
    
    @Test
    public void shouldThrowWithNoStatusProvided() throws Exception {
        final UUID examId = UUID.randomUUID();
        
        http.perform(put(new URI(String.format("/exam/%s/status/", examId)))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError());
        
    }
}
