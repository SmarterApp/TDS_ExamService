package tds.exam.controllers;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import tds.exam.Exam;
import tds.exam.services.ExamService;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

public class ExamControllerTest {
    private ExamService examService;
    private ExamController controller;

    @Before
    public void setUp() {
        examService = mock(ExamService.class);
        controller = new ExamController(examService);
    }

    @After
    public void tearDown() {}

    @Test
    public void anExamCanBeReturnedForWithValidId() {
        UUID uuid = UUID.randomUUID();
        when(examService.getExam(uuid)).thenReturn(new Exam(uuid));

        Exam exam = controller.getExamById(uuid);

        assertThat(exam.getId()).isEqualTo(uuid);

        verify(examService).getExam(uuid);
    }
}
