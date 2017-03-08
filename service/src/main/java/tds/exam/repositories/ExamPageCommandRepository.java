package tds.exam.repositories;

import java.util.UUID;

import tds.exam.ExamPage;

/**
 * Handles data modification for the exam_page table
 */
public interface ExamPageCommandRepository {
    /**
     * Inserts a collection of {@link tds.exam.ExamPage}s
     *
     * @param examPages One or more {@link tds.exam.ExamPage}s to insert
     */
    void insert(final ExamPage... examPages);

    /**
     * Marks all {@link tds.exam.ExamPage}s for the exam as deleted
     *
     * @param examId the id of the {@link tds.exam.Exam}
     */
    void deleteAll(final UUID examId);

    /**
     * Update an {@link tds.exam.ExamPage}.
     *
     * @param examPages One or more {@link tds.exam.ExamPage} to update
     */
    void update(final ExamPage... examPages);
}
