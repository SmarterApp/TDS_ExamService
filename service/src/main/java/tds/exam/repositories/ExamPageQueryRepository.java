package tds.exam.repositories;

import java.util.List;
import java.util.UUID;

import tds.exam.models.ExamItem;
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
     * Fetch a collection of {@link tds.exam.models.ExamItem}s for the specified exam page.
     *
     * @param page The {@link tds.exam.models.ExamPage} to fetch items for
     * @return A collection of {@link tds.exam.models.ExamItem}s for the specified exam page.
     */
    List<ExamItem> findAllItemsForExamPage(ExamPage page);
}
