package tds.exam.services;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import tds.assessment.Assessment;
import tds.common.Response;
import tds.common.ValidationError;
import tds.exam.Exam;
import tds.exam.ExamConfiguration;
import tds.exam.ExamStatusCode;
import tds.exam.ExpandableExam;
import tds.exam.OpenExamRequest;
import tds.exam.SegmentApprovalRequest;

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
    Optional<Exam> findExam(final UUID uuid);

    /**
     * Opens a new exam
     *
     * @param openExamRequest {@link tds.exam.OpenExamRequest}
     * @return {@link tds.common.Response<tds.exam.Exam>} containing exam or errors
     */
    Response<Exam> openExam(final OpenExamRequest openExamRequest);

    /**
     * Starts a new or existing exam.
     *
     * @param examId The exam to start
     * @return {@link tds.common.Response<tds.exam.Exam>} containing the exam's configuration or errors.
     */
    Response<ExamConfiguration> startExam(final UUID examId);

    /**
     * Retrieves the initial ability value for an {@link Exam}.
     *
     * @param exam       the exam to retrieve an ability for.
     * @param assessment the {@link tds.assessment.Assessment} associated with the exam
     * @return the initial ability for an {@link Exam}.
     */
    Optional<Double> getInitialAbility(final Exam exam, final Assessment assessment);

    /**
     * Change the {@link tds.exam.Exam}'s status to a new status.
     *
     * @param examId             The id of the exam whose status is being changed
     * @param newStatus          The {@link tds.exam.ExamStatusCode} to transition to
     * @param statusChangeReason The reason why the {@link tds.exam.Exam} status is being updated
     * @return {@code Optional<ValidationError>} if the {@link tds.exam.Exam} cannot be updated from its current status
     * to the new status; otherwise {@code Optional.empty()}.
     */
    Optional<ValidationError> updateExamStatus(final UUID examId, final ExamStatusCode newStatus, final String statusChangeReason);

    /**
     * Change the {@link tds.exam.Exam}'s status to a new status.
     *
     * @param examId    The id of the exam whose status is being changed
     * @param newStatus The {@link tds.exam.ExamStatusCode} to transition to
     * @return {@code Optional<ValidationError>} if the {@link tds.exam.Exam} cannot be updated from its current status
     * to the new status; otherwise {@code Optional.empty()}.
     */
    Optional<ValidationError> updateExamStatus(final UUID examId, final ExamStatusCode newStatus);

    /**
     * Update the status of all {@link tds.exam.Exam}s in the specified {@link tds.session.Session} to "paused"
     *
     * @param sessionId The unique identifier of the session that has been closed
     */
    void pauseAllExamsInSession(final UUID sessionId);

    /**
     * Returns a list of all {@link tds.exam.ExpandableExam}s within a session. The expandable exam contains
     * additional optional exam data.
     *
     * @param sessionId        the id of the session the {@link tds.exam.Exam}s belong to
     * @param expandableParams a param representing the optional expandable data to include
     * @return a list of {@link tds.exam.ExpandableExam}s in the session
     */
    List<ExpandableExam> findExamsBySessionId(final UUID sessionId, final Set<String> invalidStatuses, final String... expandableParams);

    /**
     * Performs exam access validation and updates the {@link Exam} status to wait for segment approval.
     *
     * @param examId The id of the exam seeking segment approval
     * @param request A request object containing data related to the segment approval request
     * @return {@code Optional<ValidationError>} if the {@link tds.exam.Exam} cannot be updated from its current status
     * to the new status of if the approval request fails.
     */
    Optional<ValidationError> waitForSegmentApproval(UUID examId, SegmentApprovalRequest request);
}
