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

import tds.exam.wrapper.ExamPageWrapper;

/**
 * Finds {@link tds.exam.wrapper.ExamPageWrapper}
 */
public interface ExamPageWrapperQueryRepository {
    /**
     * Fetch an {@link tds.exam.wrapper.ExamPageWrapper} with its collection of {@link tds.exam.ExamItem}s.
     *
     * @param examId   the id of the {@link tds.exam.Exam} that the {@link tds.exam.ExamPage} corresponds to
     * @param position the position number
     * @return An {@link tds.exam.ExamPage} with its collection of {@link tds.exam.ExamItem}s for the specified exam
     * page.
     */
    Optional<ExamPageWrapper> findPageWithItems(final UUID examId, final int position);

    /**
     * Finds the exam pages with items for an exam
     *
     * @param examId the exam id
     * @return List of all {@link tds.exam.wrapper.ExamPageWrapper} for an exam id
     */
    List<ExamPageWrapper> findPagesWithItems(final UUID examId);

    /**
     * Finds the exam pages for a particular exam segment
     *
     * @param examId     exam id
     * @param segmentKey the segment key
     * @return List of {@link tds.exam.wrapper.ExamPageWrapper} for the exam segment
     */
    List<ExamPageWrapper> findPagesForExamSegment(final UUID examId, final String segmentKey);
}
