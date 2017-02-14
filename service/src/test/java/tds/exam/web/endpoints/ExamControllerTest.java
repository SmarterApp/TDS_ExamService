package tds.exam.web.endpoints;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.UUID;

import tds.common.Response;
import tds.common.ValidationError;
import tds.common.web.exceptions.NotFoundException;
import tds.common.web.resources.NoContentResponseResource;
import tds.exam.Exam;
import tds.exam.ExamConfiguration;
import tds.exam.ExamStatusCode;
import tds.exam.ExamStatusStage;
import tds.exam.OpenExamRequest;
import tds.exam.builder.ExamBuilder;
import tds.exam.builder.OpenExamRequestBuilder;
import tds.exam.error.ValidationErrorCode;
import tds.exam.services.ExamService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExamControllerTest {
    private ExamController controller;

    @Mock
    private ExamService mockExamService;

    @Before
    public void setUp() {
        HttpServletRequest request = new MockHttpServletRequest();
        ServletRequestAttributes requestAttributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(requestAttributes);

        controller = new ExamController(mockExamService);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void shouldReturnExam() {
        UUID uuid = UUID.randomUUID();
        when(mockExamService.findExam(uuid)).thenReturn(Optional.of(new Exam.Builder().withId(uuid).build()));

        ResponseEntity<Exam> response = controller.getExamById(uuid);
        verify(mockExamService).findExam(uuid);

        assertThat(response.getBody().getId()).isEqualTo(uuid);
    }

    @Test(expected = NotFoundException.class)
    public void shouldReturnNotFoundWhenExamCannotBeFoundById() {
        UUID uuid = UUID.randomUUID();
        when(mockExamService.findExam(uuid)).thenReturn(Optional.empty());
        controller.getExamById(uuid);
    }

    @Test
    public void shouldCreateErrorResponseWhenOpenExamFailsWithValidationError() {
        OpenExamRequest openExamRequest = new OpenExamRequestBuilder().build();
        when(mockExamService.openExam(openExamRequest)).thenReturn(new Response<Exam>(new ValidationError(ValidationErrorCode.PREVIOUS_SESSION_NOT_FOUND, "Session not found")));

        ResponseEntity<Response<Exam>> response = controller.openExam(openExamRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody().hasError()).isTrue();
        assertThat(response.getBody().getError().get().getCode()).isEqualTo(ValidationErrorCode.PREVIOUS_SESSION_NOT_FOUND);
    }

    @Test
    public void shouldOpenExam() throws URISyntaxException {
        OpenExamRequest openExamRequest = new OpenExamRequestBuilder().build();

        UUID examId = UUID.randomUUID();
        when(mockExamService.openExam(openExamRequest)).thenReturn(new Response<>(new Exam.Builder().withId(examId).build()));

        ResponseEntity<Response<Exam>> response = controller.openExam(openExamRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getLocation()).isEqualTo(new URI("http://localhost/exam/" + examId));
    }

    @Test
    public void shouldReturnExamConfiguration() {
        Exam exam = new ExamBuilder().build();
        ExamConfiguration mockExamConfig = new ExamConfiguration.Builder()
            .withExam(exam)
            .withStatus("started")
            .build();
        when(mockExamService.startExam(exam.getId())).thenReturn(
            new Response<>(mockExamConfig));

        ResponseEntity<Response<ExamConfiguration>> response = controller.startExam(exam.getId());
        verify(mockExamService).startExam(exam.getId());

        assertThat(response.getBody().getData().get().getExam().getId()).isEqualTo(exam.getId());
        assertThat(response.getBody().getData().get().getStatus()).isEqualTo("started");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().hasError()).isFalse();
    }

    @Test
    public void shouldCreateErrorResponseWhenStartExamValidationError() {
        final UUID examId = UUID.randomUUID();
        when(mockExamService.startExam(examId)).thenReturn(
            new Response<>(new ValidationError(ValidationErrorCode.EXAM_APPROVAL_SESSION_ID_MISMATCH, "Session mismatch")));

        ResponseEntity<Response<ExamConfiguration>> response = controller.startExam(examId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody().hasError()).isTrue();
        assertThat(response.getBody().getError().get().getCode()).isEqualTo(ValidationErrorCode.EXAM_APPROVAL_SESSION_ID_MISMATCH);
    }

    @Test
    public void shouldPauseAnExam() throws Exception {
        UUID examId = UUID.randomUUID();

        when(mockExamService.updateExamStatus(examId, new ExamStatusCode(ExamStatusCode.STATUS_PAUSED, ExamStatusStage.INACTIVE))).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.pauseExam(examId);

        verify(mockExamService).updateExamStatus(examId, new ExamStatusCode(ExamStatusCode.STATUS_PAUSED, ExamStatusStage.INACTIVE));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getHeaders()).hasSize(1);
        assertThat(response.getHeaders().getLocation()).isEqualTo(new URI(String.format("http://localhost/exam/%s", examId)));
        assertThat(response.getBody()).isNull();
    }

    @Test
    public void shouldNotUpdateAnExamStatus() {
        UUID examId = UUID.randomUUID();

        when(mockExamService.updateExamStatus(examId, new ExamStatusCode(ExamStatusCode.STATUS_PAUSED, ExamStatusStage.INACTIVE)))
            .thenReturn(Optional.of(new ValidationError(ValidationErrorCode.EXAM_STATUS_TRANSITION_FAILURE, "Bad transition from foo to bar")));

        ResponseEntity<NoContentResponseResource> response = controller.pauseExam(examId);

        verify(mockExamService).updateExamStatus(examId, new ExamStatusCode(ExamStatusCode.STATUS_PAUSED, ExamStatusStage.INACTIVE));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody().getErrors()).hasSize(1);

        ValidationError error = response.getBody().getErrors()[0];
        assertThat(error.getCode()).isEqualTo(ValidationErrorCode.EXAM_STATUS_TRANSITION_FAILURE);
        assertThat(error.getMessage()).isEqualTo("Bad transition from foo to bar");
    }

    @Test
    public void shouldPauseAllExamsInASession() {
        UUID sessionId = UUID.randomUUID();
        doNothing().when(mockExamService).pauseAllExamsInSession(sessionId);

        controller.pauseExamsInSession(sessionId);

        verify(mockExamService).pauseAllExamsInSession(sessionId);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowForStatusWithStageNotFound() {
        final UUID examId = UUID.randomUUID();
        final String statusWithNoStage = "foo";
        controller.updateStatus(examId, statusWithNoStage, null, null);
    }
    
    @Test
    public void shouldUpdateStatusWithNoStageProvided() {
        final UUID examId = UUID.randomUUID();
        final String statusCode = ExamStatusCode.STATUS_APPROVED;
        
        when(mockExamService.updateExamStatus(eq(examId), any(), (String) isNull())).thenReturn(Optional.empty());
        ResponseEntity<NoContentResponseResource> response = controller.updateStatus(examId, statusCode, null, null);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
    
    @Test
    public void shouldUpdateStatusWithStageAndReasonProvided() {
        final UUID examId = UUID.randomUUID();
        final String statusCode = ExamStatusCode.STATUS_APPROVED;
        final String stage = ExamStatusStage.OPEN.getType();
        final String reason = "Nausea";
        
        when(mockExamService.updateExamStatus(eq(examId), any(), eq(reason))).thenReturn(Optional.empty());
        ResponseEntity<NoContentResponseResource> response = controller.updateStatus(examId, statusCode, stage, reason);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
    
    @Test
    public void shouldReturn422ForValidationErrors() {
        final UUID examId = UUID.randomUUID();
        final String statusCode = ExamStatusCode.STATUS_APPROVED;
        final String stage = ExamStatusStage.OPEN.getType();
        final String reason = "Puppies";

        when(mockExamService.updateExamStatus(eq(examId), any(), eq(reason))).thenReturn(Optional.of(new ValidationError("Some", "Error")));
        ResponseEntity<NoContentResponseResource> response = controller.updateStatus(examId, statusCode, stage, reason);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
