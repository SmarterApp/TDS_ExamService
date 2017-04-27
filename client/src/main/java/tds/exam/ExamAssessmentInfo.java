package tds.exam;

/**
 * An object containing recent exam data for an assessment
 */
public class ExamAssessmentInfo {
    private String assessmentKey;
    private String assessmentId;
    private int attempt;
    private int maxAttempts;
    private String assessmentLabel;
    private String status;
    private String subject;
    private String grade;
    private String deniedReason;

    /**
     * Private constructor for frameworks
     */
    private ExamAssessmentInfo() {}

    private ExamAssessmentInfo(Builder builder) {
        this.subject = builder.subject;
        this.assessmentKey = builder.assessmentKey;
        this.assessmentLabel = builder.assessmentLabel;
        this.status = builder.status;
        this.grade = builder.grade;
        this.attempt = builder.attempt;
        this.maxAttempts = builder.maxAttempts;
        this.assessmentId = builder.assessmentId;
        this.deniedReason = builder.deniedReason;
    }

    public static class Builder {
        private String assessmentKey;
        private String assessmentId;
        private int attempt;
        private int maxAttempts;
        private String assessmentLabel;
        private String status;
        private String subject;
        private String grade;
        private String deniedReason;

        public Builder withAssessmentKey(String assessmentKey) {
            this.assessmentKey = assessmentKey;
            return this;
        }

        public Builder withAssessmentId(String assessmentId) {
            this.assessmentId = assessmentId;
            return this;
        }

        public Builder withAttempt(int attempt) {
            this.attempt = attempt;
            return this;
        }

        public Builder withMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
            return this;
        }

        public Builder withAssessmentLabel(String assessmentLabel) {
            this.assessmentLabel = assessmentLabel;
            return this;
        }

        public Builder withStatus(String status) {
            this.status = status;
            return this;
        }

        public Builder withSubject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder withGrade(String grade) {
            this.grade = grade;
            return this;
        }

        public Builder withDeniedReason(String deniedReason) {
            this.deniedReason = deniedReason;
            return this;
        }

        public ExamAssessmentInfo build() {
            return new ExamAssessmentInfo(this);
        }
    }

    /**
     * @return The key of the assessment for the exam
     */
    public String getAssessmentKey() {
        return assessmentKey;
    }

    /**
     * @return The id of the assessment for the exam
     */
    public String getAssessmentId() {
        return assessmentId;
    }

    /**
     * @return The current attempt number for the assessment
     */
    public int getAttempt() {
        return attempt;
    }

    /**
     * @return The maximum number of attempts for an assessment
     */
    public int getMaxAttempts() {
        return maxAttempts;
    }

    /**
     * @return The label for the assessment
     */
    public String getAssessmentLabel() {
        return assessmentLabel;
    }

    /**
     * @return The status of exam assessment
     */
    public String getStatus() {
        return status;
    }

    /**
     * @return The subject of the assessment
     */
    public String getSubject() {
        return subject;
    }

    /**
     * @return The grade of the assessment
     */
    public String getGrade() {
        return grade;
    }

    /**
     * @return The reason why the assessment is unavailable
     */
    public String getDeniedReason() {
        return deniedReason;
    }
}
