package tds.exam.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import tds.exam.models.ExamPage;
import tds.exam.models.ExamResponse;

/**
 * Created by emunoz on 12/28/16.
 */
public interface ExamItemService {
    /**
     * Inserts a {@link java.util.List} of {@link tds.exam.models.ExamPage}s
     *
     * @param examPage
     */
    void insertPages(List<ExamPage> examPage);

    /**
     * Marks all {@link tds.exam.models.ExamPage}s as "deleted" for the exam.
     *
     * @param examId the id of the {@link tds.exam.Exam}
     */
    void deletePages(UUID examId);

    /**
     * Fetches the highest exam position - the position of the {@link tds.exam.models.ExamItem} that
     * was last responded to by a student.
     *
     * @param examId the id of the {@link tds.exam.Exam}
     * @return the position of the last {@link tds.exam.models.ExamItem} responded to by a student
     */
    int getExamPosition(UUID examId);

    /**
     * Fetches a list of all {@link tds.exam.models.ExamPage}s for an exam.
     *
     * @param examId the id of the {@link tds.exam.Exam}
     * @return
     */
    List<ExamPage> findAllPages(UUID examId);
}
