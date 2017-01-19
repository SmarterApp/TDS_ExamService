package tds.exam.repositories;

import java.util.List;

import tds.exam.models.FieldTestItemGroup;

/**
 * Repository for writing to the field_test_item_group and field_test_item_group_event repositories
 */
public interface FieldTestItemGroupCommandRepository {

    /**
     * Inserts a collection of {@link tds.exam.models.FieldTestItemGroup}s
     *
     * @param fieldTestItemGroups the {@link tds.exam.models.FieldTestItemGroup}s to insert
     */
    void insert(List<FieldTestItemGroup> fieldTestItemGroups);
}
