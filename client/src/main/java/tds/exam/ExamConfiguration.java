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
 * Class containing exam configuration data
 */
public class ExamConfiguration {
    private Exam exam;
    private int contentLoadTimeoutMinutes;
    private int interfaceTimeoutMinutes;
    private int examRestartWindowMinutes;
    private int requestInterfaceTimeoutMinutes;
    private int prefetch;
    private int startPosition;
    private String status;
    private String failureMessage;
    private int testLength;
    private boolean validateCompleteness;

    /**
     * Private constructor for frameworks
     */
    private ExamConfiguration() {
    }

    private ExamConfiguration(Builder builder) {
        this.exam = builder.exam;
        this.contentLoadTimeoutMinutes = builder.contentLoadTimeoutMinutes;
        this.interfaceTimeoutMinutes = builder.interfaceTimeoutMinutes;
        this.examRestartWindowMinutes = builder.examRestartWindowMinutes;
        this.requestInterfaceTimeoutMinutes = builder.requestInterfaceTimeoutMinutes;
        this.prefetch = builder.prefetch;
        this.startPosition = builder.startPosition;
        this.status = builder.status;
        this.failureMessage = builder.failureMessage;
        this.testLength = builder.testLength;
        this.validateCompleteness = builder.validateCompleteness;
    }

    public static class Builder {
        private Exam exam;
        private int contentLoadTimeoutMinutes;
        private int interfaceTimeoutMinutes;
        private int examRestartWindowMinutes;
        private int requestInterfaceTimeoutMinutes;
        private int prefetch;
        private int startPosition;
        private String status;
        private String failureMessage;
        private int testLength;
        private boolean validateCompleteness;

        public Builder withExam(Exam exam) {
            this.exam = exam;
            return this;
        }

        public Builder withContentLoadTimeout(int contentLoadTimeout) {
            this.contentLoadTimeoutMinutes = contentLoadTimeout;
            return this;
        }

        public Builder withInterfaceTimeout(int interfaceTimeout) {
            this.interfaceTimeoutMinutes = interfaceTimeout;
            return this;
        }

        public Builder withExamRestartWindowMinutes(int examRestartWindowMinutes) {
            this.examRestartWindowMinutes = examRestartWindowMinutes;
            return this;
        }

        public Builder withPrefetch(int prefetch) {
            this.prefetch = prefetch;
            return this;
        }

        public Builder withRequestInterfaceTimeout(int requestInterfaceTimeout) {
            this.requestInterfaceTimeoutMinutes = requestInterfaceTimeout;
            return this;
        }

        public Builder withStartPosition(int startPosition) {
            this.startPosition = startPosition;
            return this;
        }

        public Builder withStatus(String status) {
            this.status = status;
            return this;
        }

        public Builder withTestLength(int testLength) {
            this.testLength = testLength;
            return this;
        }

        public Builder withValidateCompleteness(boolean validateCompleteness) {
            this.validateCompleteness = validateCompleteness;
            return this;
        }

        public Builder withFailureMessage(String failureMessage) {
            this.failureMessage = failureMessage;
            return this;
        }

        public ExamConfiguration build() {
            return new ExamConfiguration(this);
        }
    }

    /**
     * @return The exam that was started
     */
    public Exam getExam() {
        return exam;
    }

    /**
     * @return The timeout period of content load
     */
    public int getContentLoadTimeoutMinutes() {
        return contentLoadTimeoutMinutes;
    }

    /**
     * @return The timeout period for interface inactivity
     */
    public int getInterfaceTimeoutMinutes() {
        return interfaceTimeoutMinutes;
    }

    /**
     * @return The number of minutes after an exam is started where an exam is eligible for a resume
     */
    public int getExamRestartWindowMinutes() {
        return examRestartWindowMinutes;
    }

    /**
     * @return The number of pages to prefetch for the exam
     */
    public int getPrefetch() {
        return prefetch;
    }

    /**
     * @return The timeout period for proctor approval requests
     */
    public int getRequestInterfaceTimeoutMinutes() {
        return requestInterfaceTimeoutMinutes;
    }

    /**
     * @return The position of the exam to begin from
     */
    public int getStartPosition() {
        return startPosition;
    }

    /**
     * @return The status of the exam
     */
    public String getStatus() {
        return status;
    }

    /**
     * @return An optional failure message if the exam fails to start
     */
    public String getFailureMessage() {
        return failureMessage;
    }

    /**
     * @return The number of items in an exam
     */
    public int getTestLength() {
        return testLength;
    }

    /**
     * @return Flag for validating that all exam items are answered before submission
     */
    public boolean isValidateCompleteness() {
        return validateCompleteness;
    }
}
