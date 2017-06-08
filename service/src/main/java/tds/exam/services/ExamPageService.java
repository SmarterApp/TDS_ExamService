package tds.exam.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
