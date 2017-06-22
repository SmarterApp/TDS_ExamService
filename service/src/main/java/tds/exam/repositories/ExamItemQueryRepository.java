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
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import tds.exam.ExamItem;

/**
 * Handles data reads from the exam_item_response table
 */
public interface ExamItemQueryRepository {

    /**
     * Gets the item position of the last item that has a response
     *
     * @param examId the id of the {@link tds.exam.Exam} to find the position for
     * @return the item position of the last item responded to
     */

    int getCurrentExamItemPosition(final UUID examId);

    /**
     * Fetches a map of examIds for their respective number of items responded to
     *
     * @param examIds the ids of the exams to fetch response counts for
     * @return a mapping of examIds to the number of items that exam has responded to
     */
    Map<UUID, Integer> getResponseCounts(final UUID... examIds);

    /**
     * Fetches Exam Item and Response based on exam id and position
     *
     * @param examId   the exam UUID
     * @param position the item position
     * @return ExamItem if found otherwise empty.  ExamItemResponse will be empty if a response is not present
     */
    Optional<ExamItem> findExamItemAndResponse(final UUID examId, int position);

    /**
     * Fetches all the exam items and the associated valid responses for the exam
     * @param examId the exam UUID
     * @return List of {@link tds.exam.ExamItem} with the most active {@link tds.exam.ExamItemResponse}
     */
    List<ExamItem> findExamItemAndResponses(final UUID examId);

    /**
     * Fetches a mapping of the ids of the {@link tds.exam.ExamItem} to their number of responses
     *
     * @param examId the exam UUID
     * @return A map of the item id to the number of response updates
     */
    Map<UUID,Integer> getResponseUpdateCounts(final UUID examId);
}