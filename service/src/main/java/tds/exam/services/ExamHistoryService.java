package tds.exam.services;

import java.util.List;
import java.util.UUID;

import tds.exam.models.ItemGroupHistory;

public interface ExamHistoryService {
    /**
     * Finds all the previous {@link tds.exam.models.ItemGroupHistory} for exams
     *
     * @param studentId      the student id
     * @param excludedExamId the exam id to exclude in the item group history
     * @param assessmentId   the assessment id
     * @return list of {@link tds.exam.models.ItemGroupHistory}
     */
    List<ItemGroupHistory> findPreviousItemGroups(final long studentId, final UUID excludedExamId, final String assessmentId);
}
