package tds.exam;

public class ExamStatusCode {
    public static final String STAGE_CLOSED = "closed";
    public static final String STAGE_INACTIVE = "inactive";
    public static final String STAGE_INUSE = "inuse";
    public static final String STAGE_OPEN = "open";

    public static final String STATUS_PAUSED = "paused";
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_APPROVED = "approved";

    private String status;
    private String stage;
    private String description;

    public String getStatus() {
        return status;
    }

    public String getStage() {
        return stage;
    }

    public String getDescription() {
        return description;
    }

    private ExamStatusCode(Builder builder) {
        status = builder.status;
        stage = builder.stage;
        description = builder.description;
    }

    public static class Builder {
        private String status;
        private String stage;
        private String description;

        public Builder withStatus(String newStatus) {
            status = newStatus;
            return this;
        }

        public Builder withStage(String newStage) {
            stage = newStage;
            return this;
        }

        public Builder withDescription(String newDescription) {
            description = newDescription;
            return this;
        }

        public ExamStatusCode build() {
            return new ExamStatusCode(this);
        }
    }
}
