package tds.exam.repositories;

import java.util.List;

import tds.exam.models.ExamSegment;

/**
 * Repository for writing to the exam_segment and exam_segment_event tables.
 */
public interface ExamSegmentCommandRepository {

    /**
     * Inserts a list of {@link ExamSegment}s into the exam_segment table.
     *
     * @param segment the segment to insert
     */
    void insert(List<ExamSegment> segment);

    /**
     * Inserts an exam segment event into the exam_segment_event table.
     *
     * @param segment the segment to update
     */
    void update(ExamSegment segment);

    /**
     * Inserts a list of exam segment events into the exam_segment_event table.
     *
     * @param segment the segment to update
     */
    void update(List<ExamSegment> segment);
}
