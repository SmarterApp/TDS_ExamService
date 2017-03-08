package tds.exam.services;

import tds.exam.ExamItemResponse;
import tds.exam.ExamItemResponseScore;

/**
 * Interface for scoring {@link tds.exam.ExamItemResponse}s
 */
public interface ExamItemResponseScoringService {
    /**
     * Apply scoring logic to an {@link tds.exam.ExamItemResponse}.
     *
     * @param response The {@link tds.exam.ExamItemResponse} that needs to be scored
     * @return An {@link tds.exam.ExamItemResponseScore} describing the score and scoring metadata for this
     * {@link tds.exam.ExamItemResponse}
     */
    ExamItemResponseScore getScore(final ExamItemResponse response);
}
