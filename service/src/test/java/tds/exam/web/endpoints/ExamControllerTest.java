package tds.exam.web.endpoints;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
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
import tds.exam.Exam;
import tds.exam.OpenExamRequest;
import tds.exam.error.ValidationErrorCode;
import tds.exam.services.ExamService;
import tds.exam.web.resources.ExamResource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ExamControllerTest {
    private ExamService examService;
    private ExamController controller;

    @Before
    public void setUp() {
        HttpServletRequest request = new MockHttpServletRequest();
        ServletRequestAttributes requestAttributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(requestAttributes);

        examService = mock(ExamService.class);
        controller = new ExamController(examService);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void shouldReturnExam() {
        UUID uuid = UUID.randomUUID();
        when(examService.getExam(uuid)).thenReturn(Optional.of(new Exam.Builder().withId(uuid).build()));

        ResponseEntity<ExamResource> response = controller.getExamById(uuid);
        verify(examService).getExam(uuid);

        assertThat(response.getBody().getExam().getId()).isEqualTo(uuid);
        assertThat(response.getBody().getId().getHref()).isEqualTo("http://localhost/exam/" + uuid.toString());
    }

    @Test(expected = NotFoundException.class)
    public void shouldReturnNotFoundWhenExamCannotBeFoundById() {
        UUID uuid = UUID.randomUUID();
        when(examService.getExam(uuid)).thenReturn(Optional.empty());
        controller.getExamById(uuid);
    }

    @Test
    public void shouldCreateErrorResponseWhenOpenExamFailsWithValidationError() {
        OpenExamRequest openExamRequest = new OpenExamRequest();
        openExamRequest.setClientName("SBAC-PT");
        openExamRequest.setSessionId(UUID.randomUUID());
        openExamRequest.setStudentId(1);

        when(examService.openExam(openExamRequest)).thenReturn(new Response<Exam>(new ValidationError(ValidationErrorCode.SESSION_TYPE_MISMATCH, "Session mismatch")));

        ResponseEntity<ExamResource> response = controller.openExam(openExamRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody().getErrors()).hasSize(1);
        assertThat(response.getBody().getErrors()[0].getCode()).isEqualTo(ValidationErrorCode.SESSION_TYPE_MISMATCH);
    }

    @Test
    public void shouldOpenExam() throws URISyntaxException {
        OpenExamRequest openExamRequest = new OpenExamRequest();
        openExamRequest.setClientName("SBAC-PT");
        openExamRequest.setSessionId(UUID.randomUUID());
        openExamRequest.setStudentId(1);

        UUID examId = UUID.randomUUID();
        when(examService.openExam(openExamRequest)).thenReturn(new Response<>(new Exam.Builder().withId(examId).build()));

        ResponseEntity<ExamResource> response = controller.openExam(openExamRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getLocation()).isEqualTo(new URI("http://localhost/exam/" + examId));
    }
}
