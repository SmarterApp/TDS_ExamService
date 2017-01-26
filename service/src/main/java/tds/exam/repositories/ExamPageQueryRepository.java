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
     * Fetch a collection of {@link tds.exam.ExamItem}s for the specified exam page.
     *
     * @param examId   the id of the {@link tds.exam.Exam} that the {@link tds.exam.ExamPage}s correspond to
     * @param position the position number
     * @return A collection of {@link tds.exam.ExamItem}s for the specified exam page.
     */
    Optional<ExamPage> findPageWithItems(UUID examId, int position);
}
