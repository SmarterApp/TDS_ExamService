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
    List<FieldTestItemGroup> find(final UUID examId, final String segmentKey);

    /**
     * Find all {@link tds.exam.models.FieldTestItemGroup}s that were delivered/administered in an exam.
     *
     * @param examId The id of the {@link tds.exam.Exam} to fetch item groups by
     * @return The list of all {@link tds.exam.models.FieldTestItemGroup}s in the specified exam
     */
    List<FieldTestItemGroup> findUsageInExam(final UUID examId);
}
