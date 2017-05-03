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
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import tds.common.Response;
import tds.common.ValidationError;
import tds.common.web.exceptions.NotFoundException;
import tds.common.web.resources.NoContentResponseResource;
import tds.exam.ApproveAccommodationsRequest;
import tds.exam.Exam;
import tds.exam.ExamConfiguration;
import tds.exam.ExamStatusCode;
import tds.exam.ExamStatusRequest;
import tds.exam.ExamStatusStage;
import tds.exam.OpenExamRequest;
import tds.exam.builder.ExamBuilder;
import tds.exam.builder.OpenExamRequestBuilder;
import tds.exam.error.ValidationErrorCode;
import tds.exam.services.ExamService;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
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
        final String browserUserAgent = "007";
        Exam exam = new ExamBuilder().build();
        ExamConfiguration mockExamConfig = new ExamConfiguration.Builder()
            .withExam(exam)
            .withStatus("started")
            .build();
        when(mockExamService.startExam(exam.getId(), browserUserAgent)).thenReturn(
            new Response<>(mockExamConfig));

        ResponseEntity<Response<ExamConfiguration>> response = controller.startExam(exam.getId(), browserUserAgent);
        verify(mockExamService).startExam(exam.getId(), browserUserAgent);

        assertThat(response.getBody().getData().get().getExam().getId()).isEqualTo(exam.getId());
        assertThat(response.getBody().getData().get().getStatus()).isEqualTo("started");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().hasError()).isFalse();
    }

    @Test
    public void shouldCreateErrorResponseWhenStartExamValidationError() {
        final String browserUserAgent = "007";
        final UUID examId = UUID.randomUUID();
        when(mockExamService.startExam(examId, browserUserAgent)).thenReturn(
            new Response<>(new ValidationError(ValidationErrorCode.EXAM_APPROVAL_SESSION_ID_MISMATCH, "Session mismatch")));

        ResponseEntity<Response<ExamConfiguration>> response = controller.startExam(examId, browserUserAgent);

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


    @Test
    public void shouldUpdateStatusWithStageAndReasonProvided() {
        final UUID examId = UUID.randomUUID();
        final ExamStatusRequest request = random(ExamStatusRequest.class);

        when(mockExamService.updateExamStatus(eq(examId), any(), eq(request.getReason()))).thenReturn(Optional.empty());
        ResponseEntity<NoContentResponseResource> response = controller.updateStatus(examId, request);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    public void shouldReturn422ForValidationErrors() {
        final UUID examId = UUID.randomUUID();
        final ExamStatusRequest request = random(ExamStatusRequest.class);

        when(mockExamService.updateExamStatus(eq(examId), any(), eq(request.getReason()))).thenReturn(Optional.of(new ValidationError("Some", "Error")));
        ResponseEntity<NoContentResponseResource> response = controller.updateStatus(examId, request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    public void shouldApproveAccommodationsAndReturnNoErrors() {
        final UUID examId = UUID.randomUUID();
        final UUID sessionId = UUID.randomUUID();
        final UUID browserId = UUID.randomUUID();
        ApproveAccommodationsRequest request = new ApproveAccommodationsRequest(sessionId, browserId, new HashMap<>());

        when(mockExamService.updateExamAccommodationsAndExam(examId, request)).thenReturn(Optional.empty());

        ResponseEntity<NoContentResponseResource> response = controller.approveAccommodations(examId, request);

        verify(mockExamService).updateExamAccommodationsAndExam(examId, request);
        verifyNoMoreInteractions(mockExamService);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    public void shouldReturnValidationError() {
        final UUID examId = UUID.randomUUID();
        final UUID sessionId = UUID.randomUUID();
        final UUID browserId = UUID.randomUUID();
        final String errCode = "Error code";
        final String errMsg = "Error message";
        ApproveAccommodationsRequest request = new ApproveAccommodationsRequest(sessionId, browserId, new HashMap<>());

        when(mockExamService.updateExamAccommodationsAndExam(examId, request)).thenReturn(Optional.of(new ValidationError(errCode, errMsg)));

        ResponseEntity<NoContentResponseResource> response = controller.approveAccommodations(examId, request);

        verify(mockExamService).updateExamAccommodationsAndExam(examId, request);
        verifyNoMoreInteractions(mockExamService);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody().getErrors()).isNotEmpty();
    }
}
