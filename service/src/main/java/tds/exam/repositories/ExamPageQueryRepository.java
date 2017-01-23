package tds.exam.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import tds.exam.models.ExamPage;

/**
 * Handles data reads from the exam_page tables
 */
public interface ExamPageQueryRepository {
    /**
     * Fetches all non-deleted {@link tds.exam.models.ExamPage}s for an exam
     *
     * @param examId the id of the {@link tds.exam.Exam} that the {@link tds.exam.models.ExamPage}s correspond to
     * @return A collection of active {@link tds.exam.models.ExamPage}s for the specified exam
     */
    List<ExamPage> findAll(UUID examId);

    /**
     * @param examId   the id of the {@link tds.exam.Exam} that the {@link tds.exam.models.ExamPage}s correspond to
     * @param position the position of the {@link tds.exam.models.ExamPage} to find
     * @return The {@link tds.exam.models.ExamPage} for the specified exam id and position
     */
    Optional<ExamPage> find(UUID examId, int position);

    /**
     * Fetch a collection of {@link tds.exam.models.ExamItem}s for the specified exam page.
     *
     * @param examId   the id of the {@link tds.exam.Exam} that the {@link tds.exam.models.ExamPage}s correspond to
     * @param position the position number
     * @return A collection of {@link tds.exam.models.ExamItem}s for the specified exam page.
     */
    Optional<ExamPage> findPageWithItems(UUID examId, int position);
}
