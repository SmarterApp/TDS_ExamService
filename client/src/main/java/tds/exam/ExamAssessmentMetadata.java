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

import static tds.common.util.Preconditions.checkNotNull;

/**
 * An object containing recent exam data for an assessment
 */
public class ExamAssessmentMetadata {
    private String assessmentKey;
    private String assessmentId;
    private int attempt;
    private int maxAttempts;
    private String assessmentLabel;
    private String status;
    private String subject;
    private String grade;
    private String deniedReason;

    /**
     * Private constructor for frameworks
     */
    private ExamAssessmentMetadata() {}

    private ExamAssessmentMetadata(Builder builder) {
        this.subject = checkNotNull(builder.subject);
        this.assessmentKey = checkNotNull(builder.assessmentKey);
        this.assessmentLabel = builder.assessmentLabel;
        this.status = checkNotNull(builder.status);
        this.grade = checkNotNull(builder.grade);
        this.attempt = checkNotNull(builder.attempt);
        this.maxAttempts = checkNotNull(builder.maxAttempts);
        this.assessmentId = checkNotNull(builder.assessmentId);
        this.deniedReason = builder.deniedReason;
    }

    public static class Builder {
        private String assessmentKey;
        private String assessmentId;
        private int attempt;
        private int maxAttempts;
        private String assessmentLabel;
        private String status;
        private String subject;
        private String grade;
        private String deniedReason;

        public Builder withAssessmentKey(String assessmentKey) {
            this.assessmentKey = assessmentKey;
            return this;
        }

        public Builder withAssessmentId(String assessmentId) {
            this.assessmentId = assessmentId;
            return this;
        }

        public Builder withAttempt(int attempt) {
            this.attempt = attempt;
            return this;
        }

        public Builder withMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
            return this;
        }

        public Builder withAssessmentLabel(String assessmentLabel) {
            this.assessmentLabel = assessmentLabel;
            return this;
        }

        public Builder withStatus(String status) {
            this.status = status;
            return this;
        }

        public Builder withSubject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder withGrade(String grade) {
            this.grade = grade;
            return this;
        }

        public Builder withDeniedReason(String deniedReason) {
            this.deniedReason = deniedReason;
            return this;
        }

        public ExamAssessmentMetadata build() {
            return new ExamAssessmentMetadata(this);
        }
    }

    /**
     * @return The key of the assessment for the exam
     */
    public String getAssessmentKey() {
        return assessmentKey;
    }

    /**
     * @return The id of the assessment for the exam
     */
    public String getAssessmentId() {
        return assessmentId;
    }

    /**
     * If the assessment has already been completed before,
     * this value will be the attempt number of the previous exam + 1.
     *
     * @return The current attempt number available for the assessment - 1 based.
     */
    public int getAttempt() {
        return attempt;
    }

    /**
     * @return The maximum number of attempts for an assessment
     */
    public int getMaxAttempts() {
        return maxAttempts;
    }

    /**
     * @return The label for the assessment
     */
    public String getAssessmentLabel() {
        return assessmentLabel;
    }

    /**
     * @return The status of exam assessment
     */
    public String getStatus() {
        return status;
    }

    /**
     * @return The subject of the assessment
     */
    public String getSubject() {
        return subject;
    }

    /**
     * @return The grade of the assessment
     */
    public String getGrade() {
        return grade;
    }

    /**
     * @return The reason why the assessment is unavailable
     */
    public String getDeniedReason() {
        return deniedReason;
    }
}
