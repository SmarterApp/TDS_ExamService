package tds.exam.repositories;

import java.util.List;
import java.util.UUID;

import tds.exam.models.FieldTestItemGroup;

/**
 * Repository for reading from the field_test_item_group and field_test_item_group_event table.
 */
public interface FieldTestItemGroupQueryRepository {

    /**
     * Finds all {@link tds.exam.models.FieldTestItemGroup}s for an exam and segment.
     *
     * @param examId     The id of the {@link tds.exam.Exam} to fetch item groups by
     * @param segmentKey The id of the {@link tds.assessment.Segment} to fetch item groups by
     * @return The list of {@link tds.exam.models.FieldTestItemGroup}s fetched
     */
    List<FieldTestItemGroup> find(UUID examId, String segmentKey);
}
