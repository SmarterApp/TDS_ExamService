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

import tds.common.Response;
import tds.common.ValidationError;
import tds.exam.Exam;
import tds.exam.ExamApproval;
import tds.exam.ExamInfo;

/**
 * Service to support requesting approval for interacting with a {@link tds.exam.Exam}.
 */
public interface ExamApprovalService {
    /**
     * Get approval for the open exam request.
     * <p>
     * This method is called in a loop by the Student application while waiting for the Proctor to approve or deny
     * the Student's request to start his/her exam.
     * </p>
     *
     * @param examInfo The {@link tds.exam.ExamInfo} representing the request to open the specified exam.
     * @return {@link tds.exam.ExamApproval} describing whether the exam is approved to be opened.
     */
    Response<ExamApproval> getApproval(final ExamInfo examInfo);

    /**
     * List of exams pending approval from the proctor
     *
     * @param sessionId session id of the exams pending approval
     * @return list of {@link tds.exam.Exam} pending approval
     */
    List<Exam> getExamsPendingApproval(final UUID sessionId);

    /**
     * Verify all the rules for granting approval to an {@link tds.exam.Exam} are satisfied.
     * <p>
     * The rules are:
     * <ul>
     * <li>The browser key of the approval request must match the browser key of the {@link tds.exam.Exam}.</li>
     * <li>The session id of the approval request must match the session id of the {@link tds.exam.Exam}.</li>
     * <li>The {@link tds.session.Session} must be open (unless the environment is set to "simulation" or "development")</li>
     * <li>The TA Check-In time window cannot be passed</li>
     * </ul>
     * <strong>NOTE:</strong>  If the {@link tds.session.Session} has no Proctor (because the {@link tds.session.Session} is a guest session
     * or is otherwise proctor-less), approval is granted as long as the {@link tds.session.Session} is open.
     * </p>
     *
     * @param examInfo The {@link tds.exam.ExamInfo} being evaluated
     * @param exam            The {@link tds.exam.Exam} for which approval is being requested
     * @return An empty optional if the approval rules are satisfied; otherwise an optional containing a
     * {@link tds.common.ValidationError} describing the rule that was not satisfied
     */
    Optional<ValidationError> verifyAccess(final ExamInfo examInfo, final Exam exam);
}
