package tds.exam.services;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import tds.exam.ExpandableExam;
import tds.exam.ExpandableExamAttributes;

/**
 * Handles operations on {@link tds.exam.ExpandableExam}
 */
public interface ExpandableExamService {
    /**
     * Returns a list of all {@link tds.exam.ExpandableExam}s within a session. The expandable exam contains
     * additional optional exam data.
     *
     * @param sessionId        the id of the session the {@link tds.exam.Exam}s belong to
     * @param expandableAttributes parameters representing the optional expandable data to include
     * @return a list of {@link tds.exam.ExpandableExam}s in the session
     */
    List<ExpandableExam> findExamsBySessionId(final UUID sessionId, final Set<String> invalidStatuses, final ExpandableExamAttributes... expandableAttributes);

    /**
     * Fetches an {@link tds.exam.ExpandableExam} for the given id (if one exists) with additional properties based on the
     * {@link tds.exam.ExpandableExamAttributes} that are requested.
     *
     * @param examId           The id of the {@link tds.exam.ExpandableExam} to fetch
     * @param expandableAttributes parameters representing the optional expandable data to include
     * @return The {@link tds.exam.ExpandableExam} containing additional data
     */
    Optional<ExpandableExam> findExam(final UUID examId, final ExpandableExamAttributes... expandableAttributes);
}
