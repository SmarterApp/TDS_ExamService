package tds.exam.repositories;

import tds.exam.ExamItemResponse;

/**
 * Handles data modification to the exam_item_response tables
 */
public interface ExamItemResponseCommandRepository {
    /**
     * Insert a collection of {@link tds.exam.ExamItemResponse}s.
     *
     * @param responses The collection of {@link tds.exam.ExamItemResponse}s to insert
     */
    void insertResponses(ExamItemResponse... responses);
}
