package tds.exam.web.endpoints;

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
import java.util.UUID;

import tds.common.Response;
import tds.common.ValidationError;
import tds.exam.ExamApproval;
import tds.exam.ExamApprovalStatus;
import tds.exam.ExamInfo;
import tds.exam.ExamStatusCode;
import tds.exam.ExamStatusStage;
import tds.exam.error.ValidationErrorCode;
import tds.exam.services.ExamApprovalService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static tds.exam.ExamStatusCode.STATUS_APPROVED;

@RunWith(MockitoJUnitRunner.class)
public class ExamApprovalControllerTest {
    @Mock
    private ExamApprovalService mockExamApprovalService;

    private ExamApprovalController controller;

    @Before
    public void setUp() {
        HttpServletRequest request = new MockHttpServletRequest();
        ServletRequestAttributes requestAttributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(requestAttributes);

        controller = new ExamApprovalController(mockExamApprovalService);
    }

    @Test
    public void shouldGetAnExamApprovalRequestThatIsApproved() {
        UUID examId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        UUID browserId = UUID.randomUUID();
        ExamInfo examInfo = new ExamInfo(examId, sessionId, browserId);

        ExamApproval mockExamApproval = new ExamApproval(examId,
            new ExamStatusCode(STATUS_APPROVED,
                ExamStatusStage.OPEN),
            null);
        when(mockExamApprovalService.getApproval(isA(ExamInfo.class))).thenReturn(new Response<>(mockExamApproval));

        ResponseEntity<Response<ExamApproval>> response = controller.getApproval(
            examInfo.getExamId(),
            examInfo.getSessionId(),
            examInfo.getBrowserId());
        verify(mockExamApprovalService).getApproval(isA(ExamInfo.class));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().hasError()).isFalse();
        assertThat(response.getBody().getData().isPresent()).isTrue();
        assertThat(response.getBody().getData().get().getExamApprovalStatus()).isEqualTo(ExamApprovalStatus.APPROVED);
    }


    @Test
    public void shouldGetAValidationErrorWhenBrowserIdsDoNotMatch() {
        UUID examId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        UUID browserId = UUID.randomUUID();
        ExamInfo examInfo = new ExamInfo(examId, sessionId, browserId);

        Response<ExamApproval> errorResponse = new Response<ExamApproval>(new ValidationError(ValidationErrorCode.EXAM_APPROVAL_BROWSER_ID_MISMATCH, "foo"));
        when(mockExamApprovalService.getApproval(isA(ExamInfo.class))).thenReturn(errorResponse);

        ResponseEntity<Response<ExamApproval>> response = controller.getApproval(
            examInfo.getExamId(),
            examInfo.getSessionId(),
            examInfo.getBrowserId());
        verify(mockExamApprovalService).getApproval(isA(ExamInfo.class));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        Response<ExamApproval> body = response.getBody();
        assertThat(body.hasError()).isTrue();
        assertThat(body.getError().get().getCode()).isEqualTo(ValidationErrorCode.EXAM_APPROVAL_BROWSER_ID_MISMATCH);
        assertThat(body.getError().get().getMessage()).isEqualTo("foo");
        assertThat(response.getBody().getData().isPresent()).isFalse();
    }
}
