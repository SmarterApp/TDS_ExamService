package tds.exam.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import tds.exam.ExamSegment;

/**
 * Repository for reading from the exam_segment and exam_segment_event tables.
 */
public interface ExamSegmentQueryRepository {

    /**
     * Retrieves a list of {@link ExamSegment}s for this particular segment.
     *
     * @param examId the exam to retrieve segments for
     * @return the list of {@link ExamSegment}s
     */
    List<ExamSegment> findByExamId(final UUID examId);

    /**
     * Retrieves the {@link ExamSegment} for the given exam and segment position.
     *
     * @param examId          the exam to retrieve the segment for
     * @param segmentPosition the position of the segment in the exam
     * @return the {@link ExamSegment}
     */
    Optional<ExamSegment> findByExamIdAndSegmentPosition(final UUID examId, final int segmentPosition);

    /**
     * Fetches the count of segments that are not satisfied/completed.
     *
     * @param examId the exam to to retrieve exam segment satisfied counts for
     * @return the count of unsatisfied {@link tds.exam.ExamSegment}s for the exam
     */
    int findCountOfUnsatisfiedSegments(final UUID examId);

    /**
     * Finds an {@link tds.exam.ExamSegment} by exam id and segment key
     *
     * @param examId     the exam id
     * @param segmentKey the {@link tds.exam.ExamSegment} segment key
     * @return an {@link tds.exam.ExamSegment} otherwise empty
     */
    Optional<ExamSegment> findByExamIdAndSegmentKey(final UUID examId, final String segmentKey);
}
