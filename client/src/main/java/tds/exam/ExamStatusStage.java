package tds.exam;

/**
 * Exam status also has an associated stage.
 */
public enum ExamStatusStage {
    IN_PROGRESS("inprogress"),
    IN_USE("inuse"),
    CLOSED("closed"),
    INACTIVE("inactive"),
    OPEN("open");

    private final String type;

    ExamStatusStage(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    /**
     * @param type the string type for the stage
     * @return the equivalent {@link tds.exam.ExamStatusStage}
     */
    public static ExamStatusStage fromType(String type) {
        for (ExamStatusStage stage : ExamStatusStage.values()) {
            if (stage.getType().equals(type)) {
                return stage;
            }
        }

        throw new IllegalArgumentException(String.format("Could not find ExamStatusStage for %s", type));
    }
}
