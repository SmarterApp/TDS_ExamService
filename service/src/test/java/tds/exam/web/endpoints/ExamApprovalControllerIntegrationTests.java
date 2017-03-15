package tds.exam.web.endpoints;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import tds.common.Response;
import tds.common.ValidationError;
import tds.common.configuration.JacksonObjectMapperConfiguration;
import tds.common.configuration.SecurityConfiguration;
import tds.common.web.advice.ExceptionAdvice;
import tds.exam.ExamApproval;
import tds.exam.ExamInfo;
import tds.exam.ExamStatusCode;
import tds.exam.ExamStatusStage;
import tds.exam.configuration.web.InterceptorConfiguration;
import tds.exam.error.ValidationErrorCode;
import tds.exam.services.ExamApprovalService;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = ExamApprovalController.class, excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {InterceptorConfiguration.class})})
@Import({ExceptionAdvice.class, JacksonObjectMapperConfiguration.class, SecurityConfiguration.class})
//@WebMvcControllerIntegrationTest(controllers = ExamAccommodationController.class)
public class ExamApprovalControllerIntegrationTests {
    @Autowired
    private MockMvc http;

    @MockBean
    private ExamApprovalService mockExamApprovalService;

    @Test
    public void shouldGetExamApproval() throws Exception {
        UUID examId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        UUID browserId = UUID.randomUUID();

        ExamInfo examInfo = new ExamInfo(examId, sessionId, browserId);
        ArgumentCaptor<ExamInfo> approvalRequestArgumentCaptor = ArgumentCaptor.forClass(ExamInfo.class);
        ExamApproval mockApproval = new ExamApproval(examId,
            new ExamStatusCode(ExamStatusCode.STATUS_APPROVED, ExamStatusStage.INACTIVE),
            null);

        when(mockExamApprovalService.getApproval(isA(ExamInfo.class)))
            .thenReturn(new Response<>(mockApproval));

        http.perform(get("/exam/{id}/approval", examId)
            .contentType(MediaType.APPLICATION_JSON)
            .param("sessionId", examInfo.getSessionId().toString())
            .param("browserId", examInfo.getBrowserId().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("data").isNotEmpty())
            .andExpect(jsonPath("data.examId", is(examId.toString())))
            .andExpect(jsonPath("data.examApprovalStatus", is(ExamStatusCode.STATUS_APPROVED.toUpperCase())));

        verify(mockExamApprovalService).getApproval(approvalRequestArgumentCaptor.capture());
    }

    @Test
    public void shouldGetExamApprovalFailureWithValidationError() throws Exception {
        UUID examId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        UUID browserId = UUID.randomUUID();

        ExamInfo examInfo = new ExamInfo(examId, sessionId, browserId);
        ArgumentCaptor<ExamInfo> approvalRequestArgumentCaptor = ArgumentCaptor.forClass(ExamInfo.class);
        ValidationError mockFailure = new ValidationError(ValidationErrorCode.EXAM_APPROVAL_SESSION_CLOSED, "session is closed");

        when(mockExamApprovalService.getApproval(isA(ExamInfo.class)))
            .thenReturn(new Response<>(mockFailure));

        http.perform(get("/exam/{id}/approval", examId)
            .contentType(MediaType.APPLICATION_JSON)
            .param("sessionId", examInfo.getSessionId().toString())
            .param("browserId", examInfo.getBrowserId().toString()))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("error").isNotEmpty())
            .andExpect(jsonPath("error.code", is(ValidationErrorCode.EXAM_APPROVAL_SESSION_CLOSED)))
            .andExpect(jsonPath("error.message", is("session is closed")));

        verify(mockExamApprovalService).getApproval(approvalRequestArgumentCaptor.capture());
    }
}
