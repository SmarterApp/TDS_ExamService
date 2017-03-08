package tds.exam.repositories;

import java.util.List;
import java.util.UUID;

import tds.exam.ExamAccommodation;

/**
 * Interface for reading {@link tds.exam.ExamAccommodation} data for an {@link tds.exam.Exam}.
 */
public interface ExamAccommodationQueryRepository {
    /**
     * Retrieve a list of {@link tds.exam.ExamAccommodation}s for the specified exam id and a collection of accommodation types.
     *
     * @param examId             The id of the {@link tds.exam.Exam} that owns the {@link tds.exam.ExamAccommodation}s
     * @param segmentKey         The key of the segment to which the {@link tds.exam.ExamAccommodation}s apply
     * @param accommodationTypes a list of types of {@link tds.exam.ExamAccommodation}s to find
     * @return A list of {@link tds.exam.ExamAccommodation}s that correspond to the specified accommodation types
     */
    List<ExamAccommodation> findAccommodations(final UUID examId, final String segmentKey, final String... accommodationTypes);

    /**
     * Retrieves all the accommodations associated with an exam
     *
     * @param examIds the exam ids to find exam accommodations for
     * @return list of {@link tds.exam.ExamAccommodation}
     */
    List<ExamAccommodation> findAccommodations(final UUID... examIds);

    /**
     * Retrieves all the approved {@link tds.exam.ExamAccommodation} associated with the exam
     *
     * @param examIds the unique ids for the exams
     * @return list containing approved {@link tds.exam.ExamAccommodation}
     */
    List<ExamAccommodation> findApprovedAccommodations(final UUID... examIds);
}
