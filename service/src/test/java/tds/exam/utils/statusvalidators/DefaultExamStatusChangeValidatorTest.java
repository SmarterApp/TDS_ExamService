package tds.exam.utils.statusvalidators;

import org.joda.time.Instant;
import org.junit.Before;
import org.junit.Test;

import tds.exam.Exam;
import tds.exam.ExamStatusCode;
import tds.exam.builder.ExamBuilder;
import tds.exam.utils.ExamStatusChangeValidator;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultExamStatusChangeValidatorTest {
    private ExamStatusChangeValidator defaultExamStatusChangeValidator;

    @Before
    public void setUp() {
        defaultExamStatusChangeValidator = new DefaultStatusChangeValidator();
    }

    @Test
    public void shouldReturnTrueForAValidStatusTransition() {
        final Exam exam = new ExamBuilder()
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_PENDING), Instant.now())
            .build();
        final ExamStatusCode newStatus = new ExamStatusCode(ExamStatusCode.STATUS_APPROVED);

        final boolean result = defaultExamStatusChangeValidator.validate(exam, newStatus);

        assertThat(result).isTrue();
    }

    @Test
    public void shouldReturnFalesForAnInvalidStatusTransition() {
        final Exam exam = new ExamBuilder()
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_DENIED), Instant.now())
            .build();
        final ExamStatusCode newStatus = new ExamStatusCode(ExamStatusCode.STATUS_STARTED);

        final boolean result = defaultExamStatusChangeValidator.validate(exam, newStatus);

        assertThat(result).isFalse();
    }
}
