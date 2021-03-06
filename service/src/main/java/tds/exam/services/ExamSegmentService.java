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

import tds.assessment.Assessment;
import tds.common.ValidationError;
import tds.exam.Exam;
import tds.exam.ExamSegment;

/**
 * Service that handles interactions with exam segments.
 */
public interface ExamSegmentService {

    /**
     * Initializes the {@link ExamSegment}s for the {@link Exam}.
     *
     * @param exam       The {@link Exam} to initialize segments for
     * @param assessment The {@link tds.assessment.Assessment} containing the {@link tds.assessment.Segment}s to initialize
     * @return The number of total items for all exam segments initialized.
     */
    int initializeExamSegments(final Exam exam, final Assessment assessment);

    /**
     * Fetches the {@link tds.exam.ExamSegment}s for the exam id after validating the exam and session.
     *
     * @param examId The id of the exam to fetch the exam segments for
     * @return The list of {@link tds.exam.ExamSegment}s for the exam
     */
    List<ExamSegment> findExamSegments(final UUID examId);

    /**
     * Find an {@link tds.exam.ExamSegment} for the specified exam Id and position.
     *
     * @param examId          The id of the {@link tds.exam.Exam} to fetch the exam segments for
     * @param segmentPosition The position/sequence of the {@link tds.exam.ExamSegment} to find
     * @return The {@link tds.exam.ExamSegment} for the specified exam id and position
     */
    Optional<ExamSegment> findByExamIdAndSegmentPosition(final UUID examId, final int segmentPosition);

    /**
     * Update a one or more {@link tds.exam.ExamSegment}s with new values.
     *
     * @param examSegments The {@link tds.exam.ExamSegment}s to update
     */
    void update(final ExamSegment... examSegments);

    /**
     * Marks an {@link tds.exam.ExamSegment} as exited
     *
     * @param examId          The exam id of the {@link tds.exam.ExamSegment} to exit
     * @param segmentPosition The segment position of the {@link tds.exam.ExamSegment} to exit
     */
    Optional<ValidationError> exitSegment(final UUID examId, final int segmentPosition);

    /**
     * Checks if all the segments for the exam are completed/"satisfied"
     *
     * @param examId The id of the {@link tds.exam.Exam} to check for
     * @return {@code true} if the segment is completed
     */
    boolean checkIfSegmentsCompleted(final UUID examId);

    /**
     * Finds an {@link tds.exam.ExamSegment} by exam id and segment key
     *
     * @param examId     the exam id
     * @param segmentKey the {@link tds.exam.ExamSegment} segment key
     * @return an {@link tds.exam.ExamSegment} otherwise empty
     */
    Optional<ExamSegment> findByExamIdAndSegmentKey(final UUID examId, final String segmentKey);
}
