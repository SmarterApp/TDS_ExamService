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

import tds.exam.ExamItem;
import tds.exam.ExamItemResponse;

/**
 * Handles data modification for the exam_item and exam_item_response tables
 * <p>
 * Typically, a table describing an entity will have an associated "_event" table to record changes to that entity's
 * state (e.g. {@code exam} for the entity and {@code exam_event} for the changes to the {@link tds.exam.Exam}.
 * {@link tds.exam.ExamItem}s never change after they are created; only the responses made to them can change.  As
 * such, the "_event" equivalent for an {@link tds.exam.ExamItem} is the {@code exam_item_response} table.  Since the
 * {@link tds.exam.ExamItemResponse} entity represents the "event" for an {@link tds.exam.ExamItem}, the methods for
 * storing both entities are contained within this repository.
 * </p>
 */
public interface ExamItemCommandRepository {
    /**
     * Insert a collection of {@link tds.exam.ExamItem}s.
     *
     * @param examItems The collection of {@link tds.exam.ExamItem}s to insert
     */
    void insert(final ExamItem... examItems);

    /**
     * Insert one or more {@link tds.exam.ExamItemResponse}s.
     *
     * @param responses The collection of {@link tds.exam.ExamItemResponse}s to insert
     */
    void insertResponses(final ExamItemResponse... responses);
}
