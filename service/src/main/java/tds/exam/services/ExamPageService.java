package tds.exam.services;

import java.util.List;
import java.util.UUID;

import tds.exam.models.ExamPage;

/**
 * Service for interacting with exam items, pages, and responses
 */
public interface ExamPageService {
    /**
     * Inserts a {@link java.util.List} of {@link tds.exam.models.ExamPage}s
     *
     * @param examPage A collection of {@link tds.exam.models.ExamPage} to insert
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
     * @return A collection of all {@link tds.exam.models.ExamPage}s for the specified exam Id
     */
    List<ExamPage> findAllPages(UUID examId);

    /**
     * Fetch an {@link tds.exam.models.ExamPage} for the specified {@link tds.exam.Exam} id and page number.
     *
     * @param examId The unique identifier of the {@link tds.exam.Exam}
     * @param pageNumber The page number (1-based) of the page to return
     * @return An {@link tds.exam.models.ExamPage} containing a collection of {@link tds.exam.models.ExamItem}s that
     * should be displayed
     */
    ExamPage getPage(UUID examId, int pageNumber);
}
