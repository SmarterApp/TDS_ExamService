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
 * Represents a request for updating the status of an {@link tds.exam.Exam}
 */
public class ExamStatusRequest {
    private ExamStatusCode examStatus;
    private String reason;

    /* Empty private constructor for frameworks */
    private ExamStatusRequest() {

    }

    public ExamStatusRequest(final ExamStatusCode examStatus, final String reason) {
        this.examStatus = examStatus;
        this.reason = reason;
    }

    /**
     * @return The status to update the exam to
     */
    public ExamStatusCode getExamStatus() {
        return examStatus;
    }

    /**
     * @return The reason for the status change
     */
    public String getReason() {
        return reason;
    }
}
