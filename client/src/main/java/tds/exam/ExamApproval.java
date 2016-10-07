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
        // From the OpportunityStatusExtensions in tds.student.sql.data package of the legacy Student application
        final String APPROVED = "approved";
        final String DENIED = "denied";
        final String PAUSED = "paused";

        this.examId = examId;
        this.statusChangeReason = statusChangeReason;

        // Emulate the ApprovalInfo constructor to get the appropriate approval status
        switch (examStatusCode.getStatus()) {
            case APPROVED:
                this.examApprovalStatus = ExamApprovalStatus.APPROVED;
                break;
            case DENIED:
                this.examApprovalStatus = ExamApprovalStatus.DENIED;
                break;
            case PAUSED:
                this.examApprovalStatus = ExamApprovalStatus.LOGOUT;
                break;
            default:
                this.examApprovalStatus = ExamApprovalStatus.WAITING;
                break;
        }
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
