package tds.exam.services;

import java.util.Optional;
import java.util.UUID;

import tds.common.Response;
import tds.config.ClientTestProperty;
import tds.exam.Exam;
import tds.exam.ExamApproval;
import tds.exam.ExamApprovalRequest;
import tds.exam.OpenExamRequest;

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

    Response<Exam> openExam(OpenExamRequest openExamRequest);

    /**
     * Get approval for the open exam request.
     * <p>
     *     This method is called in a loop by the Student application while waiting for the Proctor to approve or deny
     *     the Student's request to start his/her exam.
     * </p>
     *
     * @param examApprovalRequest The {@link ExamApprovalRequest} representing the request to open the specified exam.
     * @return {@link ExamApproval} describing whether the exam is approved to be opened.
     */
    Response<ExamApproval> getApproval(ExamApprovalRequest examApprovalRequest);

    /**
     * Retrieves the initial ability value for an {@link Exam}.
     *
     * @param exam      the exam to retrieve an ability for.
     * @param clientTestProperty  properties object for the exam.
     * @return  the initial ability for an {@link Exam}.
     */
    Optional<Double> getInitialAbility(Exam exam, ClientTestProperty clientTestProperty);
}
