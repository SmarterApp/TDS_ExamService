package tds.exam.services;

import tds.exam.ExamItemResponse;
import tds.exam.ExamItemResponseScore;

/**
 * Interface for scoring {@link tds.exam.ExamItemResponse}s
 */
public interface ExamItemResponseScoringService {
    ExamItemResponseScore getScore(ExamItemResponse response);
}
