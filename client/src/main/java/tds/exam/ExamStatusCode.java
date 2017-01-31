package tds.exam;

public class ExamStatusCode {
    public static final String STATUS_PAUSED = "paused";
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_APPROVED = "approved";
    public static final String STATUS_SUSPENDED = "suspended";
    public static final String STATUS_REVIEW = "review";
    public static final String STATUS_INITIALIZING = "initializing";
    public static final String STATUS_STARTED = "started";
    public static final String STATUS_FAILED = "failed";
    public static final String STATUS_DENIED = "denied";
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_SCORED = "scored";
    public static final String STATUS_SEGMENT_ENTRY = "segmentEntry";
    public static final String STATUS_SEGMENT_EXIT = "segmentExit";
    public static final String STATUS_FORCE_COMPLETED = "forceCompleted";
    public static final String STATUS_INVALIDATED = "invalidated";
    public static final String STATUS_EXPIRED = "expired";
    public static final String STATUS_SUBMITTED = "submitted";
    public static final String STATUS_RESCORED = "rescored";
    public static final String STATUS_REPORTED = "reported";
    public static final String STATUS_CLOSED = "closed";
    public static final String STATUS_DISABLED = "disabled";

    private String code;
    private ExamStatusStage stage;

    public String getCode() {
        return code;
    }

    public ExamStatusStage getStage() {
        return stage;
    }

    /**
     * Private constructor for frameworks
     */
    private ExamStatusCode() {
    }

    public ExamStatusCode(String code, ExamStatusStage stage) {
        this.code = code;
        this.stage = stage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExamStatusCode that = (ExamStatusCode) o;

        if (code != null ? !code.equals(that.code) : that.code != null) return false;
        return stage != null ? stage.equals(that.stage) : that.stage == null;

    }

    @Override
    public int hashCode() {
        int result = code != null ? code.hashCode() : 0;
        result = 31 * result + (stage != null ? stage.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ExamStatusCode{" +
            "code='" + code + '\'' +
            ", stage='" + stage + '\'' +
            '}';
    }
}
