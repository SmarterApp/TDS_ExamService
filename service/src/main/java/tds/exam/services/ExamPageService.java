package tds.exam.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import tds.common.Response;
import tds.exam.ExamPage;

/**
 * Service for interacting with exam pages
 */
public interface ExamPageService {
    /**
     * Inserts a {@link java.util.List} of {@link tds.exam.ExamPage}s
     *
     * @param examPages A collection of {@link tds.exam.ExamPage}s to insert
     */
    void insertPages(ExamPage... examPages);

    /**
     * Marks all {@link tds.exam.ExamPage}s as "deleted" for the exam.
     *
     * @param examId the id of the {@link tds.exam.Exam}
     */
    void deletePages(final UUID examId);

    /**
     * Fetches a list of all {@link tds.exam.ExamPage}s for an exam.
     *
     * @param examId the id of the {@link tds.exam.Exam}
     * @return A collection of all {@link tds.exam.ExamPage}s for the specified exam Id
     */
    List<ExamPage> findAllPages(final UUID examId);

    /**
     * Fetches a list of all {@link tds.exam.ExamPage}s for an exam.  Each {@link tds.exam.ExamPage} will have its
     * collection of {@link tds.exam.ExamItem}s.
     *
     * @param examId the id of the {@link tds.exam.Exam}
     * @return A collection of all {@link tds.exam.ExamPage}s for the specified exam Id
     */
    List<ExamPage> findAllPagesWithItems(final UUID examId);

    /**
     * Fetch an {@link tds.exam.ExamPage} for the specified {@link tds.exam.Exam} id and page number.
     *
     * @param examId     The exam ID
     * @param pageNumber The page number (1-based) of the page to return
     * @return An {@link tds.exam.ExamPage} containing a collection of {@link tds.exam.ExamItem}s that
     * should be displayed
     */
    Response<ExamPage> getPage(final UUID examId, final int pageNumber);

    /**
     * Finds an {@link tds.exam.ExamPage} by its id
     *
     * @param id the exam page id
     * @return {@link tds.exam.ExamPage} if found otherwise empty
     */
    Optional<ExamPage> find(final UUID id);

    /**
     * Updates the exam page
     *
     * @param examPage exam pages to update
     */
    void update(ExamPage... examPage);
}
