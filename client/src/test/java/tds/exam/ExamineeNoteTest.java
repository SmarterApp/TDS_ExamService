package tds.exam;

import org.junit.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ExamineeNoteTest {
    @Test
    public void shouldCreateAnExamineeNote() {
        UUID mockExamId = UUID.randomUUID();
        ExamineeNote note = new ExamineeNote.Builder()
            .withId(42L)
            .withExamId(mockExamId)
            .withContext(ExamineeNoteContext.ITEM)
            .withItemPosition(5)
            .withNote("exam item note")
            .build();

        assertThat(note.getId()).isEqualTo(42L);
        assertThat(note.getExamId()).isEqualTo(mockExamId);
        assertThat(note.getContext()).isEqualTo(ExamineeNoteContext.ITEM);
        assertThat(note.getItemPosition()).isEqualTo(5);
        assertThat(note.getNote()).isEqualTo("exam item note");
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotCreateAnExamineeNoteWithExamContextAndItemPositionGreaterThanZero() {
        ExamineeNote note = new ExamineeNote.Builder()
            .withId(42L)
            .withExamId(UUID.randomUUID())
            .withContext(ExamineeNoteContext.EXAM)
            .withItemPosition(5)
            .withNote("exam item note")
            .build();
    }
}
