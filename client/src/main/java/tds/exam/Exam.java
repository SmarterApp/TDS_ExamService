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

import org.joda.time.Instant;

import java.util.UUID;

import tds.common.util.Preconditions;

/**
 * Class representing an exam
 */
public class Exam {
    private UUID id;
    private UUID sessionId;
    private UUID browserId;
    private String assessmentId;
    private long studentId;
    private int attempts;
    private int maxItems;
    private ExamStatusCode status;
    private String statusChangeReason;
    private Instant statusChangedAt;
    private String clientName;
    private String subject;
    private Instant startedAt;
    private Instant changedAt;
    private Instant deletedAt;
    private Instant scoredAt;
    private Instant completedAt;
    private Instant createdAt;
    private Instant expiresAt;
    private String loginSSID;
    private String studentName;
    private Instant dateJoined;
    private String assessmentWindowId;
    private String assessmentAlgorithm;
    private String assessmentKey;
    private String environment;
    private String languageCode;
    private boolean segmented;
    private int abnormalStarts;
    private int waitingForSegmentApprovalPosition;
    private int currentSegmentPosition;
    private boolean customAccommodations;
    private int resumptions;
    private int restartsAndResumptions;
    private String browserUserAgent;
    private boolean multiStageBraille;

    /**
     * Private constructor for frameworks
     */
    private Exam() {
    }

    public static class Builder {
        private UUID id;
        private UUID sessionId;
        private UUID browserId;
        private String assessmentId;
        private long studentId;
        private int attempts;
        private int maxItems;
        private ExamStatusCode status;
        private String statusChangeReason;
        private Instant statusChangedAt;
        private String clientName;
        private String subject;
        private Instant startedAt;
        private Instant changedAt;
        private Instant deletedAt;
        private Instant createdAt;
        private Instant scoredAt;
        private Instant completedAt;
        private Instant expiresAt;
        private String loginSSID;
        private String studentName;
        private Instant dateJoined;
        private String assessmentWindowId;
        private String assessmentAlgorithm;
        private String assessmentKey;
        private String environment;
        private String languageCode;
        private boolean segmented;
        private int abnormalStarts;
        private int waitingForSegmentApprovalPosition;
        private int currentSegmentPosition;
        private boolean customAccommodations;
        private int resumptions;
        private int restartsAndResumptions;
        private String browserUserAgent;
        private boolean multiStageBraille;

        public Builder withSegmented(boolean segmented) {
            this.segmented = segmented;
            return this;
        }

        public Builder withLoginSSID(String loginSSID) {
            this.loginSSID = loginSSID;
            return this;
        }

        public Builder withStudentName(String studentName) {
            this.studentName = studentName;
            return this;
        }

        public Builder withJoinedAt(Instant joinedAt) {
            this.dateJoined = joinedAt;
            return this;
        }

        public Builder withAssessmentWindowId(String windowId) {
            this.assessmentWindowId = windowId;
            return this;
        }

        public Builder withAssessmentAlgorithm(String assessmentAlgorithm) {
            this.assessmentAlgorithm = assessmentAlgorithm;
            return this;
        }

        public Builder withAssessmentKey(String assessmentKey) {
            this.assessmentKey = assessmentKey;
            return this;
        }

        public Builder withEnvironment(String environment) {
            this.environment = environment;
            return this;
        }

        public Builder withId(UUID newId) {
            id = newId;
            return this;
        }

        public Builder withSessionId(UUID newSessionId) {
            sessionId = newSessionId;
            return this;
        }

        public Builder withBrowserId(UUID newBrowserId) {
            browserId = newBrowserId;
            return this;
        }

        public Builder withAssessmentId(String newAssessmentId) {
            assessmentId = newAssessmentId;
            return this;
        }

        public Builder withStudentId(long newStudentId) {
            studentId = newStudentId;
            return this;
        }

        public Builder withAttempts(int attempts) {
            this.attempts = attempts;
            return this;
        }

        public Builder withMaxItems(int maxItems) {
            this.maxItems = maxItems;
            return this;
        }

        public Builder withStatus(ExamStatusCode newStatus, Instant statusChangedAt) {
            Preconditions.checkNotNull(newStatus, "status cannot be null");
            Preconditions.checkNotNull(statusChangedAt, "status change date cannot be null");

            status = newStatus;
            this.statusChangedAt = statusChangedAt;
            return this;
        }

        public Builder withStatusChangeReason(String newStatusChangeReason) {
            statusChangeReason = newStatusChangeReason;
            return this;
        }

        public Builder withSubject(String newSubject) {
            subject = newSubject;
            return this;
        }

        public Builder withClientName(String newClientName) {
            clientName = newClientName;
            return this;
        }

        public Builder withScoredAt(Instant scoredAt) {
            this.scoredAt = scoredAt;
            return this;
        }

        public Builder withStartedAt(Instant startedAt) {
            this.startedAt = startedAt;
            return this;
        }

        public Builder withChangedAt(Instant changedAt) {
            this.changedAt = changedAt;
            return this;
        }

        public Builder withDeletedAt(Instant deletedAt) {
            this.deletedAt = deletedAt;
            return this;
        }

        public Builder withCreatedAt(Instant newCreatedAt) {
            createdAt = newCreatedAt;
            return this;
        }

        public Builder withCompletedAt(Instant completedAt) {
            this.completedAt = completedAt;
            return this;
        }

        public Builder withExpiresAt(Instant expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public Builder withAbnormalStarts(int abnormalStarts) {
            this.abnormalStarts = abnormalStarts;
            return this;
        }

        public Builder withWaitingForSegmentApprovalPosition(int waitingForSegmentApprovalPosition) {
            this.waitingForSegmentApprovalPosition = waitingForSegmentApprovalPosition;
            return this;
        }

        public Builder withCurrentSegmentPosition(int currentSegmentPosition) {
            this.currentSegmentPosition = currentSegmentPosition;
            return this;
        }

        public Builder withLanguageCode(String languageCode) {
            this.languageCode = languageCode;
            return this;
        }

        public Builder withCustomAccommodation(boolean customAccommodation) {
            this.customAccommodations = customAccommodation;
            return this;
        }

        public Builder withResumptions(int resumptions) {
            this.resumptions = resumptions;
            return this;
        }

        public Builder withRestartsAndResumptions(int restartsAndResumptions) {
            this.restartsAndResumptions = restartsAndResumptions;
            return this;
        }

        public Builder withBrowserUserAgent(String browserUserAgent) {
            this.browserUserAgent = browserUserAgent;
            return this;
        }

        public Builder withMultiStageBraille(boolean multiStageBraille) {
            this.multiStageBraille = multiStageBraille;
            return this;
        }

        public Builder fromExam(Exam exam) {
            id = exam.id;
            sessionId = exam.sessionId;
            browserId = exam.browserId;
            assessmentId = exam.assessmentId;
            studentId = exam.studentId;
            attempts = exam.attempts;
            maxItems = exam.maxItems;
            status = exam.status;
            statusChangeReason = exam.statusChangeReason;
            statusChangedAt = exam.statusChangedAt;
            subject = exam.subject;
            clientName = exam.clientName;
            startedAt = exam.startedAt;
            changedAt = exam.changedAt;
            deletedAt = exam.deletedAt;
            scoredAt = exam.scoredAt;
            createdAt = exam.createdAt;
            completedAt = exam.completedAt;
            expiresAt = exam.expiresAt;
            loginSSID = exam.loginSSID;
            studentName = exam.studentName;
            dateJoined = exam.dateJoined;
            assessmentWindowId = exam.assessmentWindowId;
            assessmentAlgorithm = exam.assessmentAlgorithm;
            assessmentKey = exam.assessmentKey;
            environment = exam.environment;
            segmented = exam.segmented;
            abnormalStarts = exam.abnormalStarts;
            waitingForSegmentApprovalPosition = exam.waitingForSegmentApprovalPosition;
            currentSegmentPosition = exam.currentSegmentPosition;
            languageCode = exam.languageCode;
            customAccommodations = exam.isCustomAccommodations();
            resumptions = exam.resumptions;
            restartsAndResumptions = exam.restartsAndResumptions;
            browserUserAgent = exam.browserUserAgent;
            multiStageBraille = exam.multiStageBraille;
            return this;
        }

        public Exam build() {
            return new Exam(this);
        }
    }

    private Exam(Builder builder) {
        id = builder.id;
        sessionId = builder.sessionId;
        browserId = builder.browserId;
        assessmentId = builder.assessmentId;
        studentId = builder.studentId;
        attempts = builder.attempts;
        maxItems = builder.maxItems;
        status = builder.status;
        statusChangeReason = builder.statusChangeReason;
        statusChangedAt = builder.statusChangedAt;
        subject = builder.subject;
        clientName = builder.clientName;
        startedAt = builder.startedAt;
        changedAt = builder.changedAt;
        deletedAt = builder.deletedAt;
        scoredAt = builder.scoredAt;
        createdAt = builder.createdAt;
        completedAt = builder.completedAt;
        expiresAt = builder.expiresAt;
        loginSSID = builder.loginSSID;
        studentName = builder.studentName;
        dateJoined = builder.dateJoined;
        assessmentWindowId = builder.assessmentWindowId;
        assessmentAlgorithm = builder.assessmentAlgorithm;
        assessmentKey = builder.assessmentKey;
        environment = builder.environment;
        segmented = builder.segmented;
        abnormalStarts = builder.abnormalStarts;
        waitingForSegmentApprovalPosition = builder.waitingForSegmentApprovalPosition;
        currentSegmentPosition = builder.currentSegmentPosition;
        customAccommodations = builder.customAccommodations;
        languageCode = builder.languageCode;
        resumptions = builder.resumptions;
        restartsAndResumptions = builder.restartsAndResumptions;
        browserUserAgent = builder.browserUserAgent;
        multiStageBraille = builder.multiStageBraille;
    }

    /**
     * @return The unique identifier of the exam
     */
    public UUID getId() {
        return id;
    }

    /**
     * @return The identifier of the session that hosts the exam
     */
    public UUID getSessionId() {
        return sessionId;
    }

    /**
     * @return The identifier of the browser of the browser information for thie eaxm
     * <p>
     * "Browser information" refers to IP address, user-agent etc, from another table.
     * </p>
     */
    public UUID getBrowserId() {
        return browserId;
    }

    /**
     * @return The id of the assessment this exam represents
     */
    public String getAssessmentId() {
        return assessmentId;
    }

    /**
     * @return The identifier of the student taking the exam
     */
    public long getStudentId() {
        return studentId;
    }

    /**
     * @return The number of times this exam has been attempted
     */
    public int getAttempts() {
        return attempts;
    }

    /**
     * @return The maximum number of items in the exam
     */
    public int getMaxItems() {
        return maxItems;
    }

    /**
     * @return The current status of the exam
     */
    public ExamStatusCode getStatus() {
        return status;
    }

    /**
     * @return Text describing the reason for the most recent status change.
     * <p>
     * Sources for this value include restarting an exam or when a Proctor denies approval to start an exam.
     * </p>
     */
    public String getStatusChangeReason() {
        return statusChangeReason;
    }

    /**
     * @return The date when the {@link tds.exam.Exam}'s status changed.
     */
    public Instant getStatusChangedAt() {
        return statusChangedAt;
    }

    /**
     * @return The client name that owns the exam
     */
    public String getClientName() {
        return clientName;
    }


    /**
     * @return The subject of the exam
     */
    public String getSubject() {
        return subject;
    }

    /**
     * @return The date and time when the exam was started
     */
    public Instant getStartedAt() {
        return startedAt;
    }

    /**
     * @return The most recent date and time at which the exam was changed
     */
    public Instant getChangedAt() {
        return changedAt;
    }

    /**
     * @return The date and time when the exam was deleted
     */
    public Instant getDeletedAt() {
        return deletedAt;
    }

    /**
     * @return The date and time when the exam was completed
     */
    public Instant getCompletedAt() {
        return completedAt;
    }

    /**
     * @return The date and time when the exam record was created
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * @return The date and time when the exam was scored
     */
    public Instant getScoredAt() {
        return scoredAt;
    }

    /**
     * @return The date and time of when the exam expires from.
     */
    public Instant getExpiresAt() {
        return expiresAt;
    }

    /**
     * @return the student key of the student taking the exam
     */
    public String getLoginSSID() {
        return loginSSID;
    }

    /**
     * @return the name of the student taking the exam
     */
    public String getStudentName() {
        return studentName;
    }

    /**
     * @return the time when the student joined the session
     */
    public Instant getDateJoined() {
        return dateJoined;
    }

    /**
     * @return the assessment window id
     */
    public String getAssessmentWindowId() {
        return assessmentWindowId;
    }

    /**
     * @return the algorithm associated with the assessment
     */
    public String getAssessmentAlgorithm() {
        return assessmentAlgorithm;
    }

    /**
     * @return the unique key for the assessment
     */
    public String getAssessmentKey() {
        return assessmentKey;
    }

    /**
     * @return the environment.
     */
    public String getEnvironment() {
        return environment;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    /**
     * @return {@code true} associated assessment is segmented
     */
    public boolean isSegmented() {
        return segmented;
    }

    /**
     * @return the number of times an exam was started under abnormal instances.  For example,
     * restarting an exam that was already in the process of being started.
     */
    public int getAbnormalStarts() {
        return abnormalStarts;
    }

    /**
     * @return {@code true} when approval is required before the student can move onto the next segment
     */
    public int getWaitingForSegmentApprovalPosition() {
        return waitingForSegmentApprovalPosition;
    }

    /**
     * @return the current segment position in multi segmented exams
     */
    public int getCurrentSegmentPosition() {
        return currentSegmentPosition;
    }

    /**
     * @return {@code true} if exam accommodations are not defaults
     */
    public boolean isCustomAccommodations() {
        return customAccommodations;
    }

    /**
     * @return the number of exam restarts (resumes) within the grace period
     */
    public int getResumptions() {
        return resumptions;
    }

    /**
     * @return the total number of exam restarts
     */
    public int getRestartsAndResumptions() {
        return restartsAndResumptions;
    }

    /**
     * @return a string containing browser user agent metadata
     */
    public String getBrowserUserAgent() {
        return browserUserAgent;
    }

    /**
     * @return {@code true} if the exam is multi stage braille
     */
    public boolean isMultiStageBraille() {
        return multiStageBraille;
    }
}
