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

package tds.exam.services;

import java.util.List;
import java.util.Set;

import tds.assessment.Assessment;
import tds.assessment.Segment;
import tds.exam.Exam;
import tds.exam.models.FieldTestItemGroup;

/**
 * An service for selecting field test item groups
 */
public interface FieldTestItemGroupSelector {
    /**
     * This method returns a list
     *
     * @param exam             the {@link tds.exam.Exam} to select {@link tds.exam.models.FieldTestItemGroup}s for
     * @param assignedGroupIds a {@link java.util.Set} of group ids that have already been assigned for this {@link tds.exam.Exam}.
     * @param assessment       the {@link tds.assessment.Assessment} to select {@link tds.exam.models.FieldTestItemGroup}s for
     * @param currentSegment       the {@link tds.assessment.Segment} to select {@link tds.exam.models.FieldTestItemGroup}s for
     * @param numItems         the number of items to select
     * @return the {@link java.util.List} of the selected {@link tds.exam.models.FieldTestItemGroup}s
     */
    List<FieldTestItemGroup> selectLeastUsedItemGroups(final Exam exam, final Set<String> assignedGroupIds, final Assessment assessment, final Segment currentSegment, final int numItems);
}
