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

import tds.exam.ExamAccommodation;

/**
 * Processes data modification calls to for {@link tds.exam.ExamAccommodation}
 */
public interface ExamAccommodationCommandRepository {
    /**
     * Inserts exam accommodations for the exam
     *
     * @param accommodations list of {@link tds.exam.ExamAccommodation} to insert
     */
    void insert(final List<ExamAccommodation> accommodations);

    /**
     * Updates the exam accommodations for the exam
     *
     * @param accommodation {@link tds.exam.ExamAccommodation} to update
     */
    void update(final ExamAccommodation... accommodation);

    /**
     * Deletes the exam accommodations for the exam
     *
     * @param accommodations {@link tds.exam.ExamAccommodation} to delete
     */
    void delete(final List<ExamAccommodation> accommodations);
}
