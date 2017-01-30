package tds.exam.repositories;

import tds.exam.ExamItem;

/**
 * Handles data modification for the exam_page table
 */
public interface ExamItemCommandRepository {
    /**
     * Insert a collection of {@link tds.exam.ExamItem}s.
     *
     * @param examItems The collection of {@link tds.exam.ExamItem}s to insert
     */
    void insert(ExamItem... examItems);
}
