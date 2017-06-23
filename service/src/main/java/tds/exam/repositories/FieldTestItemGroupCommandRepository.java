/***************************************************************************************************
 * Copyright 2017 Regents of the University of California. Licensed under the Educational
 * Community License, Version 2.0 (the “license”); you may not use this file except in
 * compliance with the License. You may obtain a copy of the license at
 *
 * https://opensource.org/licenses/ECL-2.0
 *
 * Unless required under applicable law or agreed to in writing, software distributed under the
 * License is distributed in an “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for specific language governing permissions
 * and limitations under the license.
 **************************************************************************************************/

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
    void insert(final List<FieldTestItemGroup> fieldTestItemGroups);

    /**
     * Update a collection of {@link tds.exam.models.FieldTestItemGroup}s
     *
     * @param fieldTestItemGroups The {@link tds.exam.models.FieldTestItemGroup}s to update
     */
    void update(final FieldTestItemGroup... fieldTestItemGroups);
}
