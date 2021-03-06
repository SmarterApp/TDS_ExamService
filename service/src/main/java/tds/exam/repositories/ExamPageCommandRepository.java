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

import java.util.UUID;

import tds.exam.ExamPage;

/**
 * Handles data modification for the exam_page table
 */
public interface ExamPageCommandRepository {
    /**
     * Inserts a collection of {@link tds.exam.ExamPage}s
     *
     * @param examPages One or more {@link tds.exam.ExamPage}s to insert
     */
    void insert(final ExamPage... examPages);

    /**
     * Marks all {@link tds.exam.ExamPage}s for the exam as deleted
     *
     * @param examId the id of the {@link tds.exam.Exam}
     */
    void deleteAll(final UUID examId);

    /**
     * Update an {@link tds.exam.ExamPage}.
     *
     * @param examPages One or more {@link tds.exam.ExamPage} to update
     */
    void update(final ExamPage... examPages);
}
