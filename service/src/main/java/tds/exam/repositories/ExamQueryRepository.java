package tds.exam.repositories;

import java.util.Optional;
import java.util.UUID;

import tds.exam.Exam;

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
}
