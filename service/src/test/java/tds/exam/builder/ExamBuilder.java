package tds.exam.builder;

import org.joda.time.Instant;

import java.util.UUID;

import tds.exam.Exam;
import tds.exam.ExamStatusCode;

/**
 * Build an {@link Exam} populated with test data.
 */
public class ExamBuilder {
    private UUID examId = UUID.fromString("af880054-d1d2-4c24-805c-0dfdb45a0d24");
    private UUID sessionId = UUID.fromString("244363EE-D4D3-4C02-AFAE-52FFE1AEAC33");
    private UUID browserId = UUID.fromString("38446ebf-e181-482d-8e41-3ca21ba66303");
    private String assessmentId = "assessmentId";
    private long studentId = 1L;
    private int attempts = 0;
    private String clientName = "clientName";
    private Instant dateDeleted = null;
    private Instant dateScored = null;
    private Instant dateChanged = null;
    private Instant dateStarted = null;
    private Instant dateCompleted = null;
    private ExamStatusCode status = new ExamStatusCode.Builder()
        .withStatus("pending")
        .build();
    private String subject = "ELA";
    private String studentKey = "ADV001";
    private String studentName = "Darth";
    private Instant dateJoined = null;
    private String assessmentWindowId = "ANNUAL";
    private String assessmentAlgorithm = "fixedForm";
    private String assessmentKey = "(SBAC_PT)SBAC-IRP-CAT-ELA-3-Summer-2015-2016";
    private String environment = "Development";
    private boolean segmented = false;

    public Exam build() {
        return new Exam.Builder()
            .withId(examId)
            .withSessionId(sessionId)
            .withBrowserId(browserId)
            .withAssessmentId(assessmentId)
            .withStudentId(studentId)
            .withAttempts(attempts)
            .withClientName(clientName)
            .withDateDeleted(dateDeleted)
            .withDateScored(dateScored)
            .withDateChanged(dateChanged)
            .withDateStarted(dateStarted)
            .withDateCompleted(dateCompleted)
            .withStatus(status)
            .withSubject(subject)
            .withStudentKey(studentKey)
            .withDateJoined(dateJoined)
            .withAssessmentWindowId(assessmentWindowId)
            .withStudentName(studentName)
            .withAssessmentAlgorithm(assessmentAlgorithm)
            .withAssessmentKey(assessmentKey)
            .withEnvironment(environment)
            .withSegmented(segmented)
            .build();
    }

    public ExamBuilder withStudentKey(String studentKey) {
        this.studentKey = studentKey;
        return this;
    }

    public ExamBuilder withStudentName(String studentName) {
        this.studentName = studentName;
        return this;
    }

    public ExamBuilder withDateJoined(Instant dateJoined) {
        this.dateJoined = dateJoined;
        return this;
    }

    public ExamBuilder withAssessmentWindowId(String assessmentWindowId) {
        this.assessmentWindowId = assessmentWindowId;
        return this;
    }

    public ExamBuilder withAssessmentAlgorithm(String assessmentAlgorithm) {
        this.assessmentAlgorithm = assessmentAlgorithm;
        return this;
    }

    public ExamBuilder withAssessmentKey(String assessmentKey) {
        this.assessmentKey = assessmentKey;
        return this;
    }

    public ExamBuilder withEnvironment(String environment) {
        this.environment = environment;
        return this;
    }

    public ExamBuilder withSegmented(boolean segmented) {
        this.segmented = segmented;
        return this;
    }

    public ExamBuilder withId(UUID examId) {
        this.examId = examId;
        return this;
    }

    public ExamBuilder withSessionId(UUID sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    public ExamBuilder withBrowserId(UUID browserId) {
        this.browserId = browserId;
        return this;
    }

    public ExamBuilder withAssessmentId(String assessmentId) {
        this.assessmentId = assessmentId;
        return this;
    }

    public ExamBuilder withStudentId(long studentId) {
        this.studentId = studentId;
        return this;
    }

    public ExamBuilder withAttempts(int attempts) {
        this.attempts = attempts;
        return this;
    }

    public ExamBuilder withClientName(String clientName) {
        this.clientName = clientName;
        return this;
    }

    public ExamBuilder withDateDeleted(Instant dateDeleted) {
        this.dateDeleted = dateDeleted;
        return this;
    }

    public ExamBuilder withDateScored(Instant dateScored) {
        this.dateScored = dateScored;
        return this;
    }

    public ExamBuilder withDateChanged(Instant dateChanged) {
        this.dateChanged = dateChanged;
        return this;
    }

    public ExamBuilder withDateStarted(Instant dateStarted) {
        this.dateStarted = dateStarted;
        return this;
    }

    public ExamBuilder withDateCompleted(Instant dateCompleted) {
        this.dateCompleted = dateCompleted;
        return this;
    }

    public ExamBuilder withStatus(ExamStatusCode status) {
        this.status = status;
        return this;
    }

    public ExamBuilder withSubject(String subject) {
        this.subject = subject;
        return this;
    }
}
