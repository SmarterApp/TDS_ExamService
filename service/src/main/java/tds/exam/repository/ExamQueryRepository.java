package tds.exam.repository;

import tds.exam.Exam;

import java.util.Optional;
import java.util.UUID;

/**
 * Data access for exams
 */
public interface ExamQueryRepository {
    /**
     * Retrieves the exam by uniqueKey
     * @param uniqueKey exam uniqueKey
     * @return the {@link Exam} if found otherwise null
     */
    Optional<Exam> getExamByUniqueKey(UUID uniqueKey);
}
