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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import tds.exam.ExpandableExam;
import tds.exam.ExpandableExamAttributes;

/**
 * Handles operations on {@link tds.exam.ExpandableExam}
 */
public interface ExpandableExamService {
    /**
     * Returns a list of all {@link tds.exam.ExpandableExam}s within a session. The expandable exam contains
     * additional optional exam data.
     *
     * @param sessionId        the id of the session the {@link tds.exam.Exam}s belong to
     * @param expandableAttributes parameters representing the optional expandable data to include
     * @return a list of {@link tds.exam.ExpandableExam}s in the session
     */
    List<ExpandableExam> findExamsBySessionId(final UUID sessionId, final Set<String> invalidStatuses, final ExpandableExamAttributes... expandableAttributes);

    /**
     * Fetches an {@link tds.exam.ExpandableExam} for the given id (if one exists) with additional properties based on the
     * {@link tds.exam.ExpandableExamAttributes} that are requested.
     *
     * @param examId           The id of the {@link tds.exam.ExpandableExam} to fetch
     * @param expandableAttributes parameters representing the optional expandable data to include
     * @return The {@link tds.exam.ExpandableExam} containing additional data
     */
    Optional<ExpandableExam> findExam(final UUID examId, final ExpandableExamAttributes... expandableAttributes);
}
