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

package tds.exam;

import java.util.UUID;

/**
 * Describe the approval of a request to start an {@link Exam}.
 */
public class ExamApproval {
    private UUID examId;
    private ExamApprovalStatus examApprovalStatus;
    private String statusChangeReason;
    private String examStatusCode;

    public ExamApproval(UUID examId, ExamStatusCode examStatusCode, String statusChangeReason) {
        this.examId = examId;
        this.statusChangeReason = statusChangeReason;
        this.examStatusCode = examStatusCode.getCode();
        this.examApprovalStatus = ExamApprovalStatus.fromExamStatus(examStatusCode.getCode());
    }

    /**
     * Private constructor for frameworks
     */
    private ExamApproval() {
    }

    /**
     * @return The id of the {@link Exam} for which approval is requested.
     */
    public UUID getExamId() {
        return examId;
    }
    
    /**
     * @return The status code of the exam
     */
    public String getExamStatusCode() {
        return examStatusCode;
    }

    /**
     * @return The status of the exam approval.
     */
    public ExamApprovalStatus getExamApprovalStatus() {
        return examApprovalStatus;
    }

    /**
     * @return The text explaining the reason behind the {@link Exam}'s most recent status change.
     */
    public String getStatusChangeReason() {
        return statusChangeReason;
    }
}
