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

import tds.exam.Exam;
import tds.exam.ExamineeAttribute;
import tds.exam.ExamineeContext;
import tds.exam.ExamineeRelationship;
import tds.student.Student;

/**
 * A service for interacting with {@link tds.exam.ExamineeAttribute}s and {@link tds.exam.ExamineeRelationship}s
 */
public interface ExamineeService {
    /**
     * Fetches a list of {@link tds.exam.ExamineeAttribute}s for the given examId
     *
     * @param examId          The id of the {@link tds.exam.Exam} for which to fetch attributes for
     * @return The list of attributes for the given exam and context
     */
    List<ExamineeAttribute> findAllAttributes(final UUID examId);

    /**
     * Fetches a list of {@link tds.exam.ExamineeRelationship}s for the given examId
     *
     * @param examId          The id of the {@link tds.exam.Exam} for which to fetch relationships for
     * @return The list of attributes for the given exam and context
     */
    List<ExamineeRelationship> findAllRelationships(final UUID examId);

    /**
     * Insert the {@link tds.exam.ExamineeAttribute}s and {@link tds.exam.ExamineeRelationship}s for this
     * {@link tds.exam.Exam} for the specified {@link tds.exam.ExamineeContext}.
     *
     * @param exam    The {@link tds.exam.Exam} for which attributes and relationships are being stored
     * @param context The {@link tds.exam.ExamineeContext} that describes the state of the examniee's information when
     *                these attributes and relationships were collected
     */
    void insertAttributesAndRelationships(final Exam exam, final ExamineeContext context);

    /**
     * Insert the {@link tds.exam.ExamineeAttribute}s and {@link tds.exam.ExamineeRelationship}s for this
     * {@link tds.exam.Exam} for the specified {@link tds.exam.ExamineeContext}.
     *
     * @param exam    The {@link tds.exam.Exam} for which attributes and relationships are being stored
     * @param student the {@link tds.student.Student} with the attributes and relationships
     * @param context The {@link tds.exam.ExamineeContext} that describes the state of the examniee's information when
     *                these attributes and relationships were collected
     */
    void insertAttributesAndRelationships(final Exam exam, final Student student, final ExamineeContext context);
}
