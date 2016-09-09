package tds.exam.web.endpoints;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

import tds.exam.Exam;
import tds.exam.services.ExamService;
import tds.exam.web.resources.ExamResource;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

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
    public void tearDown() {}

    @Test
    public void anExamCanBeReturnedForWithValidId() {
        UUID uuid = UUID.randomUUID();
        Exam exam = new Exam();
        exam.setId(uuid);
        when(examService.getExam(uuid)).thenReturn(Optional.of(exam));

        ResponseEntity<ExamResource> response = controller.getExamById(uuid);
        verify(examService).getExam(uuid);

        assertThat(response.getBody().getExam().getId()).isEqualTo(uuid);
        assertThat(response.getBody().getId().getHref()).isEqualTo("http://localhost/exam/" + uuid.toString());
    }
}
