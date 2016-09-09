package tds.exam.repositories;

import tds.exam.Exam;

import java.util.Optional;
import java.util.UUID;

/**
 * Data access for exams
 */
public interface ExamQueryRepository {
    /**
     * Retrieves the exam by uniqueKey
     * @param examId exam id
     * @return the {@link Exam} if found otherwise null
     */
    Optional<Exam> getExamById(UUID examId);
}
