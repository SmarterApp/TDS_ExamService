package tds.exam.web.endpoints;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import tds.common.ValidationError;
import tds.exam.Exam;
import tds.exam.ExamStatusCode;
import tds.exam.ExamStatusStage;
import tds.exam.SegmentApprovalRequest;
import tds.exam.WebMvcControllerIntegrationTest;
import tds.exam.builder.ExamBuilder;
import tds.exam.error.ValidationErrorCode;
import tds.exam.services.ExamApprovalService;
import tds.exam.services.ExamPageService;
import tds.exam.services.ExamService;
import tds.exam.web.interceptors.VerifyAccessInterceptor;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
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

@RunWith(SpringRunner.class)
@WebMvcControllerIntegrationTest(controllers = ExamController.class)
public class ExamControllerIntegrationTests {

    @Autowired
    private MockMvc http;

    @MockBean
    private ExamService mockExamService;

    @MockBean
    private ExamPageService mockExamPageService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VerifyAccessInterceptor mockVerifyAccessInterceptor;

    @MockBean
    private ExamApprovalService mockExamApprovalService;

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
    public void shouldThrowWithNoStatusProvided() throws Exception {
        final UUID examId = UUID.randomUUID();

        http.perform(put(new URI(String.format("/exam/%s/status/", examId)))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError());

    }

    @Test
    public void shouldWaitForSegmentSuccessfully() throws Exception {
        UUID examId = UUID.randomUUID();
        SegmentApprovalRequest request = random(SegmentApprovalRequest.class);
        when(mockExamService.waitForSegmentApproval(eq(examId), any())).thenReturn(Optional.empty());

        ObjectWriter ow = objectMapper
            .writer().withDefaultPrettyPrinter();

        http.perform(put(new URI(String.format("/exam/%s/segmentApproval/", examId)))
            .contentType(MediaType.APPLICATION_JSON)
            .content(ow.writeValueAsString(request)))
            .andExpect(header().string("Location", String.format("http://localhost/exam/%s", examId)))
            .andExpect(status().isNoContent());

        verify(mockExamService).waitForSegmentApproval(eq(examId), any());
    }

    @Test
    public void shouldReturnErrorWaitForSegment() throws Exception {
        UUID examId = UUID.randomUUID();
        SegmentApprovalRequest request = random(SegmentApprovalRequest.class);
        when(mockExamService.waitForSegmentApproval(eq(examId), any())).thenReturn(Optional.of(new ValidationError("some", "error")));

        ObjectWriter ow = objectMapper
            .writer().withDefaultPrettyPrinter();

        http.perform(put(new URI(String.format("/exam/%s/segmentApproval/", examId)))
            .contentType(MediaType.APPLICATION_JSON)
            .content(ow.writeValueAsString(request)))
            .andExpect(status().isUnprocessableEntity());


        verify(mockExamService).waitForSegmentApproval(eq(examId), any());
    }
}
