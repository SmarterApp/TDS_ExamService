package tds.exam.services;

import java.util.UUID;

import tds.common.Response;
import tds.exam.ApprovalRequest;
import tds.exam.ExamItemResponse;

/**
 * A service for interacting with an {@link tds.exam.Exam}'s {@link tds.exam.ExamItem}s and their associated
 * {@link tds.exam.ExamItemResponse}s.
 */
public interface ExamItemService {
    /**
     * Persist one or more {@link tds.exam.ExamItemResponse}s
     *
     * @param request The data required to verify the requester can fetch the requested page
     * @param responses The collection of the {@link tds.exam.ExamItemResponse}s to persist
     * @return
     */
    Response<String> insertResponses(ApprovalRequest request, ExamItemResponse... responses);


    /**
     * Fetches the highest exam position - the position of the {@link tds.exam.ExamItem} that
     * was last responded to by a student.
     *
     * @param examId the id of the {@link tds.exam.Exam}
     * @return the position of the last {@link tds.exam.ExamItem} responded to by a student
     */
    int getExamPosition(UUID examId);
}
