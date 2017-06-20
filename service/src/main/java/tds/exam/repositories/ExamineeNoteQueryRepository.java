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

import tds.exam.ExamineeNote;

/**
 * Handles data reads from the examinee_note table
 */
public interface ExamineeNoteQueryRepository {
    /**
     * Get the most recent {@link tds.exam.ExamineeNote} for the specified {@link tds.exam.Exam}
     *
     * @param examId The unique identifier of the {@link tds.exam.Exam}
     * @return The {@link tds.exam.ExamineeNote} with an {@link tds.exam.ExamineeNoteContext} of "exam"
     */
    Optional<ExamineeNote> findNoteInExamContext(final UUID examId);

    /**
     * Finds all {@link tds.exam.ExamineeNote} for the specified {@link tds.exam.Exam}
     *
     * @param examId
     * @return the {@link tds.exam.ExamineeNote}s for the {@link tds.exam.Exam}
     */
    List<ExamineeNote> findAllNotes(final UUID examId);
}
