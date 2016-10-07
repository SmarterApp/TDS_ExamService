package tds.exam;

import javax.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Request approval to start an {@link Exam}.
 */
public class ExamApprovalRequest {
    @NotNull
    private UUID examId;

    @NotNull
    private UUID sessionId;

    @NotNull
    private UUID browserId;

    @NotNull
    private String clientName;

    public ExamApprovalRequest(UUID examId, UUID sessionId, UUID browserId, String clientName) {
        this.examId = examId;
        this.sessionId = sessionId;
        this.browserId = browserId;
        this.clientName = clientName;
    }

    /**
     * @return The id of the exam for which approval is being requested
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
}
