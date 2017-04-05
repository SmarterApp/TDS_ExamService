package tds.exam.web.interceptors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

import tds.common.Response;
import tds.common.ValidationError;
import tds.exam.ExamApproval;
import tds.exam.ExamInfo;
import tds.exam.ExamStatusCode;
import tds.exam.ExamStatusStage;
import tds.exam.error.ValidationErrorCode;
import tds.exam.services.ExamApprovalService;
import tds.exam.web.annotations.VerifyAccess;
import tds.exam.web.exceptions.ValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static tds.exam.ExamStatusCode.STATUS_APPROVED;

@RunWith(SpringRunner.class)
public class VerifyAccessInterceptorTests {
    private VerifyAccessInterceptor verifyAccessInterceptor;
    private HttpServletResponse mockResponse;

    @MockBean
    private ExamApprovalService mockExamApprovalService;

    @MockBean
    private HandlerMethod mockHandlerMethod;

    @MockBean
    private VerifyAccess mockVerifyAccess;

    @Captor
    ArgumentCaptor<ExamInfo> examInfoArgumentCaptor;

    @Before
    public void setup() {
        verifyAccessInterceptor = new VerifyAccessInterceptor(mockExamApprovalService);
        mockResponse = new MockHttpServletResponse();

        when(mockVerifyAccess.sessionParamName()).thenReturn("sessionId");
        when(mockVerifyAccess.browserParamName()).thenReturn("browserId");
    }

    @Test
    public void shouldReturnEarlyWhenNoAnnotation() throws Exception{
        UUID examId = UUID.randomUUID();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(String.format("/exam/%s/segments", examId));
        request.setMethod("GET");

        when(mockHandlerMethod.getMethodAnnotation(VerifyAccess.class)).thenReturn(null);

        boolean returnValue = verifyAccessInterceptor.preHandle(request, mockResponse, mockHandlerMethod);

        assertThat(returnValue).isTrue();
        verifyZeroInteractions(mockExamApprovalService);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentWhenDoesntStartWithExam() throws Exception {
        UUID examId = UUID.randomUUID();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(String.format("/bad/%s", examId));
        request.setMethod("GET");

        when(mockHandlerMethod.getMethodAnnotation(VerifyAccess.class)).thenReturn(mockVerifyAccess);

        verifyAccessInterceptor.preHandle(request, mockResponse, mockHandlerMethod);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentWhenExamIdIsNotUUID() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/exam/1234");
        request.setMethod("GET");

        when(mockHandlerMethod.getMethodAnnotation(VerifyAccess.class)).thenReturn(mockVerifyAccess);

        verifyAccessInterceptor.preHandle(request, mockResponse, mockHandlerMethod);
    }

    @Test
    public void shouldSuccessfullyVerifyAccess() throws Exception {
        UUID examId = UUID.randomUUID();
        UUID browserId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(String.format("/exam/%s/segments", examId));
        request.setMethod("GET");
        request.setParameter("sessionId", sessionId.toString());
        request.setParameter("browserId", browserId.toString());

        when(mockHandlerMethod.getMethodAnnotation(VerifyAccess.class)).thenReturn(mockVerifyAccess);

        ExamApproval mockExamApproval = new ExamApproval(examId,
            new ExamStatusCode(STATUS_APPROVED,
                ExamStatusStage.OPEN),
            null);
        when(mockExamApprovalService.getApproval(isA(ExamInfo.class))).thenReturn(new Response<>(mockExamApproval));

        boolean returnValue = verifyAccessInterceptor.preHandle(request, mockResponse, mockHandlerMethod);

        assertThat(returnValue).isTrue();
        verify(mockExamApprovalService).getApproval(isA(ExamInfo.class));
    }

    @Test(expected = ValidationException.class)
    public void shouldThrowValidationExceptionWhenAprovalFails() throws Exception {
        UUID examId = UUID.randomUUID();
        UUID browserId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(String.format("/exam/%s/segments", examId));
        request.setMethod("GET");
        request.setParameter("sessionId", sessionId.toString());
        request.setParameter("browserId", browserId.toString());

        when(mockHandlerMethod.getMethodAnnotation(VerifyAccess.class)).thenReturn(mockVerifyAccess);

        Response mockApprovalResponse = new Response<>(new ValidationError(ValidationErrorCode.EXAM_APPROVAL_BROWSER_ID_MISMATCH, "message"));
        when(mockExamApprovalService.getApproval(isA(ExamInfo.class))).thenReturn(mockApprovalResponse);

        verifyAccessInterceptor.preHandle(request, mockResponse, mockHandlerMethod);
    }

    @Test
    public void shouldUseCustomRequestParamValues() throws Exception {
        UUID examId = UUID.randomUUID();
        UUID browserId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(String.format("/exam/%s/segments", examId));
        request.setMethod("GET");
        request.setParameter("sessId", sessionId.toString());
        request.setParameter("browserId", browserId.toString());

        when(mockVerifyAccess.sessionParamName()).thenReturn("sessId");
        when(mockHandlerMethod.getMethodAnnotation(VerifyAccess.class)).thenReturn(mockVerifyAccess);

        ExamApproval mockExamApproval = new ExamApproval(examId,
            new ExamStatusCode(STATUS_APPROVED,
                ExamStatusStage.OPEN),
            null);
        when(mockExamApprovalService.getApproval(isA(ExamInfo.class))).thenReturn(new Response<>(mockExamApproval));

        boolean returnValue = verifyAccessInterceptor.preHandle(request, mockResponse, mockHandlerMethod);

        assertThat(returnValue).isTrue();
        verify(mockExamApprovalService).getApproval(examInfoArgumentCaptor.capture());

        ExamInfo examInfo = examInfoArgumentCaptor.getValue();
        assertThat(examInfo.getSessionId()).isEqualTo(sessionId);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenNullBrowserId() throws Exception {
        UUID examId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(String.format("/exam/%s/segments", examId));
        request.setMethod("GET");
        request.setParameter("sessionId", sessionId.toString());

        when(mockHandlerMethod.getMethodAnnotation(VerifyAccess.class)).thenReturn(mockVerifyAccess);

        verifyAccessInterceptor.preHandle(request, mockResponse, mockHandlerMethod);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenNullSessionId() throws Exception {
        UUID examId = UUID.randomUUID();
        UUID browserId = UUID.randomUUID();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(String.format("/exam/%s/segments", examId));
        request.setMethod("GET");
        request.setParameter("browserId", browserId.toString());

        when(mockHandlerMethod.getMethodAnnotation(VerifyAccess.class)).thenReturn(mockVerifyAccess);

        verifyAccessInterceptor.preHandle(request, mockResponse, mockHandlerMethod);
    }
}

