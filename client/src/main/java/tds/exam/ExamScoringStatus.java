package tds.exam;

/**
 * Enumerate possible scoring statuses.  Source: {@code tds.itemscoringengine.ScoringStatus}:
 * <ul>
 * <li>{@link #NOT_SCORED}</li>
 * <li>{@link #SCORED}</li>
 * <li>{@link #WAITING_FOR_MACHINE_SCORE}</li>
 * <li>{@link #NO_SCORING_ENGINE}</li>
 * <li>{@link #SCORING_ERROR}</li>
 * </ul>
 */
public enum ExamScoringStatus {
    /**
     * Item has not been scored. The item has an indeterminate score.
     */
    NOT_SCORED("NotScored"),

    /**
     * Item has been scored properly. The item's score is valid.
     */
    SCORED("Scored"),

    /**
     * Item is in the process of being scored. The item has an indeterminate score.
     */
    WAITING_FOR_MACHINE_SCORE("WaitingForMachineScore"),

    /**
     * Item cannot be scored as no suitable scorer exists for this item type.  The item has an indeterminate score.
     */
    NO_SCORING_ENGINE("NoScoringEngine"),

    /**
     * Item cannot be scored as an error occurred during the scoring process.  The item has an indeterminate score.
     */
    SCORING_ERROR("ScoringError");

    private final String type;

    ExamScoringStatus(String type) {
        this.type = type;
    }

    String getType() {
        return type;
    }

    /**
     * Get an {@link tds.exam.ExamScoringStatus} from its string representation
     *
     * @param type The string that describes the type of {@link tds.exam.ExamScoringStatus} to get
     * @return The equivalent {@link tds.exam.ExamScoringStatus}
     */
    public static ExamScoringStatus fromType(String type) {
        for (ExamScoringStatus status : ExamScoringStatus.values()) {
            if (status.getType().equals(type)) {
                return status;
            }
        }

        throw new IllegalArgumentException(String.format("Could not find ExamScoringStatus for %s", type));
    }

    @Override
    public String toString() {
        return type;
    }
}
