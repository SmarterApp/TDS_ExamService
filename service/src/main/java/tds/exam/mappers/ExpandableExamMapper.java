package tds.exam.mappers;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import tds.exam.ExpandableExam;
import tds.exam.ExpandableExamAttributes;

/**
 * Interface for a service that maps an {@link tds.exam.ExpandableExam} with optional attributes
 */
public interface ExpandableExamMapper {
    /**
     * A method that will update an {@link tds.exam.ExpandableExam} in a session based on the specified exam attributes
     *
     * @param expandableAttributes A set of optional exam attributes
     * @param examBuilders         A mapping of exam Ids to their respective {@link tds.exam.ExpandableExam}s
     * @param sessionId            The session id of the session the {@link tds.exam.ExpandableExam} belongs to
     */
    void updateExpandableMapper(final Set<ExpandableExamAttributes> expandableAttributes, final Map<UUID, ExpandableExam.Builder> examBuilders,
                                final UUID sessionId);
}
