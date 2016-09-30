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
    private UUID browserKey;

    @NotNull
    private String clientName;

    /**
     * @return The id of the exam for which approval is being requested
     */
    public UUID getExamId() {
        return examId;
    }

    public void setExamId(UUID examId) {
        this.examId = examId;
    }

    /**
     * @return The id of the session that hosts the exam
     */
    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * @return The id of the user's browser
     */
    public UUID getBrowserKey() {
        return browserKey;
    }

    public void setBrowserKey(UUID browserKey) {
        this.browserKey = browserKey;
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

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }
}
