package tds.exam;

import javax.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Request approval to an {@link Exam}.
 */
public class ApprovalRequest {
    @NotNull
    private UUID examId;

    @NotNull
    private UUID sessionId;

    @NotNull
    private UUID browserId;

    public ApprovalRequest(UUID examId, UUID sessionId, UUID browserId) {
        this.examId = examId;
        this.sessionId = sessionId;
        this.browserId = browserId;
    }

    /**
     * Private constructor for frameworks
     */
    private ApprovalRequest() {
    }

    /**
     * @return The id of the exam for which access is being requested
     */
    public UUID getExamId() {
        return examId;
    }

    /**
     * @return The id of the session that hosts the exam
     */
    public UUID getSessionId() {
        return sessionId;
    }

    /**
     * @return The id of the user's browser
     */
    public UUID getBrowserId() {
        return browserId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ApprovalRequest)) return false;

        ApprovalRequest that = (ApprovalRequest) o;

        if (!getExamId().equals(that.getExamId())) return false;
        if (!getSessionId().equals(that.getSessionId())) return false;
        return getBrowserId().equals(that.getBrowserId());
    }

    @Override
    public int hashCode() {
        int result = getExamId().hashCode();
        result = 31 * result + getSessionId().hashCode();
        result = 31 * result + getBrowserId().hashCode();
        return result;
    }
}
