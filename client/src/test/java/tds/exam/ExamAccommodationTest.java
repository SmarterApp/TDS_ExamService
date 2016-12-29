package tds.exam;

import org.joda.time.Instant;
import org.junit.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ExamAccommodationTest {
    @Test
    public void shouldCreateAnAccommodation() {
        UUID mockExamId = UUID.randomUUID();
        ExamAccommodation examAccommodation = new ExamAccommodation.Builder()
            .withId(1L)
            .withExamId(mockExamId)
            .withSegmentKey("Segment 1")
            .withType("unit test type")
            .withCode("unit test code")
            .withCreatedAt(Instant.now())
            .build();

        assertThat(examAccommodation.getId()).isEqualTo(1L);
        assertThat(examAccommodation.getExamId()).isEqualTo(mockExamId);
        assertThat(examAccommodation.getSegmentKey()).isEqualTo("Segment 1");
        assertThat(examAccommodation.getType()).isEqualTo("unit test type");
        assertThat(examAccommodation.getCode()).isEqualTo("unit test code");
    }

    @Test
    public void shouldBeApproved() {
        ExamAccommodation examAccommodation = new ExamAccommodation.Builder()
            .withId(1L)
            .withExamId(UUID.randomUUID())
            .withSegmentKey("Segment 1")
            .withType("unit test type")
            .withCode("unit test code")
            .withCreatedAt(Instant.now())
            .build();

        assertThat(examAccommodation.isApproved()).isTrue();
    }

    @Test
    public void shouldNotBeApproved() {
        ExamAccommodation examAccommodation = new ExamAccommodation.Builder()
            .withId(1L)
            .withExamId(UUID.randomUUID())
            .withSegmentKey("Segment 1")
            .withType("unit test type")
            .withCode("unit test code")
            .withDeniedAt(Instant.now())
            .withCreatedAt(Instant.now())
            .build();

        assertThat(examAccommodation.isApproved()).isFalse();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionBecasueExamIdCannotBeNull() {
        ExamAccommodation examAccommodation = new ExamAccommodation.Builder()
            .withExamId(null)
            .withSegmentKey("Segment 1")
            .withType("unit test type")
            .withCode("unit test code")
            .withCreatedAt(Instant.now())
            .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionBecausSegmentIdCannotBeNull() {
        ExamAccommodation examAccommodation = new ExamAccommodation.Builder()
            .withId(1L)
            .withExamId(UUID.randomUUID())
            .withSegmentKey(null)
            .withType("unit test type")
            .withCode("unit test code")
            .withCreatedAt(Instant.now())
            .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionBecauseTypeCannotBeNull() {
        new ExamAccommodation.Builder()
            .withId(1L)
            .withExamId(UUID.randomUUID())
            .withSegmentKey("Segment 1")
            .withType(null)
            .withCode("unit test code")
            .withCreatedAt(Instant.now())
            .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionBecauseCodeCannotBeNull() {
        new ExamAccommodation.Builder()
            .withId(1L)
            .withExamId(UUID.randomUUID())
            .withSegmentKey("Segment 1")
            .withType("unit test type")
            .withCode(null)
            .withCreatedAt(Instant.now())
            .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionBecauseCreatedAtCannotBeNull() {
        new ExamAccommodation.Builder()
            .withId(1L)
            .withExamId(UUID.randomUUID())
            .withSegmentKey("Segment 1")
            .withType("unit test type")
            .withCode("unit test code")
            .withCreatedAt(null)
            .build();
    }
}
