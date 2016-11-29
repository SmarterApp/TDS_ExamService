package tds.exam.repositories;

import tds.exam.Exam;

/**
 * Handles data modification in the exam related tables
 */
public interface ExamCommandRepository {
    /**
     * Saves the {@link tds.exam.Exam}
     *
     * @param exam a non null {@link tds.exam.Exam}
     */
    void insert(Exam exam);

    /**
     * Updates the exam
     *
     * @param exam a non null {@link tds.exam.Exam}
     */
    void update(Exam exam);
}
