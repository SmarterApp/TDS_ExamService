package tds.exam.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import tds.exam.ExamPage;
import tds.exam.wrapper.ExamPageWrapper;

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

    /**
     * Fetch an {@link tds.exam.wrapper.ExamPageWrapper} with its collection of {@link tds.exam.ExamItem}s.
     *
     * @param examId   the id of the {@link tds.exam.Exam} that the {@link tds.exam.ExamPage} corresponds to
     * @param pagePosition the page position number
     * @return An {@link tds.exam.ExamPage} with its collection of {@link tds.exam.ExamItem}s for the specified exam
     * page.
     */
    Optional<ExamPageWrapper> findPageWithItems(final UUID examId, final int pagePosition);

    /**
     * Finds the exam pages with items for an exam
     *
     * @param examId the exam id
     * @return List of all {@link tds.exam.wrapper.ExamPageWrapper} for an exam id
     */
    List<ExamPageWrapper> findPagesWithItems(final UUID examId);

    /**
     * Finds the exam pages for a particular exam segment
     *
     * @param examId     exam id
     * @param segmentKey the segment key
     * @return List of {@link tds.exam.wrapper.ExamPageWrapper} for the exam segment
     */
    List<ExamPageWrapper> findPagesForExamSegment(final UUID examId, final String segmentKey);
}
