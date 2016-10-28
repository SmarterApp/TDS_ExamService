package tds.exam.repositories;

import java.util.List;
import java.util.UUID;

import tds.exam.Accommodation;

/**
 * Interface for reading {@link Accommodation} data for an {@link tds.exam.Exam}.
 */
public interface AccommodationQueryRepository {
    /**
     * Retrieve a list of {@link Accommodation}s for the specified exam id and a collection of accommodation types.
     *
     * @param examId The id of the {@link tds.exam.Exam} that owns the {@link Accommodation}s
     * @param segmentId The id of the segment to which the {@link Accommodation}s apply
     * @param accommodationTypes a list of types of {@link Accommodation}s to find
     * @return A list of {@link Accommodation}s that correspond to the specified accommodation types
     */
    List<Accommodation> findAccommodations(UUID examId, String segmentId, String[] accommodationTypes);
}
