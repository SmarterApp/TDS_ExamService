package tds.exam.repository;

import tds.exam.Exam;

import java.util.UUID;

/**
 * Data access for exams
 */
public interface ExamRepository {
    /**
     * Retrieves the exam by id
     * @param id exam id
     * @return the {@link Exam} if found otherwise null
     */
    Exam getExamById(UUID id);
}
