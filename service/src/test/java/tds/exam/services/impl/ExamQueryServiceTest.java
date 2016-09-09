package tds.exam.services.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;
import java.util.UUID;

import tds.exam.Exam;
import tds.exam.repository.ExamQueryRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExamQueryServiceTest {
    private ExamQueryRepository repository;
    private ExamServiceImpl examService;

    @Before
    public void setUp() {
        repository = mock(ExamQueryRepository.class);
        examService = new ExamServiceImpl(repository);
    }

    @After
    public void tearDown() {}

    @Test
    public void itShouldReturnAExam() {
        Exam exam = new Exam();
        UUID uniqueKey = UUID.randomUUID();
        exam.setUniqueKey(uniqueKey);
        when(repository.getExamByUniqueKey(uniqueKey)).thenReturn(Optional.of(exam));

        assertThat(examService.getExam(uniqueKey).get().getUniqueKey()).isEqualTo(uniqueKey);
    }
}
