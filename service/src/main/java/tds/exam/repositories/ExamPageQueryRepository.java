package tds.exam.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import tds.exam.ExamPage;

/**
 * Handles data reads from the exam_page tables
 */
public interface ExamPageQueryRepository {
    /**
     * Fetches all non-deleted {@link tds.exam.ExamPage}s for an exam
     *
     * @param examId the id of the {@link tds.exam.Exam} that the {@link tds.exam.ExamPage}s correspond to
     * @return A collection of active {@link tds.exam.ExamPage}s for the specified exam
     */
    List<ExamPage> findAll(UUID examId);

    /**
     * Fetch a single {@link tds.exam.ExamPage}
     *
     * @param examId   The id of the {@link tds.exam.Exam} that the {@link tds.exam.ExamPage} corresponds to
     * @param position the position number
     * @return An {@link tds.exam.ExamPage} for the specified {@link tds.exam.Exam} id and page position
     */
    Optional<ExamPage> find(UUID examId, int position);

    /**
     * Fetch an {@link tds.exam.ExamPage} with its collection of {@link tds.exam.ExamItem}s.
     *
     * @param examId   the id of the {@link tds.exam.Exam} that the {@link tds.exam.ExamPage} corresponds to
     * @param position the position number
     * @return An {@link tds.exam.ExamPage} with its collection of {@link tds.exam.ExamItem}s for the specified exam
     * page.
     */
    Optional<ExamPage> findPageWithItems(UUID examId, int position);
}
