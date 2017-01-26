package tds.exam.repositories;

import java.util.UUID;

/**
 * Handles data reads from the exam_item_response table
 */
public interface ExamResponseQueryRepository {

    /**
     * Gets the item position of the last item that has a response
     *
     * @param examId the id of the {@link tds.exam.Exam} to find the position for
     * @return the item position of the last item responded to
     */
    int getCurrentExamItemPosition(UUID examId);
}