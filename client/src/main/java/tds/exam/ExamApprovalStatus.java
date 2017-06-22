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

/**
 * Enumerate the possible status values for exam approval.
 * <p>
 *     This enumeration maps to the {@code OpportunityApprovalStatus} contained within the
 *     {@code tds.student.services.data.ApprovalInfo} class of the legacy Student application.
 * </p>
 */
public enum ExamApprovalStatus {
    WAITING("waiting"),
    APPROVED("approved"),
    DENIED("denied"),
    LOGOUT("paused");

    private final String code;

    ExamApprovalStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    /**
     * Map the {@link ExamStatusCode} to the correct approval status enum.
     *
     * @param status The status value from the {@link ExamStatusCode}
     * @return The appropriate {@link ExamApprovalStatus} for the {@link Exam}'s status; otherwise
     * {@code ExamApprovalStatus.WAITING}.
     */
    public static ExamApprovalStatus fromExamStatus(String status) {
        if (status == null) throw new IllegalArgumentException("status cannot be null");

        // Can't use Java 8 optional or streams here; must be Java 7 compliant.
        for (ExamApprovalStatus approvalStatus : ExamApprovalStatus.values()) {
            if (approvalStatus.getCode().equals(status.toLowerCase())) {
                return approvalStatus;
            }
        }

        return ExamApprovalStatus.WAITING;
    }
}
