package tds.exam;

/**
 * Enumerate the possible status values for exam approval.
 * <p>
 *     This enumeration maps to the {@code OpportunityApprovalStatus} contained within the
 *     {@code tds.student.services.data.ApprovalInfo} class of the legacy Student application.
 * </p>
 */
public enum ExamApprovalStatus {
    WAITING,
    APPROVED,
    DENIED,
    LOGOUT
}
