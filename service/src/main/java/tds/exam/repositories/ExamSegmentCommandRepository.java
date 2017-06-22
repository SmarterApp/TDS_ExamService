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

import tds.exam.ExamSegment;

/**
 * Repository for writing to the exam_segment and exam_segment_event tables.
 */
public interface ExamSegmentCommandRepository {

    /**
     * Inserts a list of {@link ExamSegment}s into the exam_segment table.
     *
     * @param segment the segment to insert
     */
    void insert(final List<ExamSegment> segment);

    /**
     * Inserts an exam segment event into the exam_segment_event table.
     *
     * @param segment the segment to update
     */
    void update(final ExamSegment segment);

    /**
     * Inserts a list of exam segment events into the exam_segment_event table.
     *
     * @param segment the segment to update
     */
    void update(final List<ExamSegment> segment);
}
