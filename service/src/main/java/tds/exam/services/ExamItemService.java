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
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import tds.common.Response;
import tds.common.ValidationError;
import tds.exam.ExamItem;
import tds.exam.ExamItemResponse;
import tds.exam.ExamPage;

/**
 * A service for interacting with an {@link tds.exam.Exam}'s {@link tds.exam.ExamItem}s and their associated
 * {@link tds.exam.ExamItemResponse}s.
 */
public interface ExamItemService {
    /**
     * Persist one or more {@link tds.exam.ExamItemResponse}s
     *
     * @param examId                 The ID of the exam
     * @param mostRecentPagePosition The last page number that has responses
     * @param responses              The collection of the {@link tds.exam.ExamItemResponse}s to persist
     * @return The next {@link tds.exam.ExamPage} that has {@link tds.exam.ExamItem}s that require student responses
     */
    Response<ExamPage> insertResponses(final UUID examId, final int mostRecentPagePosition, final ExamItemResponse... responses);

    /**
     * Fetches the highest exam position - the position of the {@link tds.exam.ExamItem} that
     * was last responded to by a student.
     *
     * @param examId the id of the {@link tds.exam.Exam}
     * @return the position of the last {@link tds.exam.ExamItem} responded to by a student
     */
    int getExamPosition(final UUID examId);

    /**
     * Fetches a map of examIds for their respective number of items responded to
     *
     * @param examIds the ids of the exams to fetch response counts for
     * @return a mapping of examIds to the number of items that exam has responded to
     */
    Map<UUID, Integer> getResponseCounts(final UUID... examIds);

    /**
     * Marks or unmarks an item for review for the given exam id and position
     *
     * @param examId   the id of the {@link tds.exam.Exam} to mark for review
     * @param position the position of the item to mark for review
     * @param mark     a boolean value representing whether an item should be marked or unmarked
     * @return an {@link tds.common.ValidationError} if one occurs
     */
    Optional<ValidationError> markForReview(final UUID examId, final int position, final Boolean mark);

    /**
     * Finds the items and associated response for an exam
     *
     * @param examId exam UUID
     * @return List of {@link tds.exam.ExamItem} and their associated {@link tds.exam.ExamItemResponse}
     */
    List<ExamItem> findExamItemAndResponses(final UUID examId);

    /**
     * Fetches Exam Item and Response based on exam id and position
     *
     * @param examId   the exam UUID
     * @param position the item position
     * @return ExamItem if found otherwise empty.  ExamItemResponse will be empty if a response is not present
     */
    Optional<ExamItem> findExamItemAndResponse(final UUID examId, int position);

    /**
     * Fetches a mapping of the ids of the {@link tds.exam.ExamItem} to their number of responses
     *
     * @param examId the exam UUID
     * @return A map of the item id to the number of response updates
     */
    Map<UUID,Integer> getResponseUpdateCounts(final UUID examId);
}
