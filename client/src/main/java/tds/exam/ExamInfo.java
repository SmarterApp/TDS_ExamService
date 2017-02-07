package tds.exam;

import javax.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Request approval to an {@link Exam}.
 */
public class ExamInfo {
    @NotNull
    private UUID examId;

    @NotNull
    private UUID sessionId;

    @NotNull
    private UUID browserId;

    public ExamInfo(UUID examId, UUID sessionId, UUID browserId) {
        this.examId = examId;
        this.sessionId = sessionId;
        this.browserId = browserId;
    }

    /**
     * Private constructor for frameworks
     */
    private ExamInfo() {
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
        if (!(o instanceof ExamInfo)) return false;

        ExamInfo that = (ExamInfo) o;

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
