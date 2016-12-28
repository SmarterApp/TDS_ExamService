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

    private String status;
    private ExamStatusStage stage;

    public String getStatus() {
        return status;
    }

    public ExamStatusStage getStage() {
        return stage;
    }

    /**
     * For Frameworks
     */
    private ExamStatusCode() {
    }

    public ExamStatusCode(String status, ExamStatusStage stage) {
        this.status = status;
        this.stage = stage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExamStatusCode that = (ExamStatusCode) o;

        if (status != null ? !status.equals(that.status) : that.status != null) return false;
        return stage != null ? stage.equals(that.stage) : that.stage == null;

    }

    @Override
    public int hashCode() {
        int result = status != null ? status.hashCode() : 0;
        result = 31 * result + (stage != null ? stage.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ExamStatusCode{" +
            "status='" + status + '\'' +
            ", stage='" + stage + '\'' +
            '}';
    }
}
