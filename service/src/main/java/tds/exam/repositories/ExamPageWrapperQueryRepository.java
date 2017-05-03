package tds.exam.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import tds.exam.wrapper.ExamPageWrapper;

/**
 * Finds {@link tds.exam.wrapper.ExamPageWrapper}
 */
public interface ExamPageWrapperQueryRepository {
    /**
     * Fetch an {@link tds.exam.wrapper.ExamPageWrapper} with its collection of {@link tds.exam.ExamItem}s.
     *
     * @param examId   the id of the {@link tds.exam.Exam} that the {@link tds.exam.ExamPage} corresponds to
     * @param position the position number
     * @return An {@link tds.exam.ExamPage} with its collection of {@link tds.exam.ExamItem}s for the specified exam
     * page.
     */
    Optional<ExamPageWrapper> findPageWithItems(final UUID examId, final int position);

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
