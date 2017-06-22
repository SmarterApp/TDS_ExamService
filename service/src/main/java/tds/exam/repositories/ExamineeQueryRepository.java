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

import tds.exam.ExamineeAttribute;
import tds.exam.ExamineeContext;
import tds.exam.ExamineeRelationship;

/**
 * Handles data reads from the examinee_attribute and examinee_relationship tables
 */
public interface ExamineeQueryRepository {
    /**
     * Find the {@link tds.exam.ExamineeAttribute}s associated with an {@link tds.exam.Exam} for all contexts
     *
     * @param examId  The unique identifier of the {@link tds.exam.Exam}
     * @return A collection of {@link tds.exam.ExamineeAttribute}s for the specified {@link tds.exam.ExamineeContext}
     */
    List<ExamineeAttribute> findAllAttributes(UUID examId);

    /**
     * Find the {@link tds.exam.ExamineeAttribute}s associated with an {@link tds.exam.Exam} for the specified
     * {@link tds.exam.ExamineeContext}
     *
     * @param examId  The unique identifier of the {@link tds.exam.Exam}
     * @param context The {@link tds.exam.ExamineeContext} in which the attributes were stored
     * @return A collection of {@link tds.exam.ExamineeAttribute}s for the specified {@link tds.exam.ExamineeContext}
     */
    List<ExamineeAttribute> findAllAttributes(final UUID examId, final ExamineeContext context);

    /**
     * Find the {@link tds.exam.ExamineeRelationship}s associated with an {@link tds.exam.Exam}
     *
     * @param examId  The unique identifier of the {@link tds.exam.Exam}
     * @return A collection of {@link tds.exam.ExamineeRelationship}s for the specified {@link tds.exam.ExamineeContext}
     */
    List<ExamineeRelationship> findAllRelationships(UUID examId);

    /**
     * Find the {@link tds.exam.ExamineeRelationship}s associated with an {@link tds.exam.Exam} for the specified
     * {@link tds.exam.ExamineeContext}
     *
     * @param examId  The unique identifier of the {@link tds.exam.Exam}
     * @param context The {@link tds.exam.ExamineeContext} in which the relationships were stored
     * @return A collection of {@link tds.exam.ExamineeRelationship}s for the specified {@link tds.exam.ExamineeContext}
     */
    List<ExamineeRelationship> findAllRelationships(final UUID examId, final ExamineeContext context);
}
