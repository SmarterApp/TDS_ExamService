package tds.exam.services;

import java.util.Map;
import java.util.UUID;

import tds.common.Response;
import tds.exam.ExamInfo;
import tds.exam.ExamItemResponse;
import tds.exam.ExamPage;

/**
 * A service for interacting with an {@link tds.exam.Exam}'s {@link tds.exam.ExamItem}s and their associated
 * {@link tds.exam.ExamItemResponse}s.
 */
public interface ExamItemService {
    /**
     * Persist one or more {@link tds.exam.ExamItemResponse}s
     *
     * @param request                The data required to verify the requester can fetch the requested page
     * @param mostRecentPagePosition The last page number that has responses
     * @param responses              The collection of the {@link tds.exam.ExamItemResponse}s to persist
     * @return The next {@link tds.exam.ExamPage} that has {@link tds.exam.ExamItem}s that require student responses
     */
    Response<ExamPage> insertResponses(final ExamInfo request, final int mostRecentPagePosition, final ExamItemResponse... responses);

    /**
     * Fetches the highest exam position - the position of the {@link tds.exam.ExamItem} that
     * was last responded to by a student.
     *
     * @param examId the id of the {@link tds.exam.Exam}
     * @return the position of the last {@link tds.exam.ExamItem} responded to by a student
     */
    int getExamPosition(final UUID examId);

    /**
     * Fetches a map of examIds for their respective number of items responded to
     *
     * @param examIds the ids of the exams to fetch response counts for
     * @return a mapping of examIds to the number of items that exam has responded to
     */
    Map<UUID, Integer> getResponseCounts(UUID... examIds);
}
