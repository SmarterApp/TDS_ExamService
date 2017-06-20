/***************************************************************************************************
 * Copyright 2017 Regents of the University of California. Licensed under the Educational
 * Community License, Version 2.0 (the “license”); you may not use this file except in
 * compliance with the License. You may obtain a copy of the license at
 *
 * https://opensource.org/licenses/ECL-2.0
 *
 * Unless required under applicable law or agreed to in writing, software distributed under the
 * License is distributed in an “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for specific language governing permissions
 * and limitations under the license.
 **************************************************************************************************/

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
