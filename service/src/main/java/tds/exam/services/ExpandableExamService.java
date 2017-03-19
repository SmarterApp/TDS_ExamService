package tds.exam.services;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import tds.exam.ExpandableExam;

/**
 * Handles operations on {@link tds.exam.ExpandableExam}
 */
public interface ExpandableExamService {
    /**
     * Returns a list of all {@link tds.exam.ExpandableExam}s within a session. The expandable exam contains
     * additional optional exam data.
     *
     * @param sessionId        the id of the session the {@link tds.exam.Exam}s belong to
     * @param expandableParams a param representing the optional expandable data to include
     * @return a list of {@link tds.exam.ExpandableExam}s in the session
     */
    List<ExpandableExam> findExamsBySessionId(final UUID sessionId, final Set<String> invalidStatuses, final String... expandableParams);
}
