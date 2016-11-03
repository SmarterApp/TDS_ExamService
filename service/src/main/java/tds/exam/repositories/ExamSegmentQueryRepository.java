package tds.exam.repositories;

import tds.exam.models.ExamSegment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for reading from the exam_segment and exam_segment_event tables.
 */
public interface ExamSegmentQueryRepository {

    /**
     * Retrieves a list of {@link ExamSegment}s for this particular segment.
     *
     * @param examId    the exam to retrieve segments for
     * @return the list of {@link ExamSegment}s
     */
    List<ExamSegment> findByExamId(UUID examId);

    /**
     * Retrieves the {@link ExamSegment} for the given exam and segment position.
     *
     * @param examId the exam to retrieve the segment for
     * @param segmentPosition   the position of the segment in the exam
     * @return the {@link ExamSegment}
     */
    Optional<ExamSegment> findByExamIdAndSegmentPosition(UUID examId, int segmentPosition);

}
