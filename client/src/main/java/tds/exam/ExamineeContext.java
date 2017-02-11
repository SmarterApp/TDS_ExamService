package tds.exam;

/**
 * Enumerate the possible contexts for an {@link tds.exam.ExamineeAttribute} or {@link tds.exam.ExamineeRelationship}:
 * <ul>
 * <li>{@link #INITIAL}</li>
 * <li>{@link #FINAL}</li>
 * </ul>
 */
public enum ExamineeContext {
    /**
     * The student package {@link tds.exam.ExamineeAttribute}s or {@link tds.exam.ExamineeRelationship}s were captured
     * when the {@link tds.exam.Exam} is completed.
     */
    FINAL("FINAL"),

    /**
     * The student package {@link tds.exam.ExamineeAttribute}s or {@link tds.exam.ExamineeRelationship}s were captured
     * when the {@link tds.exam.Exam} is being opened.
     */
    INITIAL("INITIAL");

    private final String type;

    ExamineeContext(String type) {
        this.type = type;
    }

    /**
     * @return A string representation of the {@link tds.exam.ExamineeContext} enum
     */
    String getType() {
        return type;
    }

    /**
     * Get an {@link tds.exam.ExamineeContext} from its string representation
     *
     * @param type The string that describes the type of {@link tds.exam.ExamineeContext} to get
     * @return The equivalent {@link tds.exam.ExamineeContext}
     */
    public static ExamineeContext fromType(String type) {
        for (ExamineeContext context : ExamineeContext.values()) {
            if (context.getType().equals(type)) {
                return context;
            }
        }

        throw new IllegalArgumentException(String.format("Could not find ExamineeContext for %s", type));
    }

    @Override
    public String toString() {
        return type;
    }
}
