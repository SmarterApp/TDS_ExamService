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
import java.util.Optional;
import java.util.UUID;

import tds.exam.ExamPage;

/**
 * Handles data reads from the exam_page tables
 */
public interface ExamPageQueryRepository {
    /**
     * Fetches all non-deleted {@link tds.exam.ExamPage}s for an exam
     *
     * @param examId the id of the {@link tds.exam.Exam} that the {@link tds.exam.ExamPage}s correspond to
     * @return A collection of active {@link tds.exam.ExamPage}s for the specified exam
     */
    List<ExamPage> findAll(final UUID examId);

    /**
     * Fetch a single {@link tds.exam.ExamPage}
     *
     * @param examId   The id of the {@link tds.exam.Exam} that the {@link tds.exam.ExamPage} corresponds to
     * @param position the position number
     * @return An {@link tds.exam.ExamPage} for the specified {@link tds.exam.Exam} id and page position
     */
    Optional<ExamPage> find(final UUID examId, final int position);

    /**
     * Finds page by id
     *
     * @param pageId the id for the page
     * @return {@link tds.exam.ExamPage} if found otherwise empty
     */
    Optional<ExamPage> find(final UUID pageId);
}
