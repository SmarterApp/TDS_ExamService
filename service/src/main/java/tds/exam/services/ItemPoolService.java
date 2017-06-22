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
import java.util.UUID;

import tds.assessment.Item;
import tds.assessment.ItemConstraint;

/**
 * A service used for selecting items for exam segments.
 */
public interface ItemPoolService {
    /**
     * Retrieves a collection of eligible items for the exam segment based on exam accommodations and assessment
     * item constraints.
     *
     * @param examId    the id of the {@link tds.exam.Exam}
     * @param itemConstraints    the {@link tds.assessment.ItemConstraint}s for the assessment
     * @param items     the collection of all possible {@link tds.assessment.Item}s in a {@link tds.assessment.Segment}
     * @return  returns a filtered list of {@link tds.assessment.Item}s eligible for the segment pool
     */
    Set<Item> getItemPool(final UUID examId, final List<ItemConstraint> itemConstraints, final List<Item> items);

    /**
     * Retrieves a collection of eligible items for the exam segment based on exam accommodations and assessment
     * item constraints.
     *
     * @param examId          the id of the {@link tds.exam.Exam}
     * @param itemConstraints the {@link tds.assessment.ItemConstraint}s for the assessment
     * @param items           the collection of all possible {@link tds.assessment.Item}s in a {@link tds.assessment.Segment}
     * @return returns a filtered list of {@link tds.assessment.Item}s eligible for the segment pool
     */
    Set<Item> getFieldTestItemPool(UUID examId, List<ItemConstraint> itemConstraints, final List<Item> items);
}
