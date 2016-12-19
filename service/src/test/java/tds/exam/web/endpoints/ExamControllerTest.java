package tds.exam.web.endpoints;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
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
import tds.exam.ApprovalRequest;
import tds.exam.Exam;
import tds.exam.ExamApproval;
import tds.exam.ExamApprovalStatus;
import tds.exam.ExamStatusCode;
import tds.exam.ExamStatusStage;
import tds.exam.OpenExamRequest;
import tds.exam.builder.OpenExamRequestBuilder;
import tds.exam.error.ValidationErrorCode;
import tds.exam.services.ExamService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static tds.exam.ExamStatusCode.STATUS_APPROVED;

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
        when(mockExamService.openExam(openExamRequest)).thenReturn(new Response<Exam>(new ValidationError(ValidationErrorCode.SESSION_TYPE_MISMATCH, "Session mismatch")));

        ResponseEntity<Response<Exam>> response = controller.openExam(openExamRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody().getErrors().get()).hasSize(1);
        assertThat(response.getBody().getErrors().get()[0].getCode()).isEqualTo(ValidationErrorCode.SESSION_TYPE_MISMATCH);
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
    public void shouldGetAnExamApprovalRequestThatIsApproved() {
        UUID examId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        UUID browserId = UUID.randomUUID();
        String clientName = "UNIT_TEST";
        ApprovalRequest approvalRequest = new ApprovalRequest(examId, sessionId, browserId, clientName);

        ExamApproval mockExamApproval = new ExamApproval(examId, new ExamStatusCode(STATUS_APPROVED, ExamStatusStage.OPEN), null);
        when(mockExamService.getApproval(Matchers.isA(ApprovalRequest.class))).thenReturn(new Response<>(mockExamApproval));

        ResponseEntity<Response<ExamApproval>> response = controller.getApproval(
            approvalRequest.getExamId(),
            approvalRequest.getSessionId(),
            approvalRequest.getBrowserId(),
            approvalRequest.getClientName());
        verify(mockExamService).getApproval(Matchers.isA(ApprovalRequest.class));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getErrors()).isNotPresent();
        assertThat(response.getBody().getData()).isPresent();
        assertThat(response.getBody().getData().get().getExamApprovalStatus()).isEqualTo(ExamApprovalStatus.APPROVED);
    }

    @Test
    public void shouldGetAValidationErrorWhenBrowserIdsDoNotMatch() {
        UUID examId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        UUID browserId = UUID.randomUUID();
        String clientName = "UNIT_TEST";
        ApprovalRequest approvalRequest = new ApprovalRequest(examId, sessionId, browserId, clientName);

        Response<ExamApproval> errorResponse = new Response<ExamApproval>(new ValidationError(ValidationErrorCode.EXAM_APPROVAL_BROWSER_ID_MISMATCH, "foo"));
        when(mockExamService.getApproval(Matchers.isA(ApprovalRequest.class))).thenReturn(errorResponse);

        ResponseEntity<Response<ExamApproval>> response = controller.getApproval(
            approvalRequest.getExamId(),
            approvalRequest.getSessionId(),
            approvalRequest.getBrowserId(),
            approvalRequest.getClientName());
        verify(mockExamService).getApproval(Matchers.isA(ApprovalRequest.class));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody().getErrors()).isPresent();
        assertThat(response.getBody().getErrors().get()).hasSize(1);
        assertThat(response.getBody().getErrors().get()[0].getCode()).isEqualTo(ValidationErrorCode.EXAM_APPROVAL_BROWSER_ID_MISMATCH);
        assertThat(response.getBody().getErrors().get()[0].getMessage()).isEqualTo("foo");
        assertThat(response.getBody().getData()).isNotPresent();
    }
}
