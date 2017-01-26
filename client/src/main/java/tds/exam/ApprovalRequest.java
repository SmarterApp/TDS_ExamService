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

    @NotNull
    private String clientName;

    public ApprovalRequest(UUID examId, UUID sessionId, UUID browserId, String clientName) {
        this.examId = examId;
        this.sessionId = sessionId;
        this.browserId = browserId;
        this.clientName = clientName;
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

    /**
     * @return The name of  the client that owns the exam.
     * <p>
     *     Examples include "SBAC" and "SBAC_PT".
     * </p>
     */
    public String getClientName() {
        return clientName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ApprovalRequest)) return false;

        ApprovalRequest that = (ApprovalRequest) o;

        if (!getExamId().equals(that.getExamId())) return false;
        if (!getSessionId().equals(that.getSessionId())) return false;
        if (!getBrowserId().equals(that.getBrowserId())) return false;
        return getClientName().equals(that.getClientName());
    }

    @Override
    public int hashCode() {
        int result = getExamId().hashCode();
        result = 31 * result + getSessionId().hashCode();
        result = 31 * result + getBrowserId().hashCode();
        result = 31 * result + getClientName().hashCode();
        return result;
    }
}
