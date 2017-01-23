package tds.exam.repositories;

import tds.exam.models.ExamItem;
import tds.exam.models.ExamItemResponse;

/**
 * Handles data modification for the exam_page table
 */
public interface ExamItemCommandRepository {
    /**
     * Insert a collection of {@link tds.exam.models.ExamItem}s.
     *
     * @param examItems The collection of {@link tds.exam.models.ExamItem}s to insert
     */
    void insert(ExamItem... examItems);

    /**
     * Insert a collection of {@link tds.exam.models.ExamItemResponse}s.
     *
     * @param responses The collection of {@link tds.exam.models.ExamItemResponse}s to insert
     */
    void insertResponses(ExamItemResponse... responses);
}
