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

import org.joda.time.Instant;

import java.util.UUID;

import tds.common.util.Preconditions;


/**
 * Represents a comment/note made via the Notepad tool in the Student user interface
 */
public class ExamineeNote {
    private long id;
    private UUID examId;
    private ExamineeNoteContext context;
    private int itemPosition;
    private String note;
    private Instant createdAt;

    /**
     * Private constructor for frameworks
     */
    private ExamineeNote() {
    }

    private ExamineeNote(Builder builder) {
        this.id = builder.id;
        this.examId = builder.examId;
        this.context = builder.context;
        this.itemPosition = builder.itemPosition;
        this.note = builder.note;
        this.createdAt = builder.createdAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ExamineeNote that = (ExamineeNote) o;

        if (id != that.id) return false;
        if (itemPosition != that.itemPosition) return false;
        if (!examId.equals(that.examId)) return false;
        if (context != that.context) return false;
        if (!note.equals(that.note)) return false;
        return createdAt != null ? createdAt.equals(that.createdAt) : that.createdAt == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + examId.hashCode();
        result = 31 * result + context.hashCode();
        result = 31 * result + itemPosition;
        result = 31 * result + note.hashCode();
        result = 31 * result + (createdAt != null ? createdAt.hashCode() : 0);
        return result;
    }

    public static class Builder {
        private long id;
        private UUID examId;
        private ExamineeNoteContext context;
        private int itemPosition;
        private String note;
        private Instant createdAt;

        public Builder withId(final long id) {
            this.id = id;
            return this;
        }

        public Builder withExamId(final UUID examId) {
            Preconditions.checkNotNull(examId, "examId cannot be null");
            this.examId = examId;
            return this;
        }

        public Builder withContext(final ExamineeNoteContext context) {
            Preconditions.checkNotNull(context, "context cannot be null");
            this.context = context;
            return this;
        }

        public Builder withItemPosition(final int itemPosition) {
            this.itemPosition = itemPosition;
            return this;
        }

        public Builder withNote(final String note) {
            Preconditions.checkNotNull(note, "note cannot be null");
            this.note = note;
            return this;
        }

        public Builder withCreatedAt(final Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public ExamineeNote build() {
            // RULE:  If the note is in the context of an exam, the position should be set to 0.  Item position is not
            // relevant to Exam-level notes
            if (this.context.equals(ExamineeNoteContext.EXAM) && this.itemPosition > 0) {
                throw new IllegalStateException("itemPosition must be 0 for an ExamineeNote within an EXAM-level context");
            }

            return new ExamineeNote(this);
        }

        public static Builder fromExamineeNote(ExamineeNote examineeNote) {
            return new ExamineeNote.Builder()
                .withId(examineeNote.id)
                .withExamId(examineeNote.examId)
                .withContext(examineeNote.context)
                .withItemPosition(examineeNote.itemPosition)
                .withNote(examineeNote.note)
                .withCreatedAt(examineeNote.createdAt);
        }
    }

    /**
     * @return The unique identifier of this {@link ExamineeNote}
     */
    public long getId() {
        return id;
    }

    /**
     * @return The unique identifier of the {@link tds.exam.Exam} to which this {@link ExamineeNote} belongs
     */
    public UUID getExamId() {
        return examId;
    }

    /**
     * @return The {@link ExamineeNoteContext} describing what this {@link tds.exam.ExamineeNote} is associated
     * to
     */
    public ExamineeNoteContext getContext() {
        return context;
    }

    /**
     * @return The position of the {@link tds.exam.ExamItem} for which this {@link ExamineeNote} was created
     * <p>
     * This property is only relevant for an item-scoped {@link tds.exam.ExamineeNote}.  An exam-scoped
     * {@link tds.exam.ExamineeNote} will always have an item position of 0.
     * </p>
     */
    public int getItemPosition() {
        return itemPosition;
    }

    /**
     * @return The text for this {@link ExamineeNote}
     */
    public String getNote() {
        return note;
    }

    /**
     * @return The {@link org.joda.time.Instant} the note was created at
     */
    public Instant getCreatedAt() {
        return createdAt;
    }
}
