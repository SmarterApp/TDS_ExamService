package tds.exam;

import java.util.UUID;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AccommodationTest {
    @Test
    public void shouldCreateAnAccommodation() {
        UUID mockExamId = UUID.randomUUID();
        Accommodation accommodation = new Accommodation(1L,
            mockExamId,
            1,
            "unit test type",
            "unit test code");

        assertThat(accommodation.getId()).isEqualTo(1L);
        assertThat(accommodation.getExamId()).isEqualTo(mockExamId);
        assertThat(accommodation.getSegmentId()).isEqualTo(1);
        assertThat(accommodation.getType()).isEqualTo("unit test type");
        assertThat(accommodation.getCode()).isEqualTo("unit test code");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionBecauseIdCannotBeNull() {
        Accommodation accommodation = new Accommodation(null,
            UUID.randomUUID(),
            1,
            "unit test type",
            "unit test code");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionBecasueExamIdCannotBeNull() {
        Accommodation accommodation = new Accommodation(1L,
            null,
            1,
            "unit test type",
            "unit test code");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionBecauseTypeCannotBeNull() {
        Accommodation accommodation = new Accommodation(null,
            UUID.randomUUID(),
            1,
            null,
            "unit test code");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionBecauseCodeCannotBeNull() {
        Accommodation accommodation = new Accommodation(null,
            UUID.randomUUID(),
            1,
            "unit test type",
            null);
    }
}
