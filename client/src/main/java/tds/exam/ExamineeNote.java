package tds.exam;

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
    }

    public static class Builder {
        private long id;
        private UUID examId;
        private ExamineeNoteContext context;
        private int itemPosition;
        private String note;

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

        public ExamineeNote build() {
            // RULE:  If the note is in the context of an exam, the position should be set to 0.  Item position is not
            // relevant to Exam-level notes
            if (this.context.equals(ExamineeNoteContext.EXAM) && this.itemPosition > 0) {
                throw new IllegalStateException("itemPosition must be 0 for an ExamineeNote within an EXAM-level context");
            }

            return new ExamineeNote(this);
        }

        public Builder fromExamineeNote(ExamineeNote examineeNote) {
            id = examineeNote.id;
            examId = examineeNote.examId;
            context = examineeNote.context;
            itemPosition = examineeNote.itemPosition;
            note = examineeNote.note;
            return this;
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
}
