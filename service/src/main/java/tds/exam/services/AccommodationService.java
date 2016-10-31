package tds.exam.services;

import java.util.List;
import java.util.UUID;

import tds.exam.Accommodation;

/**
 * Handles interaction with {@link tds.exam.Accommodation}s associated to an {@link tds.exam.Exam}
 */
public interface AccommodationService {
    /**
     * Find the {@link Accommodation}(s) of the specified types that is/are approved for an {@link tds.exam.Exam}.
     *
     * @param examId The ID of the {@link tds.exam.Exam}
     * @param segmentId The id of the segment to which the {@link Accommodation}s apply
     * @param accommodationTypes The types of {@link Accommodation}s to find
     * @return An {@link Accommodation} if one exists for the specified exam id and accommodation type; otherwise empty
     */
    List<Accommodation> findAccommodations(UUID examId, String segmentId, String[] accommodationTypes);
}
