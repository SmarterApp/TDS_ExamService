package tds.exam;

import org.joda.time.Instant;
import org.junit.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ExamItemResponseTest {
    @Test
    public void shouldBuildAnItemResponse() {
        UUID examId = UUID.randomUUID();
        ExamItemResponse response = new ExamItemResponse.Builder()
            .withId(1L)
            .withResponse("response text")
            .withExamItemId(examId)
            .withSequence(1)
            .withCreatedAt(Instant.now().minus(20000))
            .build();

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getResponse()).isEqualTo("response text");
        assertThat(response.getExamItemId()).isEqualTo(examId);
        assertThat(response.getSequence()).isEqualTo(1);
        assertThat(response.getCreatedAt()).isLessThan(Instant.now());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenResponseIsNull() {
        ExamItemResponse response = new ExamItemResponse.Builder()
            .withId(1L)
            .withResponse(null)
            .withSequence(1)
            .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowAnIllegalArgumentExceptionWhenSequenceIsLessThanOne() {
        ExamItemResponse response = new ExamItemResponse.Builder()
            .withId(1L)
            .withResponse("response")
            .withSequence(0)
            .build();
    }
}
