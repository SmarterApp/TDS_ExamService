package tds.exam.repositories;

import java.util.List;
import java.util.UUID;

import tds.exam.ExamPage;

/**
 * Handles data modification for the exam_page table
 */
public interface ExamPageCommandRepository {
    /**
     * Inserts a collection of {@link tds.exam.ExamPage}s
     *
     * @param examPages
     */
    void insert(List<ExamPage> examPages);

    /**
     * Marks all {@link tds.exam.ExamPage}s for the exam as deleted
     *
     * @param examId the id of the {@link tds.exam.Exam}
     */
    void deleteAll(UUID examId);

    /**
     * Update an {@link tds.exam.ExamPage}.
     *
     * @param examPage The {@link tds.exam.ExamPage} to update
     */
    void update(ExamPage examPage);
}
