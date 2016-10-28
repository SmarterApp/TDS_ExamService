package tds.exam;

import java.time.Instant;
import java.util.UUID;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AccommodationTest {
    @Test
    public void shouldCreateAnAccommodation() {
        UUID mockExamId = UUID.randomUUID();
        Accommodation accommodation = new Accommodation.Builder()
            .withId(1L)
            .withExamId(mockExamId)
            .withSegmentId("Segment 1")
            .withType("unit test type")
            .withCode("unit test code")
            .withCreatedAt(Instant.now())
            .build();

        assertThat(accommodation.getId()).isEqualTo(1L);
        assertThat(accommodation.getExamId()).isEqualTo(mockExamId);
        assertThat(accommodation.getSegmentId()).isEqualTo("Segment 1");
        assertThat(accommodation.getType()).isEqualTo("unit test type");
        assertThat(accommodation.getCode()).isEqualTo("unit test code");
    }

    @Test
    public void shouldBeApproved() {
        Accommodation accommodation = new Accommodation.Builder()
            .withId(1L)
            .withExamId(UUID.randomUUID())
            .withSegmentId("Segment 1")
            .withType("unit test type")
            .withCode("unit test code")
            .withCreatedAt(Instant.now())
            .build();

        assertThat(accommodation.isApproved()).isTrue();
    }

    @Test
    public void shouldNotBeApproved() {
        Accommodation accommodation = new Accommodation.Builder()
            .withId(1L)
            .withExamId(UUID.randomUUID())
            .withSegmentId("Segment 1")
            .withType("unit test type")
            .withCode("unit test code")
            .withDeniedAt(Instant.now())
            .withCreatedAt(Instant.now())
            .build();

        assertThat(accommodation.isApproved()).isFalse();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionBecasueExamIdCannotBeNull() {
        Accommodation accommodation = new Accommodation.Builder()
            .withExamId(null)
            .withSegmentId("Segment 1")
            .withType("unit test type")
            .withCode("unit test code")
            .withCreatedAt(Instant.now())
            .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionBecausSegmentIdCannotBeNull() {
        Accommodation accommodation = new Accommodation.Builder()
            .withId(1L)
            .withExamId(UUID.randomUUID())
            .withSegmentId(null)
            .withType("unit test type")
            .withCode("unit test code")
            .withCreatedAt(Instant.now())
            .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionBecauseTypeCannotBeNull() {
        Accommodation accommodation = new Accommodation.Builder()
            .withId(1L)
            .withExamId(UUID.randomUUID())
            .withSegmentId("Segment 1")
            .withType(null)
            .withCode("unit test code")
            .withCreatedAt(Instant.now())
            .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionBecauseCodeCannotBeNull() {
        Accommodation accommodation = new Accommodation.Builder()
            .withId(1L)
            .withExamId(UUID.randomUUID())
            .withSegmentId("Segment 1")
            .withType("unit test type")
            .withCode(null)
            .withCreatedAt(Instant.now())
            .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionBecauseCreatedAtCannotBeNull() {
        Accommodation accommodation = new Accommodation.Builder()
            .withId(1L)
            .withExamId(UUID.randomUUID())
            .withSegmentId("Segment 1")
            .withType("unit test type")
            .withCode("unit test code")
            .withCreatedAt(null)
            .build();
    }
}
