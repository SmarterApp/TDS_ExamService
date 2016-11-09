package tds.exam.services;

import java.util.Optional;
import java.util.UUID;

import tds.common.Response;
import tds.common.ValidationError;
import tds.config.ClientTestProperty;
import tds.exam.ApprovalRequest;
import tds.exam.Exam;
import tds.exam.ExamApproval;
import tds.exam.OpenExamRequest;
import tds.session.Session;

/**
 * Main entry point for interacting with {@link Exam}
 */
public interface ExamService {

    /**
     * Retrieves an exam based on the UUID
     *
     * @param uuid id for the exam
     * @return {@link Exam} otherwise null
     */
    Optional<Exam> getExam(UUID uuid);

    /**
     * Opens a new exam
     *
     * @param openExamRequest {@link tds.exam.OpenExamRequest}
     * @return {@link tds.common.Response<tds.exam.Exam>} containing exam or errors
     */
    Response<Exam> openExam(OpenExamRequest openExamRequest);

    /**
     * Get approval for the open exam request.
     * <p>
     *     This method is called in a loop by the Student application while waiting for the Proctor to approve or deny
     *     the Student's request to start his/her exam.
     * </p>
     *
     * @param approvalRequest The {@link ApprovalRequest} representing the request to open the specified exam.
     * @return {@link ExamApproval} describing whether the exam is approved to be opened.
     */
    Response<ExamApproval> getApproval(ApprovalRequest approvalRequest);

    /**
     * Retrieves the initial ability value for an {@link Exam}.
     *
     * @param exam      the exam to retrieve an ability for.
     * @param clientTestProperty  properties object for the exam.
     * @return  the initial ability for an {@link Exam}.
     */
    Optional<Double> getInitialAbility(Exam exam, ClientTestProperty clientTestProperty);

    /**
     * Verify all the rules for granting approval to an {@link Exam} are satisfied.
     * <p>
     *     The rules are:
     *     <ul>
     *         <li>The browser key of the approval request must match the browser key of the {@link Exam}.</li>
     *         <li>The session id of the approval request must match the session id of the {@link Exam}.</li>
     *         <li>The {@link Session} must be open (unless the environment is set to "simulation" or "development")</li>
     *         <li>The TA Check-In time window cannot be passed</li>
     *     </ul>
     *     <strong>NOTE:</strong>  If the {@link Session} has no Proctor (because the {@link Session} is a guest session
     *     or is otherwise proctor-less), approval is granted as long as the {@link Session} is open.
     * </p>
     *
     * @param approvalRequest The {@link ApprovalRequest} being evaluated
     * @param exam The {@link Exam} for which approval is being requested
     * @return An empty optional if the approval rules are satisfied; otherwise an optional containing a
     * {@link ValidationError} describing the rule that was not satisfied
     */
    Optional<ValidationError> verifyAccess(ApprovalRequest approvalRequest, Exam exam);
}
