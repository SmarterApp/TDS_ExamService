package tds.exam;

import java.util.UUID;

/**
 * Describe the approval of a request to start an {@link Exam}.
 */
public class ExamApproval {
    private UUID examId;
    private ExamApprovalStatus examApprovalStatus;
    private String statusChangeReason;

    public ExamApproval(UUID examId, ExamStatusCode examStatusCode, String statusChangeReason) {
        this.examId = examId;
        this.statusChangeReason = statusChangeReason;
        this.examApprovalStatus = ExamApprovalStatus.fromExamStatus(examStatusCode.getCode());
    }

    /**
     * @return The id of the {@link Exam} for which approval is requested.
     */
    public UUID getExamId() {
        return examId;
    }

    /**
     * @return The status of the exam approval.
     */
    public ExamApprovalStatus getExamApprovalStatus() {
        return examApprovalStatus;
    }

    /**
     * @return The text explaining the reason behind the {@link Exam}'s most recent status change.
     */
    public String getStatusChangeReason() {
        return statusChangeReason;
    }
}
