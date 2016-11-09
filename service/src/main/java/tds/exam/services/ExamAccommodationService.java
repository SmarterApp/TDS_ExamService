package tds.exam.services;

import java.util.List;
import java.util.UUID;

import tds.exam.ExamAccommodation;

/**
 * Handles interaction with {@link tds.exam.ExamAccommodation}s associated to an {@link tds.exam.Exam}
 */
public interface ExamAccommodationService {
    /**
     * Find the {@link tds.exam.ExamAccommodation}(s) of the specified types that is/are approved for an {@link tds.exam.Exam}.
     *
     * @param examId The ID of the {@link tds.exam.Exam}
     * @param segmentId The id of the segment to which the {@link tds.exam.ExamAccommodation}s apply
     * @param accommodationTypes The types of {@link tds.exam.ExamAccommodation}s to find
     * @return An {@link tds.exam.ExamAccommodation} if one exists for the specified exam id and accommodation type; otherwise empty
     */
    List<ExamAccommodation> findAccommodations(UUID examId, String segmentId, String[] accommodationTypes);
}
