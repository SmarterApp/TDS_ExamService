package tds.exam.repositories;

import java.util.Optional;

/**
 * Repository for querying the history table.
 */
public interface HistoryQueryRepository {
    /**
     * Retrieves the maximum ability value from the history table for the specified subject, client, and student.
     *
     * @param clientName    Name of the SBAC client
     * @param subject       Subject name (typically ELA or MATH)
     * @param studentId     Id of the student to obtain the ability for
     * @return
     */
    Optional<Double> findAbilityFromHistoryForSubjectAndStudent(String clientName, String subject, Long studentId);
}
