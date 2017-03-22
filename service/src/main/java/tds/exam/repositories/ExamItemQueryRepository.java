package tds.exam.repositories;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import tds.exam.ExamItem;

/**
 * Handles data reads from the exam_item_response table
 */
public interface ExamItemQueryRepository {

    /**
     * Gets the item position of the last item that has a response
     *
     * @param examId the id of the {@link tds.exam.Exam} to find the position for
     * @return the item position of the last item responded to
     */

    int getCurrentExamItemPosition(final UUID examId);

    /**
     * Fetches a map of examIds for their respective number of items responded to
     *
     * @param examIds the ids of the exams to fetch response counts for
     * @return a mapping of examIds to the number of items that exam has responded to
     */
    Map<UUID, Integer> getResponseCounts(final UUID... examIds);

    /**
     * Fetches Exam Item and Response based on exam id and position
     *
     * @param examId   the exam UUID
     * @param position the item position
     * @return ExamItem if found otherwise empty.  ExamItemResponse will be empty if a response is not present
     */
    Optional<ExamItem> findExamItemAndResponse(final UUID examId, int position);
}