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
import java.util.UUID;

import tds.exam.ExamPage;

/**
 * Service for interacting with exam pages
 */
public interface ExamPageService {
    /**
     * Inserts a {@link java.util.List} of {@link tds.exam.ExamPage}s
     *
     * @param examPages A collection of {@link tds.exam.ExamPage}s to insert
     */
    void insertPages(ExamPage... examPages);

    /**
     * Marks all {@link tds.exam.ExamPage}s as "deleted" for the exam.
     *
     * @param examId the id of the {@link tds.exam.Exam}
     */
    void deletePages(final UUID examId);

    /**
     * Fetches a list of all {@link tds.exam.ExamPage}s for an exam.
     *
     * @param examId the id of the {@link tds.exam.Exam}
     * @return A collection of all {@link tds.exam.ExamPage}s for the specified exam Id
     */
    List<ExamPage> findAllPages(final UUID examId);

    /**
     * Finds an {@link tds.exam.ExamPage} by its id
     *
     * @param id the exam page id
     * @return {@link tds.exam.ExamPage} if found otherwise empty
     */
    Optional<ExamPage> find(final UUID id);

    /**
     * Updates the exam page
     *
     * @param examPage exam pages to update
     */
    void update(ExamPage... examPage);
}
