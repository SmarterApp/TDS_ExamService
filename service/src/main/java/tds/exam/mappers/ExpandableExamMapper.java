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
