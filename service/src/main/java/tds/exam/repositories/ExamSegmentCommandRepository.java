package tds.exam.repositories;

import tds.exam.models.ExamSegment;

/**
 * Repository for writing to the exam_segment and exam_segment_event tables.
 */
public interface ExamSegmentCommandRepository {

    /**
     * Inserts an {@link ExamSegment} into the exam_segment table.
     *
     * @param segment the segment to insert
     */
    void insert(final ExamSegment segment);

    /**
     * Inserts an exam segment event into the exam_segment_event table.
     *
     * @param segment the segment to update
     */
    void update(final ExamSegment segment);
}
