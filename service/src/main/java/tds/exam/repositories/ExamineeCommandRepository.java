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

import tds.exam.ExamineeAttribute;
import tds.exam.ExamineeRelationship;

/**
 * Handles data modification for the examinee_attribute and examinee_relationshiptables
 */
public interface ExamineeCommandRepository {
    /**
     * Insert a collection of {@link tds.exam.ExamineeAttribute}s into the database.
     *
     * @param attributes One or more {@link tds.exam.ExamineeAttribute}s to insert
     */
    void insertAttributes(final ExamineeAttribute... attributes);

    /**
     * Insert a collection of {@link tds.exam.ExamineeRelationship}s into the database.
     *
     * @param relationships One or more {@link tds.exam.ExamineeRelationship}s to insert
     */
    void insertRelationships(final ExamineeRelationship... relationships);
}
