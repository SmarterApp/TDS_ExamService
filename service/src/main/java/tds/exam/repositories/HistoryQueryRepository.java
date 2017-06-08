package tds.exam.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import tds.exam.models.ItemGroupHistory;

/**
 * Repository for querying the history table.
 */
public interface HistoryQueryRepository {
    /**
     * Retrieves the maximum ability value from the history table for the specified subject, client, and student.
     *
     * @param clientName Name of the SBAC client
     * @param subject    Subject name (typically ELA or MATH)
     * @param studentId  Id of the student to obtain the ability for
     * @return Ability if found otherwise empty
     */
    Optional<Double> findAbilityFromHistoryForSubjectAndStudent(final String clientName, final String subject, Long studentId);

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
