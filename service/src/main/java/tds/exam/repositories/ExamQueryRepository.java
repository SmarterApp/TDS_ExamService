package tds.exam.repositories;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import tds.exam.Exam;
import tds.exam.models.Ability;

/**
 * Data access for exams
 */
public interface ExamQueryRepository {
    /**
     * Retrieves the exam by uniqueKey
     *
     * @param examId exam id
     * @return the {@link tds.exam.Exam Exam} if found otherwise empty
     */
    Optional<Exam> getExamById(UUID examId);

    /**
     * Retrieves the latest exam that isn't ended
     *
     * @param studentId    student id associated with the exam
     * @param assessmentId assessment id for the exam
     * @param clientName   client name for the exam
     * @return the {@link tds.exam.Exam Exam} if found otherwise empty
     */
    Optional<Exam> getLastAvailableExam(long studentId, String assessmentId, String clientName);

    /**
     * Find all {@link tds.exam.Exam}s that belong to a {@link tds.session.Session} so they can be paused.
     *
     * @param sessionId The unique identifier of the session
     * @param statusSet A {@code java.util.Set} of statuses that can transition to "paused"
     * @return A collection of exams that are assigned to the specified session
     */
    List<Exam> findAllExamsInSessionWithStatus(UUID sessionId, Set<String> statusSet);

    /**
     * Retrieves a listing of all ability records for the specified exam and student.
     *
     * @param exam       the exam for which to exclude from the ability query
     * @param clientName client name for the exam
     * @param subject    the subject of the exam
     * @param studentId  the student taking the exam
     * @return a list of {@link Ability} objects for past exams
     */
    List<Ability> findAbilities(UUID exam, String clientName, String subject, Long studentId);
}
