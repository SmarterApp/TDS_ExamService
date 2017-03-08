package tds.exam.repositories;

import tds.exam.ExamItem;
import tds.exam.ExamItemResponse;

/**
 * Handles data modification for the exam_item and exam_item_response tables
 * <p>
 * Typically, a table describing an entity will have an associated "_event" table to record changes to that entity's
 * state (e.g. {@code exam} for the entity and {@code exam_event} for the changes to the {@link tds.exam.Exam}.
 * {@link tds.exam.ExamItem}s never change after they are created; only the responses made to them can change.  As
 * such, the "_event" equivalent for an {@link tds.exam.ExamItem} is the {@code exam_item_response} table.  Since the
 * {@link tds.exam.ExamItemResponse} entity represents the "event" for an {@link tds.exam.ExamItem}, the methods for
 * storing both entities are contained within this repository.
 * </p>
 */
public interface ExamItemCommandRepository {
    /**
     * Insert a collection of {@link tds.exam.ExamItem}s.
     *
     * @param examItems The collection of {@link tds.exam.ExamItem}s to insert
     */
    void insert(final ExamItem... examItems);

    /**
     * Insert one or more {@link tds.exam.ExamItemResponse}s.
     *
     * @param responses The collection of {@link tds.exam.ExamItemResponse}s to insert
     */
    void insertResponses(final ExamItemResponse... responses);
}
