package tds.exam;

/**
 * Describe the approval of a request to start an {@link Exam}.
 */
public class ExamApproval {
    private ExamApprovalStatus examApprovalStatus;
    private String comment;

    // TODO:  Add collection of approved accommondations
    public ExamApproval() {}

    public ExamApproval(ExamApprovalStatus examApprovalStatus, String comment) {
        this.examApprovalStatus = examApprovalStatus;
        this.comment = comment;
    }

    /**
     * @return The status of the approval request.
     */
    public ExamApprovalStatus getExamApprovalStatus() {
        return examApprovalStatus;
    }

    public void setExamApprovalStatus(ExamApprovalStatus examApprovalStatus) {
        this.examApprovalStatus = examApprovalStatus;
    }

    /**
     * @return A comment/reason for the approval denial.
     */
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
