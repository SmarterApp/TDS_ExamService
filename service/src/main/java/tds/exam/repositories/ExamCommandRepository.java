package tds.exam.repositories;

import java.util.List;

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

    /**
     * Update a collection of {@link tds.exam.Exam}s
     * @param exams The collection of exams to update
     */
    void update(List<Exam> exams);
}
