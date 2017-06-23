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

import tds.exam.wrapper.ExamSegmentWrapper;

public interface ExamSegmentWrapperService {

    /**
     * Finds all the {@link tds.exam.wrapper.ExamSegmentWrapper} for an exam
     *
     * @param examId the exam id
     * @return list of {@link tds.exam.wrapper.ExamSegmentWrapper}
     */
    List<ExamSegmentWrapper> findAllExamSegments(final UUID examId);

    /**
     * Finds an {@link tds.exam.wrapper.ExamSegmentWrapper} with all its {@link tds.exam.ExamPage} by exam id and segment position
     *
     * @param examId          the exam id
     * @param segmentPosition the segment position
     * @return an {@link tds.exam.wrapper.ExamSegmentWrapper} otherwise empty
     */
    Optional<ExamSegmentWrapper> findExamSegment(final UUID examId, final int segmentPosition);

    /**
     * Finds an {@link tds.exam.wrapper.ExamSegmentWrapper} with only a single {@link tds.exam.ExamPage} populated
     *
     * @param examId          the exam id
     * @param segmentPosition the segment position
     * @param pagePosition    the page position within the exam
     * @return an {@link tds.exam.wrapper.ExamSegmentWrapper} otherwise empty
     */
    Optional<ExamSegmentWrapper> findExamSegmentWithPageAtPosition(final UUID examId, final int segmentPosition, final int pagePosition);

    /**
     * Finds the exam segment and page at for the exam id at the page position
     *
     * @param examId       exam id
     * @param pagePosition the {@link tds.exam.wrapper.ExamPageWrapper} page position
     * @return an {@link tds.exam.wrapper.ExamSegmentWrapper} if segment/page are found otherwise empty
     */
    Optional<ExamSegmentWrapper> findExamSegmentWithPageAtPosition(final UUID examId, final int pagePosition);
}
