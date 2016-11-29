package tds.exam.repositories;

import java.util.List;

import tds.exam.ExamAccommodation;

/**
 * Processes data modification calls to for {@link tds.exam.ExamAccommodation}
 */
public interface ExamAccommodationCommandRepository {
    /**
     * Inserts exam accommodations for the exam
     *
     * @param accommodations list of {@link tds.exam.ExamAccommodation} to insert
     */
    void insert(List<ExamAccommodation> accommodations);

    /**
     * Updates the exam accommodations for the exam
     *
     * @param accommodation {@link tds.exam.ExamAccommodation} to update
     */
    void update(ExamAccommodation accommodation);
}
