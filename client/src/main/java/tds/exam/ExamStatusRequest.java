package tds.exam;

/**
 * Represents a request for updating the status of an {@link tds.exam.Exam}
 */
public class ExamStatusRequest {
    private ExamStatusCode examStatus;
    private String reason;

    /* Empty private constructor for frameworks */
    private ExamStatusRequest() {

    }

    public ExamStatusRequest(final ExamStatusCode examStatus, final String reason) {
        this.examStatus = examStatus;
        this.reason = reason;
    }

    /**
     * @return The status to update the exam to
     */
    public ExamStatusCode getExamStatus() {
        return examStatus;
    }

    /**
     * @return The reason for the status change
     */
    public String getReason() {
        return reason;
    }
}
