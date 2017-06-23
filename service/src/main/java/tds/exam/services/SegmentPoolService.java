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
import java.util.UUID;

import tds.assessment.ItemConstraint;
import tds.assessment.Segment;
import tds.exam.Exam;
import tds.exam.models.SegmentPoolInfo;

/**
 * Service responsible for selecting segment items and gathering segment pool information.
 */
public interface SegmentPoolService {

    /**
     *  A {@link tds.exam.models.SegmentPoolInfo} object containing metadata about the selected segment pool.
     *
     * @param examId        The id of the {@link Exam}
     * @param segment       The segment being constructed
     * @return The {@link tds.exam.models.SegmentPoolInfo} containing segment pool information
     */
    SegmentPoolInfo computeSegmentPool(final UUID examId, final Segment segment, final List<ItemConstraint> itemConstraints, final String languageCode);
}
