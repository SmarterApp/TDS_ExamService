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

import org.joda.time.Instant;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import tds.exam.Exam;
import tds.exam.models.Ability;

/**
 * Data access for exams
 */
public interface ExamQueryRepository {
    /**
     * Retrieves the exam by uniqueKey
     *
     * @param examId exam id
     * @return the {@link tds.exam.Exam Exam} if found otherwise empty
     */
    Optional<Exam> getExamById(final UUID examId);

    /**
     * Retrieves the latest exam that isn't ended
     *
     * @param studentId    student id associated with the exam
     * @param assessmentId assessment id for the exam
     * @param clientName   client name for the exam
     * @return the {@link tds.exam.Exam Exam} if found otherwise empty
     */
    Optional<Exam> getLastAvailableExam(final long studentId, final String assessmentId, final String clientName);

    /**
     * Retrieves the {@link java.time.Instant} the {@link tds.exam.Exam} was last paused, an
     * {@link tds.exam.ExamItemResponse} was last submitted, or a {@link tds.exam.ExamPage} was last
     * created at.
     *
     * @param examId the exam id of the paused exam
     * @return the time the exam was last paused
     */
    Optional<Instant> findLastStudentActivity(final UUID examId);

    /**
     * Find all {@link tds.exam.Exam}s that belong to a {@link tds.session.Session} so they can be paused.
     *
     * @param sessionId The unique identifier of the session
     * @param statuses  A {@code java.util.Set} the statuses to filter by
     * @return A collection of exams that are assigned to the specified session
     */
    List<Exam> findAllExamsInSessionWithStatus(final UUID sessionId, final Set<String> statuses);

    /**
     * Find all {@link tds.exam.Exam}s that belong to a {@link tds.session.Session} that do not have the provided
     * status codes
     *
     * @param sessionId The unique identifier of the session
     * @param statuses  A {@code java.util.Set} the statuses to filter by
     * @return A collection of exams that are assigned to the specified session
     */
    List<Exam> findAllExamsInSessionWithoutStatus(final UUID sessionId, final Set<String> statuses);

    /**
     * Retrieves a listing of all ability records for the specified exam and student.
     *
     * @param exam       the exam for which to exclude from the ability query
     * @param clientName client name for the exam
     * @param subject    the subject of the exam
     * @param studentId  the student taking the exam
     * @return a list of {@link Ability} objects for past exams
     */
    List<Ability> findAbilities(final UUID exam, final String clientName, final String subject, final Long studentId);

    /**
     * List of exams pending approval from the proctor
     *
     * @param sessionId session id of the exams pending approval
     * @return list of {@link tds.exam.Exam} pending approval
     */
    List<Exam> getExamsPendingApproval(final UUID sessionId);

    /**
     * Finds all exams taken by a student
     *
     * @param studentId the id of the student to fetch exams for
     * @return The list of {@link tds.exam.Exam}s the student has taken
     */
    List<Exam> findAllExamsForStudent(final long studentId);
}
