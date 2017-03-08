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
    void insert(final List<ExamAccommodation> accommodations);

    /**
     * Updates the exam accommodations for the exam
     *
     * @param accommodation {@link tds.exam.ExamAccommodation} to update
     */
    void update(final ExamAccommodation... accommodation);

    /**
     * Deletes the exam accommodations for the exam
     *
     * @param accommodations {@link tds.exam.ExamAccommodation} to delete
     */
    void delete(final List<ExamAccommodation> accommodations);
}
