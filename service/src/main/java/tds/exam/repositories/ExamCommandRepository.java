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

import tds.exam.Exam;

/**
 * Handles data modification in the exam related tables
 */
public interface ExamCommandRepository {
    /**
     * Saves the {@link tds.exam.Exam}
     *
     * @param exam a non null {@link tds.exam.Exam}
     */
    void insert(final Exam exam);

    /**
     * Update a collection of {@link tds.exam.Exam}s
     *
     * @param exams The collection of exams to update
     */
    void update(final Exam... exams);
}
