package tds.exam;

import org.junit.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ExamTest {
    @Test
    public void anExamCanBeCreated() {
        UUID examId = UUID.randomUUID();
        Exam exam = new Exam();
        exam.setId(examId);

        assertThat(exam.getId()).isEqualTo(examId);
    }
}
