package tds.exam;

/**
 * Describe the possible contexts for an {@link tds.exam.ExamineeNote}:
 * <ul>
 * <li>{@link #EXAM}</li>
 * <li>{@link #ITEM}</li>
 * </ul>
 */
public enum ExamineeNoteContext {
    /**
     * The {@link tds.exam.ExamineeNote} was created using the Notepad tool for the {@link tds.exam.Exam}.
     */
    EXAM("GlobalNotes"),

    /**
     * The {@link tds.exam.ExamineeNote} was created using the Notepad tool at a specific {@link tds.exam.ExamItem}
     */
    ITEM("TESTITEM");

    private final String type;

    ExamineeNoteContext(final String type) {
        this.type = type;
    }

    String getType() {
        return type;
    }

    /**
     * Get an {@link ExamineeNoteContext} from its string representation
     *
     * @param type The string that describes the type of {@link ExamineeNoteContext} to get
     * @return The equivalent {@link ExamineeNoteContext}
     */
    public static ExamineeNoteContext fromType(final String type) {
        for (ExamineeNoteContext context : ExamineeNoteContext.values()) {
            if (context.getType().equals(type)) {
                return context;
            }
        }

        throw new IllegalArgumentException(String.format("Could not find ExamineeNoteContext for %s", type));
    }

    @Override
    public String toString() {
        return type;
    }
}
